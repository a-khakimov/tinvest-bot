package github.ainr.tinvest4s.rest.client

import github.ainr.tinvest4s.models.TInvestError
import github.ainr.tinvest4s.models.orders.{LimitOrderRequest, MarketOrderRequest, OrderResponse}
import github.ainr.tinvest4s.models.portfolio.Portfolio

trait TInvestApi[F[_]] {
  def getPortfolio: F[Either[String, Portfolio]]
  def limitOrder(figi: String, request: LimitOrderRequest): F[Either[String, OrderResponse]]
  def marketOrder(figi: String, request: MarketOrderRequest): F[Either[String, OrderResponse]]
}
