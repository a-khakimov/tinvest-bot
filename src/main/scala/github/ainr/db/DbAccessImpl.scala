package github.ainr.db

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import github.ainr.domain.{BotOperation, Notification}
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.tinvest4s.websocket.response.CandlePayload


class DbAccessImpl[F[_]: Bracket[*[_], Throwable]](transactor: Transactor[F]) extends DbAccess[F] {

  def insertCandle(candle: CandlePayload): F[Int] = {
    Queries.insertCandle(candle).run.transact(transactor)
  }

  def insertOperation(operation: BotOperation): F[Int] = {
    Queries.insertOperation(operation).run.transact(transactor)
  }

  def updateOperationStatus(id: Int, status: OperationStatus): F[Int] = {
    Queries.updateOperationStatus(id, status).run.transact(transactor)
  }

  def getOpsByStatus(operationStatus: OperationStatus): F[List[BotOperation]] = {
    Queries.getOperationsByStatus(operationStatus).transact(transactor)
  }

  def getNotifications: F[List[Notification]] = {
    Queries.getNotifications.transact(transactor)
  }

  def insertNotification(notification: Notification): F[Int] = {
    Queries.insertNotification(notification).run.transact(transactor)
  }
}
