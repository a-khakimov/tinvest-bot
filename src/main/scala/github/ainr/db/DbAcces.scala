package github.ainr.db

import java.sql.SQLException

import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.domain.{BotOperation, Notification}
import github.ainr.tinvest4s.websocket.response.CandlePayload

trait DbAccess[F[_]] {
    def insertCandle(candle: CandlePayload): F[Either[SQLException, Int]]
    def insertOperation(operation: BotOperation): F[Either[SQLException, Int]]
    def updateOperationStatus(id: Int, status: OperationStatus): F[Either[SQLException, Int]]
    def getOpsByStatus(operationStatus: OperationStatus): F[Either[SQLException, List[BotOperation]]]
    def getNotifications: F[Either[SQLException, List[Notification]]]
    def insertNotification(notification: Notification): F[Either[SQLException, Int]]
}
