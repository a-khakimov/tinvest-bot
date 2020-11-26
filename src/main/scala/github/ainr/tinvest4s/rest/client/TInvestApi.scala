package github.ainr.tinvest4s.rest.client

import github.ainr.tinvest4s.rest.orders.{LimitOrder, LimitOrderRequest}
import github.ainr.tinvest4s.rest.portfolio.Portfolio

trait TInvestApi[F[_]] {
  def getPortfolio(): F[Portfolio]
  def limitOrder(figi: String, request: LimitOrderRequest): F[LimitOrder]
}
