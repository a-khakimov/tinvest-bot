package github.ainr.db

import java.sql.SQLException

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.domain.{BotOperation, Notification}
import github.ainr.tinvest4s.websocket.response.CandlePayload

/**
 * https://www.scala-exercises.org/doobie/error_handling
 */

class DbAccessImpl[F[_]: Bracket[*[_], Throwable]](transactor: Transactor[F]) extends DbAccess[F] {

  def insertCandle(candle: CandlePayload): F[Either[SQLException, Int]] = {
    Queries.insertCandle(candle).run.transact(transactor).attemptSql
  }

  def insertOperation(operation: BotOperation): F[Either[SQLException, Int]] = {
    Queries.insertOperation(operation).run.transact(transactor).attemptSql
  }

  def updateOperationStatus(id: Int, status: OperationStatus): F[Either[SQLException, Int]] = {
    Queries.updateOperationStatus(id, status).run.transact(transactor).attemptSql
  }

  def getOpsByStatus(operationStatus: OperationStatus): F[Either[SQLException, List[BotOperation]]] = {
    Queries.getOperationsByStatus(operationStatus).transact(transactor).attemptSql
  }

  def getNotifications: F[Either[SQLException, List[Notification]]] = {
    Queries.getNotifications.transact(transactor).attemptSql
  }

  def insertNotification(notification: Notification): F[Either[SQLException, Int]] = {
    Queries.insertNotification(notification).run.transact(transactor).attemptSql
  }
}
