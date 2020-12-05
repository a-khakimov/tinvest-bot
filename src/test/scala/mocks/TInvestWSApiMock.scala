package mocks

import github.ainr.tinvest4s.models.CandleResolution.CandleResolution
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import github.ainr.tinvest4s.websocket.response.TInvestWSResponse

class TInvestWSApiMock[F[_]] extends TInvestWSApi[F] {
  override def subscribeCandle(figi: String, interval: CandleResolution): F[Unit] = ???

  override def subscribeOrderbook(figi: String, depth: Int): F[Unit] = ???

  override def subscribeInstrumentInfo(figi: String): F[Unit] = ???

  override def unsubscribeCandle(figi: String, interval: CandleResolution): F[Unit] = ???

  override def unsubscribeOrderbook(figi: String, depth: Int): F[Unit] = ???

  override def unsubscribeInstrumentInfo(figi: String): F[Unit] = ???

  override def listen(): F[List[TInvestWSResponse]] = ???
}
