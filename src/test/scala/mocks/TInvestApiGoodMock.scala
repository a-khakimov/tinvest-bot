package mocks

import cats.effect.Sync
import cats.implicits._
import github.ainr.tinvest4s.models.CandleResolution.CandleResolution
import github.ainr.tinvest4s.models.FIGI.FIGI
import github.ainr.tinvest4s.models._
import github.ainr.tinvest4s.rest.client.TInvestApi


class TInvestApiGoodMock[F[_]: Sync] extends TInvestApi[F] {

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

  override def limitOrder(figi: FIGI, request: LimitOrderRequest): F[Either[TInvestError, OrdersResponse]] = {
    Right(
      OrdersResponse("trackingId", "Buy",
        PlacedOrder(
          "SomeOrderId",
          "Buy",
          "GoodOrderStatus",
          Some("rejectReason"),
          Some("message"),
          request.lots,
          request.lots,
          None
        )
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def marketOrder(figi: FIGI, request: MarketOrderRequest): F[Either[TInvestError, OrdersResponse]] = {
    Right(
      OrdersResponse("trackingId", "Buy",
        PlacedOrder(
          "SomeOrderId",
          "Buy",
          "GoodOrderStatus",
          Some("rejectReason"),
          Some("message"),
          request.lots,
          request.lots,
          None
        )
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def cancelOrder(orderId: String): F[Either[TInvestError, EmptyResponse]] = {
    ???
  }

  override def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Right(
      MarketInstrumentListResponse("trackingId", "SomeStatus",
        MarketInstrumentList(1, List(MarketInstrument(
          "FIGI",
          "ticker",
          Some("isin"),
          None,
          10,
          None,
          Some("currency"),
          "Some stock",
          "type"
        )))
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Right(
      MarketInstrumentListResponse("trackingId", "SomeStatus",
        MarketInstrumentList(1, List(MarketInstrument(
          "FIGI",
          "ticker",
          Some("isin"),
          None,
          10,
          None,
          Some("currency"),
          "Some bond",
          "type"
        )))
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Right(
      MarketInstrumentListResponse("trackingId", "SomeStatus",
        MarketInstrumentList(1, List(MarketInstrument(
          "FIGI",
          "ticker",
          Some("isin"),
          None,
          10,
          None,
          Some("currency"),
          "Some etf",
          "type"
        )))
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    Right(
      MarketInstrumentListResponse("trackingId", "SomeStatus",
        MarketInstrumentList(1, List(MarketInstrument(
          "FIGI",
          "ticker",
          Some("isin"),
          None,
          10,
          None,
          Some("currency"),
          "Some currency",
          "type"
        )))
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def orderbook(figi: FIGI, depth: Int): F[Either[TInvestError, OrderbookResponse]] = {
    Right(
      OrderbookResponse("trackingId", "SomeStatus",
        Orderbook(
          figi,
          depth,
          List(OrderResponse(0, 0), OrderResponse(1, 1)),
          List(OrderResponse(0, 0), OrderResponse(1, 1)),
          TradeStatus.NormalTrading,
          10,
          Some(5000),
          Some(5000),
          Some(5000),
          Some(5000),
          Some(5000)
        )
      )
    ).withLeft[TInvestError].pure[F]
  }

  override def candles(figi: FIGI, interval: CandleResolution, from: String, to: String): F[Either[TInvestError, CandlesResponse]] = {
    ???
  }
}
