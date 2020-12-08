package github.ainr.domain

import cats.effect.{Sync, Timer}
import cats.implicits._
import github.ainr.db.DbAccess
import github.ainr.tinvest4s.models.Operation.Operation
import github.ainr.tinvest4s.models._
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import org.slf4j.LoggerFactory


trait Core[F[_]] {
  def init(): F[Unit]
  def start(): F[Unit]
  def handleTgMessage(userId: Long, text: String): F[String]
}



/**
 * В этом классе сосредоточена логика по работе с ботом
 *
 * @param dbAccess Для доступа к базе данных
 * @param tinvestRestApi Для взаимодействия с REST Тинькофф OpenApi
 * @param tinvestWSApi Для взаимодействия со Streaming Тинькофф OpenApi
 * @param notificationRepo Для отправки уведимлений пользователю
 * @author [[https://github.com/a-khakimov]]
 */
class CoreImpl[F[_]: Sync : Timer](implicit dbAccess: DbAccess[F],
                                   implicit val tinvestRestApi: TInvestApi[F],
                                   implicit val tinvestWSApi: TInvestWSApi[F],
                                   implicit val notificationRepo: NotificationRepo[F])
  extends Core[F] {

  private val log = LoggerFactory.getLogger("Core")

  /**
   *
   * @return
   */
  override def start(): F[Unit] = {
    for {
      _ <- marketOrderSellByOperation()
    } yield ()
  }

  override def init(): F[Unit] = {
    for {
      activeOpsE <- dbAccess.getOpsByStatus(OperationStatus.Active)
      _ <- activeOpsE match {
        case Left(e) => Sync[F].delay(log.error(s"$e"))
        case Right(activeOps) => activeOps.map {
          op => {
            Sync[F].delay(log.info(s"${op.figi} ${op.operationStatus}"))
            op.figi
          }
        }.distinct.traverse {
          figi => tinvestWSApi.subscribeCandle(figi, CandleResolution.`1min`) >>
            Sync[F].delay(log.info(s"subscribeCandle: $figi"))
        }
      }
    } yield ()
  }

  private def marketOrderSellByOperation(): F[Unit] = {
    for {
      opsE <- dbAccess.getOpsByStatus(OperationStatus.Running)
      _ <- opsE match {
        case Left(e) => Sync[F].delay(log.error("$e"))
        case Right(ops) => {
          ops.traverse {
            op => {
              for {
                orderResult <- tinvestRestApi.marketOrder(op.figi, MarketOrderRequest(op.executedLots, Operation.Sell))
                _ <- orderResult match {
                  case Left(e) => Sync[F].delay(log.error(s"Error market order request - ${e.status} ${e.payload}"))
                  case Right(r) => op.id match {
                    case None => Sync[F].delay(log.error("Unknown operation ID"))
                    case Some(id) =>
                      tinvestWSApi.unsubscribeCandle(op.figi, CandleResolution.`1min`) >>
                        dbAccess.updateOperationStatus(id, OperationStatus.Completed) >>
                        Sync[F].delay(log.info(s"Success market order sell $id ${r.status} ${r.payload}")) >>
                        notificationRepo.push(Notification(op.tgUserId,
                          s"[$id] Рыночная заявка на продажу ${op.figi} выполнена успешно"))
                  }
                }
              } yield ()
            }
          }
        }
      }
    } yield ()
  }

  def portfolioMsg(): F[String] = {
    for {
      portfolio <- tinvestRestApi.getPortfolio
      _ <- Sync[F].delay(log.info(portfolio.toString))
      msg <- portfolio match {
        case Right(p) => s"${p.payload.positions.sortBy(_.instrumentType).map {
            pos => s"`${pos.figi} ${pos.instrumentType} [${pos.name}] balance ${pos.balance}`"
          }.mkString("\n")
        }".pure[F]
        case Left(e) =>
          Sync[F].delay(log.error(e.toString)) >>
            s"Error: ${e.status}".pure[F]
      }
    } yield msg
  }

  def marketInstrumentMsg(instrument: String): F[String] = {
    for {
      currencies <- instrument match {
        case "stocks" => tinvestRestApi.stocks()
        case "bonds" => tinvestRestApi.bonds()
        case "etfs" => tinvestRestApi.etfs()
        case "currencies" => tinvestRestApi.currencies()
      }
      _ <- Sync[F].delay(log.info(currencies.toString))
      msg <- currencies match {
        case Right(p) => s"${p.payload.instruments.map {
            pos => s"`${pos.figi} ${pos.name}`"
          }.mkString("\n")
        }".pure[F]
        case Left(e) => s"`Error: ${e.status}`".pure[F]
      }
    } yield msg
  }

  private def parseLimitOrderArgs(text: String): Option[(Int, Double, String)] = {
    val args = text.filter(c => c != '/' || c != ' ').split('.')
    args.length match {
      case 4 =>
        val figi = args(1)
        (args(2).toIntOption, args(3).toDoubleOption) match {
          case (lots, price) if lots.isDefined && price.isDefined => Some(lots.get, price.get, figi)
          case (_, _) => None
        }
      case _ => None
    }
  }

  def doLimitOrder(operation: String, text: String): F[String] = {
    val parsedArgs = parseLimitOrderArgs(text)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args : (Int, Double, String)) =>
        val (lots, price, figi) = args
        for {
          result <- tinvestRestApi.limitOrder(figi, LimitOrderRequest(lots, operation, price))
          reply = result match {
            case Right(r) => s"`Success: orderId - ${r.payload.orderId}`"
            case Left(e) => {
              log.error(e.toString)
              s"`Error: ${e.status}`"
            }
          }
        } yield reply
    }
  }

  private def parseMarketOrderArgs(args: Array[String]): Option[(Int, String)] = {
    val figi = args(1)
    args(2).toIntOption match {
      case lots if lots.isDefined => Some(lots.get, figi)
      case _ => None
    }
  }

  private def parseSmartMarketOrderBuyArgs(args: Array[String]): Option[(String, Int, Double, Double)] = {
    val figi = args(1)
    (args(2).toIntOption, args(3).toDoubleOption, args(4).toDoubleOption) match {
      case (lots, sloss, tprofit) if lots.isDefined && sloss.isDefined && tprofit.isDefined =>
        Some(figi, lots.get, sloss.get, tprofit.get)
      case _ => None
    }
  }

  def doMarketOrderCmd(operation: String, args: String, userId: Long = 0): F[String] = {
    val sArgs = args.filter(c => c != '/' || c != ' ').split('.')
    sArgs.length match {
      case 3 => doSimpleMarketOrder(operation, sArgs)
      case 5 => doSmartMarketOrderBuy(userId, sArgs)
      case _ => "Wrong command".pure[F]
    }
  }

  private def doSimpleMarketOrder(operation: Operation, args: Array[String]): F[String] = {
    val parsedArgs = parseMarketOrderArgs(args)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args : (Int, String)) =>
        val (lots, figi) = args
        for {
          result <- tinvestRestApi.marketOrder(figi, MarketOrderRequest(lots, operation))
          reply = result match {
            case Right(r) => s"`Success: orderId - ${r.payload.orderId}`"
            case Left(e) => {
              log.error(e.toString)
              s"`Error: ${e.status}`"
            }
          }
        } yield reply
    }
  }

  private def doSmartMarketOrderBuy(userId: Long, args: Array[String]): F[String] = {
    val parsedArgs = parseSmartMarketOrderBuyArgs(args)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args: (String, Int, Double, Double)) =>
        val (figi, lots, stopLoss, takeProfit) = args
        for {
          checkRes <- checkStopLossAndTakeProfit(figi, stopLoss, takeProfit)
          msg <- checkRes match {
            case Left(e) => e.pure[F]
            case Right(checkMsg) =>
              for {
                orderE <- tinvestRestApi.marketOrder(figi, MarketOrderRequest(lots, Operation.Buy))
                msg <- orderE match {
                  case Left(e) =>
                    Sync[F].delay(log.error(e.toString)) >>
                      s"`Error: ${e.status}``".pure[F]
                  case Right(order) => for {
                    _ <- registerOperation(figi, stopLoss, takeProfit, userId, order.payload)
                    msg <- s"""|$checkMsg
                               |Выполнена покупка акций $figi, количество $lots
                               |""".stripMargin.pure[F]
                  } yield msg
                }
              } yield msg
          }
        } yield msg
    }
  }

  private def checkStopLossAndTakeProfit(figi: String,
                                         stopLoss: Double,
                                         takeProfit: Double)
  : F[Either[String, String]] = {
    for {
      orderBookE <- tinvestRestApi.orderbook(figi, 1)
      result <- orderBookE match {
        case Left(e) =>
          Sync[F].delay(log.error(e.toString)) >>
            Left(s"`Error: ${e.status}`").pure[F]
        case Right(orderBook) =>
          orderBook.payload.lastPrice match {
            case None => Left(s"Не удалось получить текущую стоимость для $figi").pure[F]
            case Some(lastPrice) =>
              if (lastPrice <= stopLoss) Left(s"StopLoss($stopLoss) выше чем стоимость акции ($lastPrice)").pure[F]
              else if (lastPrice >= takeProfit) Left(s"TakeProfit($takeProfit) ниже чем стоимость акции ($lastPrice)").pure[F]
              else Right(s"Текущая стоимость акции ($lastPrice)").pure[F]
          }
      }
    } yield result
  }

  private def registerOperation(figi: String,
                                stopLoss: Double,
                                takeProfit: Double,
                                userId: Long,
                                order: PlacedOrder)
  : F[Unit] = {
    val operation = BotOperation(
      None,
      figi,
      stopLoss,
      takeProfit,
      OperationStatus.Active,
      order.orderId,
      order.status,
      order.operation,
      order.requestedLots,
      order.executedLots,
      userId
    )
    for {
      currentOpsE <- dbAccess.getOpsByStatus(OperationStatus.Active)
      _ <- currentOpsE match {
        case Left(e) => Sync[F].delay(log.error(s"$e"))
        case Right(currentOps) => {
          /* Делаем подписку на свечи только при условии если подписка на figi отсутствует */
          val subscribed = currentOps.exists(_.figi == figi)
          if (subscribed) {
            Sync[F].delay(log.warn(s"This $figi was previously subscribed"))
          } else {
            tinvestWSApi.subscribeCandle(figi, CandleResolution.`1min`) >> // TODO: Что если тут произойдет ошибка?
              Sync[F].delay(log.info(s"subscribeCandle: $figi"))
          }
        }
      }
      _ <- dbAccess.insertOperation(operation) // TODO: Что если тут произойдет ошибка?
    } yield ()
  }

  private def doCancelOrder(args: String): F[String] = {
    val orderid = args.split('.').last
    for {
      result <- tinvestRestApi.cancelOrder(orderid)
      reply = result match {
        case Right(r) => s"`Success`"
        case Left(e) => {
          log.error(e.toString)
          s"`Error: ${e.status}`"
        }
      }
    } yield reply
  }

  private def parseOrderbookArgs(text: String): Option[(Int, String)] = {
    val args = text.filter(c => c != '/' || c != ' ').split('.')
    args.length match {
      case 3 =>
        val figi = args(1)
        args(2).toIntOption match {
          case depth if depth.isDefined => Some(depth.get, figi)
          case _ => None
        }
      case _ => None
    }
  }

  def doOrderbook(args: String): F[String] = {
    val parsedArgs = parseOrderbookArgs(args)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args: (Int, String)) =>
        val (depth, figi) = args
        for {
          result <- tinvestRestApi.orderbook(figi, depth)
          reply = result match {
            case Right(ordbook) => {
              s"""|`lastPrice:${ordbook.payload.lastPrice.getOrElse(0)}`
                  |`closePrice:${ordbook.payload.closePrice.getOrElse(0)}`
                  |`limitUp:${ordbook.payload.limitUp.getOrElse(0)}`
                  |`limitDown:${ordbook.payload.limitDown.getOrElse(0)}`
                  |`tradeStatus:${ordbook.payload.tradeStatus}`
                  |`bids:${ordbook.payload.bids.map(b => s"(${b.price} ${b.quantity})").mkString(",")}`
                  |`asks:${ordbook.payload.asks.map(a => s"(${a.price} ${a.quantity})").mkString(",")}`
                  |""".stripMargin
            }
            case Left(e) => {
              log.error(e.toString)
              s"`Error: ${e.status}`"
            }
          }
        } yield reply
    }
  }

  def activeOperations: F[String] = {
    for {
      opsE <- dbAccess.getOpsByStatus(OperationStatus.Active)
      msg <- opsE match {
        case Left(e) => s"${e.getMessage}".pure[F]
        case Right(ops) => {
          if (ops.isEmpty) {
            s"Список активных операций пуст".pure[F]
          } else {
            ops.map {
              op =>
                s"`UserID[${op.tgUserId}] [${op.id.getOrElse(-1)}] ${op.figi} " +
                  s"Lots=${op.executedLots} TakeProfit=${op.takeProfit} StopLoss=${op.stopLoss}`\n"
            }.mkString("").pure[F]
          }
        }
      }
    } yield msg
  }

  def stopOperations: F[String] = {
    for {
      opsE <- dbAccess.getOpsByStatus(OperationStatus.Active)
      results <- opsE match {
        case Left(e) => Seq(s"${e.getMessage}").pure[F]
        case Right(ops) => {
          if (ops.isEmpty) {
            Seq(s"Список активных операций пуст").pure[F]
          } else {
            ops.traverse {
              op =>
                op.id match {
                  case None => s"Wrong operation id".pure[F]
                  case Some(id) => {
                    for {
                      _ <- dbAccess.updateOperationStatus(id, OperationStatus.Stop) // TODO: Что если тут произойдет ошибка?
                      _ <- tinvestWSApi.unsubscribeCandle(op.figi, CandleResolution.`1min`) // TODO: Что если тут произойдет ошибка?
                      _ <- Sync[F].delay(log.info(s"Stop operation with id[$id]"))
                      msg <- s"`Stop $id ${op.figi} ${op.stopLoss} ${op.takeProfit}`".pure[F]
                    } yield msg
                  }
                }
            }
          }
        }
      }
      msg <- results.mkString("\n").pure[F]
    } yield msg
  }

  override def handleTgMessage(userId: Long, text: String): F[String] = {
    for {
      reply <- text match {
        case "/help" => helpMsg()
        case "/portfolio" => portfolioMsg()
        case "/stocks" => marketInstrumentMsg("stocks") /* TODO: слишком длинное сообщение */
        case "/bonds" => marketInstrumentMsg("bonds") /* TODO: слишком длинное сообщение */
        case "/etfs" => marketInstrumentMsg("etfs")
        case "/currencies" => marketInstrumentMsg("currencies")
        case args if args.startsWith("/orderbook.") => doOrderbook(args)
        case args if args.startsWith("/cancelOrder.") => doCancelOrder(args)
        case args if args.startsWith("/limitOrderBuy.") => doLimitOrder("Buy", args)
        case args if args.startsWith("/limitOrderSell.") => doLimitOrder("Sell", args)
        case args if args.startsWith("/marketOrderBuy.") => doMarketOrderCmd("Buy", args, userId)
        case args if args.startsWith("/marketOrderSell.") => doMarketOrderCmd("Sell", args, userId)
        case "/activeOperations" => activeOperations
        case "/stopOperations" => stopOperations
        case _ => helpMsg()
      }
    } yield reply
  }

  private def helpMsg(): F[String] = {
    s"""
       |Список базовых команд:
       |/portfolio - Портфель
       |/etfs - Получение списка ETF
       |/currencies - Получение списка валютных пар
       |/orderbook.`figi.depth` - Получение стакана по FIGI
       |/limitOrderBuy.`figi.lots.price` - Лимитная заявка на покупку
       |/limitOrderSell.`figi.lots.price` - Лимитная заявка на продажу
       |/marketOrderBuy.`figi.lots` - Рыночная заявка на покупку
       |/marketOrderSell.`figi.lots` - Рыночная заявка на продажу
       |/cancelOrder.`orderId` - Отмена заявки по OrderId
       |
       |Дополнительные команды:
       |/marketOrderBuy.`figi.lots.stoploss.takeprofit` - Рыночная заявка на покупку с указанными значениями `stoploss` и `takeprofit`
       |/activeOperations - Получить список активных операций
       |/stopOperations - Остановить активные операции
       |""".stripMargin.pure[F]
    /*
    * - /stocks
    * - /bonds
    */
  }
}
