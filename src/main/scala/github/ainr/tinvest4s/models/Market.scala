package github.ainr.tinvest4s.models

case class MarketInstrumentListResponse(trackingId: String,
                  status: String,
                  payload: MarketInstrumentList)

case class MarketInstrumentList(total: Int, instruments: List[MarketInstrument])

case class MarketInstrument(figi: String,
                            ticker: String,
                            isin: Option[String],
                            minPriceIncrement: Option[Double], /* Шаг цены */
                            lot: Int,
                            minQuantity: Option[Int], /* Минимальное число инструментов для покупки должно быть не меньше, чем размер лота х количество лотов */
                            currency: Option[String],
                            name: String,
                            `type`: String)