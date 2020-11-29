package github.ainr.tinvest4s.rest.client

import github.ainr.tinvest4s.models.{EmptyResponse, LimitOrderRequest, MarketInstrumentListResponse, MarketOrderRequest, OrderResponse, PortfolioResponse, TInvestError}

trait TInvestApi[F[_]] {
  def getPortfolio: F[Either[TInvestError, PortfolioResponse]]
  def limitOrder(figi: String, request: LimitOrderRequest): F[Either[TInvestError, OrderResponse]]
  def marketOrder(figi: String, request: MarketOrderRequest): F[Either[TInvestError, OrderResponse]]
  def cancelOrder(orderId: String): F[Either[TInvestError, EmptyResponse]]
  def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]]
  def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]]
  def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]]
  def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]]
}
