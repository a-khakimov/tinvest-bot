package github.ainr.tinvest.portfolio


case class Portfolio(trackingId: String, payload: Payload, status: String)
sealed case class Payload(positions: Seq[Position])
sealed case class Position(
  figi: String,
  ticker: Option[String],
  isin: Option[String],
  instrumentType: String,
  balance: Double,
  blocked: Option[Double],
  expectedYield: Option[ExpectedYield],
  lots: Int,
  averagePositionPrice: Option[AveragePositionPrice],
  averagePositionPriceNoNkd: Option[AveragePositionPriceNoNkd],
  name: String
)
sealed case class ExpectedYield(currency: String, value: Double)
sealed case class AveragePositionPrice(currency: String, value: Double)
sealed case class AveragePositionPriceNoNkd(currency: String, value: Double)
