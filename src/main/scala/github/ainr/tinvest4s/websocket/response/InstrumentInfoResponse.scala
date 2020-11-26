package github.ainr.tinvest4s.websocket.response

case class InstrumentInfoResponse(event: String,
                                  time: String, // RFC3339Nano
                                  payload: InstrumentInfoPayload) extends TInvestWSResponse

case class InstrumentInfoPayload(trade_status: String,             // Статус торгов
                                  min_price_increment: Double,      // Шаг цены
                                  lot: Double,                      // Лот
                                  accrued_interest: Option[Double], // НКД. Возвращается только для бондов
                                  limit_up: Option[Double],         // Верхняя граница заявки. Возвращается только для RTS инструментов
                                  limit_down: Option[Double],       // Нижняя граница заявки. Возвращается только для RTS инструментов
                                  figi: String) extends TInvestWSResponse
