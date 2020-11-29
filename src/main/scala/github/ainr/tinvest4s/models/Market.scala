package github.ainr.tinvest4s.models

case class MarketInstrumentListResponse(trackingId: String, status: String, payload: MarketInstrumentList)

case class MarketInstrumentList(total: Int, instruments: List[MarketInstrument])

case class MarketInstrument(figi: String,
                            ticker: String,
                            isin: Option[String],
                            minPriceIncrement: Option[Double], /* Шаг цены */
                            lot: Int,
                            minQuantity: Option[Int], /* Минимальное число инструментов для покупки должно
                                                         быть не меньше, чем размер лота х количество лотов */
                            currency: Option[String],
                            name: String,
                            `type`: String)

case class OrderbookResponse(trackingId: String, status: String, payload: Orderbook)
case class Orderbook(figi: String,
                     depth: Int,
                     bids: List[OrderResponse],
                     asks: List[OrderResponse],
                     tradeStatus: String,
                     minPriceIncrement: Double, // Шаг цены
                     faceValue: Option[Double], // Номинал для облигаций
                     lastPrice: Option[Double],
                     closePrice: Option[Double],
                     limitUp: Option[Double],   // Верхняя граница цены
                     limitDown: Option[Double]) // Нижняя граница цены
case class OrderResponse(price: Double, quantity: Int)