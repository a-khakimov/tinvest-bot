package mocks

import java.sql.SQLException

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import github.ainr.db.DbAccess
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.domain.{BotOperation, Notification, OperationStatus}
import github.ainr.tinvest4s.websocket.response.CandlePayload

class DbAccessBadMock[F[_]: Sync] extends DbAccess[F] {

  override def insertCandle(candle: CandlePayload): F[Either[SQLException, Int]] = {
    ???
  }

  override def insertOperation(operation: BotOperation): F[Either[SQLException, Int]] = {
    Left(new SQLException("SomeReason", "SomeState", 42)).withRight[Int].pure[F]
  }

  override def updateOperationStatus(id: Int, status: OperationStatus): F[Either[SQLException, Int]] = {
    Left(new SQLException("SomeReason", "SomeState", 42)).withRight[Int].pure[F]
  }

  override def getOpsByStatus(operationStatus: OperationStatus): F[Either[SQLException, List[BotOperation]]] = {
    Left(new SQLException("SomeReason", "SomeState", 42)).withRight[List[BotOperation]].pure[F]
  }

  override def getNotifications: F[Either[SQLException, List[Notification]]] = {
    Left(new SQLException("SomeReason", "SomeState", 42)).withRight[List[Notification]].pure[F]
  }

  override def insertNotification(notification: Notification): F[Either[SQLException, Int]] = {
    Left(new SQLException("SomeReason", "SomeState", 42)).withRight[Int].pure[F]
  }
}
