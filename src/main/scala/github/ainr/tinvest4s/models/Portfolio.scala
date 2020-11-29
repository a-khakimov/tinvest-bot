package github.ainr.tinvest4s.models

case class PortfolioResponse(trackingId: String, payload: Portfolio, status: String)
case class Portfolio(positions: Seq[PortfolioPosition])
case class PortfolioPosition(
  figi: String,
  ticker: Option[String],
  isin: Option[String],
  instrumentType: String,
  balance: Double,
  blocked: Option[Double],
  expectedYield: Option[MoneyAmount],
  lots: Int,
  averagePositionPrice: Option[MoneyAmount],
  averagePositionPriceNoNkd: Option[MoneyAmount],
  name: String
)
