package mocks

import cats.effect.Sync
import cats.implicits._
import github.ainr.tinvest4s.models.CandleResolution.CandleResolution
import github.ainr.tinvest4s.models.FIGI.FIGI
import github.ainr.tinvest4s.models._
import github.ainr.tinvest4s.rest.client.TInvestApi


class TInvestApiBadMock[F[_]: Sync] extends TInvestApi[F] {

  override def getPortfolio: F[Either[TInvestError, PortfolioResponse]] = {
    Left(TInvestError("Id", "getPortfolio error status", Payload(None, None)))
      .withRight[PortfolioResponse].pure[F]
  }

  override def limitOrder(figi: FIGI, request: LimitOrderRequest): F[Either[TInvestError, OrdersResponse]] = {
    Left(TInvestError("Id", "limitOrder error status", Payload(None, None)))
      .withRight[OrdersResponse].pure[F]
  }

  override def marketOrder(figi: FIGI, request: MarketOrderRequest): F[Either[TInvestError, OrdersResponse]] = {
    Left(TInvestError("Id", "marketOrder error status", Payload(None, None)))
      .withRight[OrdersResponse].pure[F]
  }

  override def cancelOrder(orderId: String): F[Either[TInvestError, EmptyResponse]] = {
    Left(TInvestError("Id", "cancelOrder error status", Payload(None, None)))
      .withRight[EmptyResponse].pure[F]
  }

  override def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Left(TInvestError("Id", "stocks error status", Payload(None, None)))
      .withRight[MarketInstrumentListResponse].pure[F]
  }

  override def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Left(TInvestError("Id", "bonds error status", Payload(None, None)))
      .withRight[MarketInstrumentListResponse].pure[F]
  }

  override def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Left(TInvestError("Id", "etfs error status", Payload(None, None)))
      .withRight[MarketInstrumentListResponse].pure[F]
  }

  override def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Left(TInvestError("Id", "currencies error status", Payload(None, None)))
      .withRight[MarketInstrumentListResponse].pure[F]
  }

  override def orderbook(figi: FIGI, depth: Int): F[Either[TInvestError, OrderbookResponse]] = {
    Left(TInvestError("Id", "orderbook error status", Payload(None, None)))
      .withRight[OrderbookResponse].pure[F]
  }

  override def candles(figi: FIGI, interval: CandleResolution, from: String, to: String): F[Either[TInvestError, CandlesResponse]] = {
    Left(TInvestError("Id", "candles error status", Payload(None, None)))
      .withRight[CandlesResponse].pure[F]
  }
}
