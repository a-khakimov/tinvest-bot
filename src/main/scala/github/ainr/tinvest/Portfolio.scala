package github.ainr.tinvest



case class Portfolio(trackingId: String, payload: Payload, status: String)
case class Payload(positions: Seq[Position])
case class Position(
  figi: String,
  ticker: Option[String],
  isin: Option[String],
  instrumentType: String,
  balance: Double,
  blocked: Option[Double],
  lots: Int,
  name: String
)
case class ExpectedYield()
