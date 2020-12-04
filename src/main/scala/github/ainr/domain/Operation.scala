package github.ainr.domain

import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.tinvest4s.models.Operation.Operation

case class BotOperation(id: Option[Int] = None,
                        figi: String,
                        stopLoss: Double,
                        takeProfit: Double,
                        operationStatus: OperationStatus,
                        orderId: String,
                        orderStatus: String,
                        orderOperation: Operation,
                        requestedLots: Int,
                        executedLots: Int,
                        tgUserId: Long)

object OperationStatus {
  type OperationStatus = String
  val Stop = "Stop"           /* Операция остановлена             */
  val Active = "Active"       /* Операция активна                 */
  val Running = "Running"     /* Операция отмечена для выполнения */
  val Completed = "Completed" /* Операция выполнена               */
}

