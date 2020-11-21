package github.ainr.tinvest.client

import github.ainr.tinvest.Portfolio

trait TInvestApi[F[_]] {
  def getPortfolio() : F[Portfolio]
}
