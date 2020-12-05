package mocks

import cats.effect.Sync
import cats.implicits._
import github.ainr.tinvest4s.models.CandleResolution.CandleResolution
import github.ainr.tinvest4s.models._
import github.ainr.tinvest4s.rest.client.TInvestApi


class TInvestApiMock[F[_]: Sync] extends TInvestApi[F] {
  override def getPortfolio: F[Either[TInvestError, PortfolioResponse]] = {
    Right(
      PortfolioResponse("trackingId", Portfolio(
        Seq(PortfolioPosition(
            "figi",
            Some("ticker"),
            Some("isin"),
            "instrumentType",
            0.2,
            Some(0.1),
            Some(MoneyAmount(Currency.USD, 1)),
            10,
            Some(MoneyAmount(Currency.USD, 2)),
            Some(MoneyAmount(Currency.USD, 3)),
            "name"
          )
        )
      ), "status")
    ).withLeft[TInvestError].pure[F]
  }

  override def limitOrder(figi: String, request: LimitOrderRequest): F[Either[TInvestError, OrdersResponse]] = ???

  override def marketOrder(figi: String, request: MarketOrderRequest): F[Either[TInvestError, OrdersResponse]] = ???

  override def cancelOrder(orderId: String): F[Either[TInvestError, EmptyResponse]] = ???

  override def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]] = ???

  override def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]] = ???

  override def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]] = ???

  override def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]] = ???

  override def orderbook(figi: String, depth: Int): F[Either[TInvestError, OrderbookResponse]] = ???

  override def candles(figi: String, interval: CandleResolution, from: String, to: String): F[Either[TInvestError, CandlesResponse]] = ???
}
