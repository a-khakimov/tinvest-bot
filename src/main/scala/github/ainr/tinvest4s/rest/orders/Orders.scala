package github.ainr.tinvest4s.rest.orders

case class Orders()


case class LimitOrderRequest(lots: Int, operation: String, price: Double)

case class LimitOrder(trackingId: String, status: String, payload: Payload)
sealed case class Payload(
                    orderId: String,
                    operation: String,
                    status: String,
                    rejectReason: Option[String],
                    message: Option[String],
                    requestedLots: Int,
                    executedLots: Int,
                    commission: Option[Commission]
                  )
sealed case class Commission(currency: String, value: Int)