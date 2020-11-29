package github.ainr.tinvest4s.models

case class MarketOrderRequest(lots: Int, operation: String)
case class LimitOrderRequest(lots: Int, operation: String, price: Double)

case class OrdersResponse(trackingId: String, status: String, payload: PlacedLimitOrder)

case class PlacedLimitOrder(orderId: String,
                            operation: String, /* Buy, Sell */
                            status: String, /* Статус заявки: New, PartiallyFill, Fill,
                                                              Cancelled, Replaced, PendingCancel,
                                                              Rejected, PendingReplace, PendingNew */
                            rejectReason: Option[String],
                            message: Option[String],
                            requestedLots: Int,
                            executedLots: Int,
                            commission: Option[MoneyAmount])

case class Order(orderId: String,
                 figi: String,
                 operation: String, /* Buy, Sell */
                 status: String, /* Статус заявки: New, PartiallyFill, Fill,
                                                   Cancelled, Replaced, PendingCancel,
                                                   Rejected, PendingReplace, PendingNew */
                 requestedLots: Int,
                 executedLots: Int,
                 price: Double)

case class MoneyAmount(currency: String, /* RUB, USD, EUR, GBP, HKD, CHF, JPY, CNY, TRY */
                       value: Double)
