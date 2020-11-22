package github.ainr.tinvest.portfolio


case class Portfolio(trackingId: String, payload: Payload, status: String)
case class Payload(positions: Seq[Position])
case class Position(
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
case class ExpectedYield(currency: String, value: Double)
case class AveragePositionPrice(currency: String, value: Double)
case class AveragePositionPriceNoNkd(currency: String, value: Double)
