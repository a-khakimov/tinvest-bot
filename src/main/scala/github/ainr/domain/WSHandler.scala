package github.ainr.domain

import cats.effect.{Sync, Timer}
import cats.implicits._
import github.ainr.db.DbAccess
import github.ainr.tinvest4s.websocket.client.TInvestWSHandler
import github.ainr.tinvest4s.websocket.response._
import org.slf4j.LoggerFactory

class WSHandler[F[_]: Sync : Timer](implicit notificationRepo: NotificationRepo[F],
                                     implicit val dbAccess: DbAccess[F])
  extends TInvestWSHandler[F] {

  private val log = LoggerFactory.getLogger("TInvestWSHandler")

  override def handle(response: TInvestWSResponse): F[Unit] = {
    response match {
      case CandleResponse(_, _, candle) => handleCandle(candle)
      case OrderBookResponse(_, _, orderBook) => handleOrderBook(orderBook)
      case InstrumentInfoResponse(_, _, instrumentInfo) => handleInstrumentInfo(instrumentInfo)
    }
  }

  private def handleCandle(candle: CandlePayload): F[Unit] = {
    for {
      opsE <- dbAccess.getOpsByStatus(OperationStatus.Active)
      _ <- opsE match {
        case Left(e) => Sync[F].delay(log.info(s"$e"))
        case Right(ops) => {
          ops.filter(_.figi == candle.figi).traverse {
            op => {
              candle.c match {
                case closePrice if closePrice <= op.stopLoss => stopLoss(op, candle)
                case closePrice if closePrice >= op.takeProfit => takeProfit(op, candle)
                case _ => justSaveCandle(candle)
              }
            }
          }
        }
      }
    } yield ()
  }

  private def handleOrderBook(orderBook: OrderBookPayload): F[Unit] = {
    for {
      _ <- Sync[F].delay(log.info(s"handleOrderBook implementation is missing"))
    } yield ()
  }

  private def handleInstrumentInfo(instrumentInfo: InstrumentInfoPayload): F[Unit] = {
    for {
      _ <- Sync[F].delay(log.info(s"handleInstrumentInfo implementation is missing"))
    } yield ()
  }

  private def justSaveCandle(candle: CandlePayload): F[Unit] = {
    for {
      retE <- dbAccess.insertCandle(candle)
      _ <- retE match {
        case Left(e) => Sync[F].delay(log.error(s"Save candle $e"))
        case Right(_) => Sync[F].delay(log.info(s"Save candle $candle"))
      }
    } yield ()
  }

  private def runOperation(id: Int, user: Long, msg: String, candle: CandlePayload) = {
    for {
      retE <- dbAccess.updateOperationStatus(id, OperationStatus.Running)
      _ <- retE match {
        case Left(e) => Sync[F].delay(log.error(s"updateOperationStatus: $e"))
        case Right(_) => {
          for {
            retE <- dbAccess.insertCandle(candle)
            _ <- retE match {
              case Left(e) => Sync[F].delay(log.error(s"insertCandle: $e"))
              case Right(_) => {
                for {
                  _ <- notificationRepo.push(Notification(user, msg))
                } yield ()
              }
            }
          } yield ()
        }
      }
    } yield ()
  }

  def stopLoss(op: BotOperation, candle: CandlePayload): F[Unit] = {
    op.id match {
      case None => Sync[F].delay(log.error(s"Unknown operation $op"))
      case Some(id) => {
        val msg = s"[$id] Продажа акций ${op.figi} по событию StopLoss(${op.stopLoss}) по цене ${candle.c}"
        runOperation(id, op.tgUserId, msg, candle)
      }
    }
  }

  def takeProfit(op: BotOperation, candle: CandlePayload): F[Unit] = {
    op.id match {
      case None => Sync[F].delay(log.error(s"Unknown operation $op"))
      case Some(id) => {
        val msg = s"[$id] Продажа акций ${op.figi} по событию TakeProfit(${op.takeProfit}) по цене ${candle.c}"
        runOperation(id, op.tgUserId, msg, candle)
      }
    }
  }
}
