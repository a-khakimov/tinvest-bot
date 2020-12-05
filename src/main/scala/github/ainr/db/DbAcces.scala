package github.ainr.db

import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.domain.{BotOperation, Notification}
import github.ainr.tinvest4s.websocket.response.CandlePayload

trait DbAccess[F[_]] {
    def insertCandle(candle: CandlePayload): F[Int]
    def insertOperation(operation: BotOperation): F[Int]
    def updateOperationStatus(id: Int, status: OperationStatus): F[Int]
    def getOpsByStatus(operationStatus: OperationStatus): F[List[BotOperation]]
    def getNotifications: F[List[Notification]]
    def insertNotification(notification: Notification): F[Int]
}
