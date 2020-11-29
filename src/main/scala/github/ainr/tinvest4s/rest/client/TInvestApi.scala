package github.ainr.tinvest4s.rest.client

import github.ainr.tinvest4s.models.{EmptyResponse, LimitOrderRequest, MarketInstrumentListResponse, MarketOrderRequest, OrdersResponse, OrderbookResponse, PortfolioResponse, TInvestError}

trait TInvestApi[F[_]] {

  /* Получение портфеля клиента */
  def getPortfolio: F[Either[TInvestError, PortfolioResponse]]

  /* Создание лимитной заявки */
  def limitOrder(figi: String, request: LimitOrderRequest): F[Either[TInvestError, OrdersResponse]]

  /* Создание рыночной заявки */
  def marketOrder(figi: String, request: MarketOrderRequest): F[Either[TInvestError, OrdersResponse]]

  /* Отмена заявки */
  def cancelOrder(orderId: String): F[Either[TInvestError, EmptyResponse]]

  /* Получение списка акций */
  def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]]

  /* Получение списка облигаций */
  def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]]

  /* Получение списка ETF */
  def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]]

  /* Получение списка валютных пар */
  def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]]

  /* Получение стакана по FIGI */
  def orderbook(figi: String, depth: Int): F[Either[TInvestError, OrderbookResponse]]
}
