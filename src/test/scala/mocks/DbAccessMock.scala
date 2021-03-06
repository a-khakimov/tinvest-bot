package mocks

import java.sql.SQLException

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import github.ainr.db.DbAccess
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.domain.{BotOperation, Notification, OperationStatus}
import github.ainr.tinvest4s.websocket.response.CandlePayload

class DbAccessMock[F[_]: Sync] extends DbAccess[F] {

  override def insertCandle(candle: CandlePayload): F[Either[SQLException, Int]] = {
    ???
  }

  override def insertOperation(operation: BotOperation): F[Either[SQLException, Int]] = {
    Right(0).withLeft[SQLException].pure[F]
  }

  override def updateOperationStatus(id: Int, status: OperationStatus): F[Either[SQLException, Int]] = {
    Right(0).withLeft[SQLException].pure[F]
  }

  override def getOpsByStatus(operationStatus: OperationStatus): F[Either[SQLException, List[BotOperation]]] = {
    Right(
      List(BotOperation(
        Some(1), "SomeFIGI", 4500, 6000,
        OperationStatus.Active,
        "SomeOrderId",
        "SomeOrderStatus",
        "SomeOrderOperation",
        10, 10, 42
    ))).withLeft[SQLException].pure[F]
  }

  override def getNotifications: F[Either[SQLException, List[Notification]]] = {
    Right(
      List(
        Notification(42, "First notifications message"),
        Notification(73, "Second notifications message")
      )
    ).withLeft[SQLException].pure[F]
  }

  override def insertNotification(notification: Notification): F[Either[SQLException, Int]] = {
    Right(0).withLeft[SQLException].pure[F]
  }
}
