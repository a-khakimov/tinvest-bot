package github.ainr.domain

import cats.effect.{Async, Sync, Timer}
import cats.implicits._
import github.ainr.db.DbAccess
import github.ainr.tinvest4s.websocket.client.TInvestWSHandler
import github.ainr.tinvest4s.websocket.response.{CandleResponse, InstrumentInfoResponse, OrderBookResponse, TInvestWSResponse}

class WSHandler[F[_]: Async : Timer](implicit notificationRepo: NotificationRepo[F],
                                     implicit val dbAccess: DbAccess[F])
  extends TInvestWSHandler[F] {

  override def handle(response: TInvestWSResponse): F[Unit] = {
    response match {
      case CandleResponse(_, _, payload) => {
        for {
          ops <- dbAccess.getOpsByStatus(OperationStatus.Active)
          _ <- ops filter(_.figi == payload.figi) traverse {
            op => {
              if (payload.c <= op.stopLoss) {
                notificationRepo.push(Notification(op.tgUserId, s"`StopLoss[${op.stopLoss}]: ${op.figi} - ${payload.c}`"))
              } else if (payload.c >= op.takeProfit)
                notificationRepo.push(Notification(op.tgUserId, s"`TakeProfit[${op.takeProfit}]: ${op.figi} - ${payload.c}`"))
              else
                notificationRepo.push(Notification(op.tgUserId, s"`Active: ${op.figi} - ${payload.c}`"))
            }
          }
        } yield ()
      }
      case OrderBookResponse(_, _, _) => ???
      case InstrumentInfoResponse(_, _, _) => ???
    }
  }
}