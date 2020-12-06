package mocks

import cats.effect.Sync
import github.ainr.tinvest4s.models.CandleResolution.CandleResolution
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import github.ainr.tinvest4s.websocket.response.TInvestWSResponse

class TInvestWSApiMock[F[_]: Sync] extends TInvestWSApi[F] {
  override def subscribeCandle(figi: String, interval: CandleResolution): F[Unit] = {
    Sync[F].delay(println(s"subscribeCandle: $figi"))
  }

  override def subscribeOrderbook(figi: String, depth: Int): F[Unit] = {
    Sync[F].delay(println(s"subscribeOrderbook: $figi"))
  }

  override def subscribeInstrumentInfo(figi: String): F[Unit] = {
    Sync[F].delay(println(s"subscribeInstrumentInfo: $figi"))
  }

  override def unsubscribeCandle(figi: String, interval: CandleResolution): F[Unit] = {
    Sync[F].delay(println(s"unsubscribeCandle: $figi"))
  }

  override def unsubscribeOrderbook(figi: String, depth: Int): F[Unit] = {
    Sync[F].delay(println(s"unsubscribeOrderbook: $figi"))
  }

  override def unsubscribeInstrumentInfo(figi: String): F[Unit] = {
    Sync[F].delay(println(s"unsubscribeInstrumentInfo: $figi"))
  }

  override def listen(): F[List[TInvestWSResponse]] = ???
}
