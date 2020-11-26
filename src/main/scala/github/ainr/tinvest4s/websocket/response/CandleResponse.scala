package github.ainr.tinvest4s.websocket.response

case class CandleResponse(event: String,
                          time: String, // RFC3339Nano
                          payload: CandlePayload) extends TInvestWSResponse

case class CandlePayload(o: Double, // Цена открытия
                          c: Double, // Цена закрытия
                          h: Double, // Наибольшая цена
                          l: Double, // Наименьшая цена
                          v: Double, // Объем торгов
                          time: String, // RFC3339
                          interval: String,
                          figi: String)
