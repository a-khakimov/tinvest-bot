package github.ainr.tinvest4s.websocket.response


case class OrderBookResponse(event: String,
                             time: String, // RFC3339Nano
                             payload: OrderBookPayload) extends TInvestWSResponse

case class OrderBookPayload(depth: Int,
                             bids: Seq[(Double, Double)], // Массив [Цена, количество] предложений цены
                             asks: Seq[(Double, Double)], // Массив [Цена, количество] запросов цены
                             figi: String)
