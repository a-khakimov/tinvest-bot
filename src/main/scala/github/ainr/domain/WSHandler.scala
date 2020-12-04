package github.ainr.domain

import cats.effect.{Async, Sync, Timer}
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
      ops <- dbAccess.getOpsByStatus(OperationStatus.Active)
      _ <- ops.filter(_.figi == candle.figi).traverse {
        op => {
          candle.c match {
            case closePrice if closePrice <= op.stopLoss => stopLoss(op, candle)
            case closePrice if closePrice >= op.takeProfit => takeProfit(op, candle)
            case _ => justSaveCandle(candle)
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
      _ <- Sync[F].delay(log.info(s"Save candle $candle"))
      _ <- dbAccess.insertCandle(candle)
    } yield ()
  }

  private def stopLoss(op: BotOperation, candle: CandlePayload): F[Unit] = {
    for {
      _ <- Sync[F].delay(log.info(s"Running stop loss operation for $candle"))
      _ <- dbAccess.updateOperationStatus(op.id.get, OperationStatus.Running)
      _ <- notificationRepo.push(
        Notification(
          op.tgUserId,
          s"""|Running **stop loss** operation for ${op.figi}
              |with close price ${candle.c} and stop loss value ${op.stopLoss}
              |""".stripMargin
        )
      )
      _ <- dbAccess.insertCandle(candle)
    } yield ()
  }

  private def takeProfit(op: BotOperation, candle: CandlePayload): F[Unit] = {
    for {
      _ <- Sync[F].delay(log.info(s"Running take profit operation for $candle"))
      _ <- dbAccess.updateOperationStatus(op.id.get, OperationStatus.Running)
      _ <- notificationRepo.push(
        Notification(
          op.tgUserId,
          s"""|Running **take profit** operation for ${op.figi}
              |with close price ${candle.c} and take profit value ${op.takeProfit}
              |""".stripMargin
        )
      )
      _ <- dbAccess.insertCandle(candle)
    } yield ()
  }
}
