package github.ainr.domain

import github.ainr.domain.OperationStatus.OperationStatus

case class BotOperation(id: Option[Int] = None, figi: String, stopLoss: Double, takeProfit: Double,
                     operationStatus: OperationStatus, orderId: String, orderStatus: String, orderOperation: OperationStatus,
                     requestedLots: Int, executedLots: Int)

object OperationStatus {
  type OperationStatus = String
  val Active = "Active"
  val Stop = "Stop"
  val Completed = "Completed"
}

