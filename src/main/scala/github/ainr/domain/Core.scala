package github.ainr.domain

import cats.implicits._
import cats.effect.{Sync, Timer}

import scala.concurrent.duration.DurationInt
import org.slf4j.LoggerFactory
import fs2.Stream
import github.ainr.db.DbAccess
import github.ainr.telegram.TgBot
import github.ainr.tinvest4s.models.{LimitOrderRequest, MarketOrderRequest}
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi

class Core[F[_]: Sync : Timer](implicit dbAccess: DbAccess[F],
                               implicit val tinvestRestApi: TInvestApi[F],
                               implicit val tinvestWSApi: TInvestWSApi[F]) {

  private val log = LoggerFactory.getLogger("Core")

  def start: F[Unit] = {
    (Stream.emit(()) ++ Stream.fixedRate[F](5.second))
      .evalTap { _ =>
        for {
          _ <- Sync[F].delay(log.info("Some actions..."))
        } yield ()
      }.compile.drain
  }

  def portfolioMsg(): F[String] = {
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
        case Left(e) => s"Error: ${e}".pure[F]
      }
    } yield msg
  }

  def parseLimitOrderArgs(text: String): Option[(Int, Double, String)] = {
    val args = text.filter(c => c != '/' || c != ' ').split('.')
    args.size match {
      case 4 => {
        val figi = args(1)
        (args(2).toIntOption, args(3).toDoubleOption) match {
          case (lots, price) if lots.isDefined && price.isDefined => Some(lots.get, price.get, figi)
          case (_, _) => None
        }
      }
      case _ => None
    }
  }

  def parseMarketOrderArgs(text: String): Option[(Int, String)] = {
    val args = text.filter(c => c != '/' || c != ' ').split('.')
    args.size match {
      case 3 => {
        val figi = args(1)
        args(2).toIntOption match {
          case lots if lots.isDefined => Some(lots.get, figi)
          case _ => None
        }
      }
      case _ => None
    }
  }

  def doLimitOrder(operation: String, text: String): F[String] = {
    val parsedArgs = parseLimitOrderArgs(text)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args : (Int, Double, String)) => {
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
  }

  def doMarketOrder(operation: String, args: String): F[String] = {
    val parsedArgs = parseMarketOrderArgs(args)
    parsedArgs match {
      case None => s"Wrong command".pure[F]
      case Some(args : (Int, String)) => {
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
  }

  def doCancelOrder(args: String): F[String] = {
    val orderid = args.split('.').last
    for {
      result <- tinvestRestApi.cancelOrder(orderid)
      reply = result match {
        case Right(r) => s"`Success`"
        case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`"
      }
    } yield reply
  }

  def parseOrderbookArgs(text: String): Option[(Int, String)] = {
    val args = text.filter(c => c != '/' || c != ' ').split('.')
    args.size match {
      case 3 => {
        val figi = args(1)
        args(2).toIntOption match {
          case depth if depth.isDefined => Some(depth.get, figi)
          case _ => None
        }
      }
      case _ => None
    }
  }

  def doOrderbook(args: String): F[String] = {
    val parsedArgs = parseMarketOrderArgs(args)
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
                                       |`bids:${ordbook.payload.bids.map(b => s"(${b.price} ${b.quantity})").mkString(",")}`
                                       |`asks:${ordbook.payload.asks.map(a => s"(${a.price} ${a.quantity})").mkString(",")}`
                                       |""".stripMargin
            case Left(e) => s"`${e.status}: ${e.payload.message.getOrElse("Unknown")}`"
          }
        } yield reply
    }
  }

  def handleTgMessage(text: String): F[String] = {
    for {
      reply <- text match {
        case "/help" => helpMsg()
        case "/portfolio" => portfolioMsg()
        case "/stocks" => marketInstrumentMsg("stocks") /* TODO: слишком длинное сообщение */
        case "/bonds" => marketInstrumentMsg("bonds") /* TODO: слишком длинное сообщение */
        case "/etfs" => marketInstrumentMsg("etfs")
        case "/currencies" => marketInstrumentMsg("currencies")
        case args if args.startsWith("/orderbook") => doOrderbook(args)
        case args if args.startsWith("/cancelOrder") => doCancelOrder(args)
        case args if args.startsWith("/limitOrderBuy") => doLimitOrder("Buy", args)
        case args if args.startsWith("/limitOrderSell") => doLimitOrder("Sell", args)
        case args if args.startsWith("/marketOrderBuy") => doMarketOrder("Buy", args)
        case args if args.startsWith("/marketOrderSell") => doMarketOrder("Sell", args)
        case _ => helpMsg()
      }
    } yield reply
  }

  def tinvestWSHandler(x: String): F[Unit] = {
    //tgBot.send(174861972, s"`$x`")
    for {
      _ <- Sync[F].delay(log.info(s"`$x`"))
    } yield ()
  }

  def helpMsg(): F[String] = {
    s"""
       |Список доступных команд:
       |/help
       |/portfolio - Портфель
       |/etfs - Получение списка ETF
       |/currencies - Получение списка валютных пар
       |/orderbook.figi.depth - Получение стакана по FIGI
       |/cancelOrder.orderId - Отмена заявки по OrderId
       |/limitOrderBuy.figi.lots.price - Создание лимитной заявки на покупку
       |/limitOrderSell.figi.lots.price - Создание лимитной заявки на продажу
       |/marketOrderBuy.figi.lots - Создание рыночной заявки на покупку
       |/marketOrderSell.figi.lots - Создание рыночной заявки на продажу
       |""".stripMargin.pure[F]
    /*
    * - /stocks
    * - /bonds
    */
  }
}
