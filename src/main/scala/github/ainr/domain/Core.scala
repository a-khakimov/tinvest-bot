package github.ainr.domain

import cats.effect.{Sync, Timer}
import cats.implicits._
import github.ainr.db.DbAccess
import github.ainr.domain.OperationStatus.OperationStatus
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

class CoreImpl[F[_]: Sync : Timer](implicit dbAccess: DbAccess[F],
                                   implicit val tinvestRestApi: TInvestApi[F],
                                   implicit val tinvestWSApi: TInvestWSApi[F],
                                   implicit val notificationRepo: NotificationRepo[F])
  extends Core[F] {

  private val log = LoggerFactory.getLogger("Core")

  override def start(): F[Unit] = {
    for {
      _ <- marketOrderSellByOperation()
    } yield ()
  }

  override def init(): F[Unit] = {
    for {
      activeOps <- dbAccess.getOpsByStatus(OperationStatus.Active)
      _ <- activeOps.map(_.figi).distinct.traverse {
        figi => tinvestWSApi.subscribeCandle(figi, CandleResolution.`1min`) >>
          Sync[F].delay(log.info(s"subscribeCandle: $figi"))
      }
    } yield ()
  }

  private def marketOrderSellByOperation(): F[Unit] = {
    for {
      ops <- dbAccess.getOpsByStatus(OperationStatus.Running)
      _ <- ops.traverse {
        op => {
          for {
            orderResult <- tinvestRestApi.marketOrder(op.figi, MarketOrderRequest(op.executedLots, Operation.Sell))
            _ <- orderResult match {
              case Left(e) => Sync[F].delay(log.error(s"Error market order request - ${e.status} ${e.payload}"))
              case Right(r) => op.id match {
                case None => Sync[F].delay(log.error("Unknown operation id"))
                case Some(id) => dbAccess.updateOperationStatus(id, OperationStatus.Completed) >>
                  Sync[F].delay(log.error(s"Success market order sell $id ${r.status} ${r.payload}")) >>
                  notificationRepo.push(Notification(op.tgUserId, s"Success market order sell $id"))
              }
            }
          } yield ()
        }
      }
    } yield ()
  }

  private def portfolioMsg(): F[String] = {
    for {
      portfolio <- tinvestRestApi.getPortfolio
      _ <- Sync[F].delay(log.info(portfolio.toString))
      msg <- portfolio match {
        case Right(p) => s"${p.payload.positions.sortBy(_.instrumentType).map {
            pos => s"`${pos.figi} ${pos.instrumentType} [${pos.name}] balance ${pos.balance}, lots ${pos.lots}`"
          }.mkString("\n")
        }".pure[F]
        case Left(e) => s"Error: ${e}".pure[F]
      }
    } yield msg
  }

  private def marketInstrumentMsg(instrument: String): F[String] = {
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
        case Left(e) => s"`Error: ${e.status} ${e.payload.message}``".pure[F]
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

  private def doLimitOrder(operation: String, text: String): F[String] = {
    val parsedArgs = parseLimitOrderArgs(text)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args : (Int, Double, String)) =>
        val (lots, price, figi) = args
        for {
          result <- tinvestRestApi.limitOrder(figi, LimitOrderRequest(lots, operation, price))
          reply = result match {
            case Right(r) => s"`Success: orderId - ${r.payload.orderId}`"
            case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`"
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

  private def doMarketOrderCmd(operation: String, args: String, userId: Long = 0): F[String] = {
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
            case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`"
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
                  case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`".pure[F]
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
        case Left(e) => Left(s"${e.status}: ${e.payload.message.getOrElse("Unknown")}").pure[F]
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
      _ <- dbAccess.insertOperation(operation)
      currentOps <- dbAccess.getOpsByStatus(OperationStatus.Active)
      _ <- {
        /* Делаем подписку на свечи только при условии если подписка на figi отсутствует */
        val subscribed = currentOps.exists(_.figi == figi)
        if (subscribed) Sync[F].delay(log.info(s"This $figi was previously subscribed"))
        else tinvestWSApi.subscribeCandle(figi, CandleResolution.`1min`)
      }
    } yield ()
  }

  private def doCancelOrder(args: String): F[String] = {
    val orderid = args.split('.').last
    for {
      result <- tinvestRestApi.cancelOrder(orderid)
      reply = result match {
        case Right(r) => s"`Success`"
        case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`"
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

  private def doOrderbook(args: String): F[String] = {
    val parsedArgs = parseOrderbookArgs(args)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args: (Int, String)) =>
        val (depth, figi) = args
        for {
          result <- tinvestRestApi.orderbook(figi, depth)
          reply = result match {
            case Right(ordbook) => s"""|`lastPrice:${ordbook.payload.lastPrice.getOrElse(0)}`
                                       |`closePrice:${ordbook.payload.closePrice.getOrElse(0)}`
                                       |`limitUp:${ordbook.payload.limitUp.getOrElse(0)}`
                                       |`limitDown:${ordbook.payload.limitDown.getOrElse(0)}`
                                       |`tradeStatus:${ordbook.payload.tradeStatus}`
                                       |`bids:${ordbook.payload.bids.map(b => s"(${b.price} ${b.quantity})").mkString(",")}`
                                       |`asks:${ordbook.payload.asks.map(a => s"(${a.price} ${a.quantity})").mkString(",")}`
                                       |""".stripMargin
            case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`"
          }
        } yield reply
    }
  }

  private def activeOperations: F[String] = {
    for {
      ops <- dbAccess.getOpsByStatus(OperationStatus.Active)
      msg <- if (ops.isEmpty) {
        s"Список активных операций пуст".pure[F]
      } else {
        ops.map {
          op => s"""|`${op.id.getOrElse(-1)} ${op.figi} lots ${op.executedLots}
                    |take profit ${op.takeProfit} stop loss ${op.stopLoss}`
                    |""".stripMargin
        }.mkString("").pure[F]
      }
    } yield msg
  }

  private def setOperationsStatus(opStatus: OperationStatus): F[String] = {
    val status = opStatus match {
      case OperationStatus.Active => OperationStatus.Stop
      case OperationStatus.Stop => OperationStatus.Active
      case _ => OperationStatus.Stop
    }
    for {
      ops <- dbAccess.getOpsByStatus(status)
      results <- ops.traverse {
        op => op.id match {
          case None => s"Wrong operation id".pure[F]
          case Some(id) =>
            //dbAccess.updateOperationStatus(id, opStatus) >>
              //Sync[F].delay(log.info("Updated status for $id to $opStatus")) >>
              s"`$opStatus ${op.id} ${op.figi} ${op.stopLoss} ${op.takeProfit}`".pure[F]
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
        //case "/stopAllOperations" => setOperationsStatus(OperationStatus.Stop)
        //case "/startAllOperations" => setOperationsStatus(OperationStatus.Active)
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
       |/startAllOperations - Запустить все неактивные операции
       |/stopAllOperations - Отменить все активные операции
       |""".stripMargin.pure[F]
    /*
    * - /stocks
    * - /bonds
    */
  }
}
