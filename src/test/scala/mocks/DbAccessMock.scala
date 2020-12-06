package mocks

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import github.ainr.db.DbAccess
import github.ainr.domain.{BotOperation, Notification, OperationStatus}
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.tinvest4s.websocket.response.CandlePayload

class DbAccessMock[F[_]: Sync] extends DbAccess[F] {
  override def insertCandle(candle: CandlePayload): F[Int] = ???

  override def insertOperation(operation: BotOperation): F[Int] = 1.pure[F]

  override def updateOperationStatus(id: Int, status: OperationStatus): F[Int] = ???

  override def getOpsByStatus(operationStatus: OperationStatus): F[List[BotOperation]] = {
    List(BotOperation(
      Some(1),
      "SomeFIGI",
      4500,
      6000,
      OperationStatus.Active,
      "SomeOrderId",
      "SomeOrderStatus",
      "SomeOrderOperation",
      10,
      10,
      42
    )).pure[F]
  }

  override def getNotifications: F[List[Notification]] = ???

  override def insertNotification(notification: Notification): F[Int] = ???
}
