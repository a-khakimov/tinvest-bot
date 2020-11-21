package github.ainr.tinvest.client

import github.ainr.tinvest.orders.{LimitOrder, LimitOrderRequest}
import github.ainr.tinvest.portfolio.Portfolio

trait TInvestApi[F[_]] {
  def getPortfolio() : F[Portfolio]
  def limitOrder(figi: String, request: LimitOrderRequest) : F[LimitOrder]
}
