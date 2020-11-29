package github.ainr.tinvest4s.rest.client

import github.ainr.tinvest4s.models.{MarketInstrumentListResponse, TInvestError}
import github.ainr.tinvest4s.models.orders.{LimitOrderRequest, MarketOrderRequest, OrderResponse}
import github.ainr.tinvest4s.models.portfolio.Portfolio

trait TInvestApi[F[_]] {
  def getPortfolio: F[Either[TInvestError, Portfolio]]
  def limitOrder(figi: String, request: LimitOrderRequest): F[Either[TInvestError, OrderResponse]]
  def marketOrder(figi: String, request: MarketOrderRequest): F[Either[TInvestError, OrderResponse]]
  def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]]
  def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]]
  def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]]
  def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]]
}
