package mocks

import github.ainr.db.DbAccess
import github.ainr.domain.{BotOperation, Notification}
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.tinvest4s.websocket.response.CandlePayload

class DbAccessMock[F[_]] extends DbAccess[F] {
  override def insertCandle(candle: CandlePayload): F[Int] = ???

  override def insertOperation(operation: BotOperation): F[Int] = ???

  override def updateOperationStatus(id: Int, status: OperationStatus): F[Int] = ???

  override def getOpsByStatus(operationStatus: OperationStatus): F[List[BotOperation]] = ???

  override def getNotifications: F[List[Notification]] = ???

  override def insertNotification(notification: Notification): F[Int] = ???
}
