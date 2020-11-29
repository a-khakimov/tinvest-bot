package github.ainr.tinvest4s.websocket.client

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Timer}
import github.ainr.tinvest4s.websocket.request.{CandleRequest, InstrumentInfoRequest, OrderBookRequest}
import github.ainr.tinvest4s.websocket.response.{CandleResponse, InstrumentInfoResponse, OrderBookResponse, TInvestWSResponse}
import io.circe.syntax.EncoderOps
import cats.syntax.functor._
import io.circe.{Decoder}
import io.circe.generic.auto._
import org.http4s.client.jdkhttpclient.{WSConnectionHighLevel, WSFrame}


trait TInvestWSApi[F[_]] {
  def subscribeCandle(figi: String, interval: String): F[Unit] /* interval ("1min","2min","3min","5min","10min","15min","30min","hour","2hour","4hour","day","week","month") */
  def subscribeOrderbook(figi: String, depth: Int): F[Unit] /* 0 < DEPTH <= 20 */
  def subscribeInstrumentInfo(figi: String): F[Unit]

  def unsubscribeCandle(figi: String, interval: String): F[Unit]
  def unsubscribeOrderbook(figi: String, depth: Int): F[Unit]
  def unsubscribeInstrumentInfo(figi: String): F[Unit]

  def listen(): F[List[String]]
}


class TInvestWSApiHttp4s[F[_] : ConcurrentEffect: Timer: ContextShift : Concurrent]
(connection: WSConnectionHighLevel[F])
  extends TInvestWSApi[F] {

  override def subscribeCandle(figi: String, interval: String): F[Unit] = {
    connection.send {
        WSFrame.Text {
          CandleRequest("candle:subscribe", figi, interval).asJson.noSpaces
        }
      }
  }

  override def subscribeOrderbook(figi: String, depth: Int): F[Unit] = {
    connection.send {
        WSFrame.Text {
          OrderBookRequest("orderbook:subscribe", figi, depth).asJson.noSpaces
        }
      }
  }

  override def subscribeInstrumentInfo(figi: String): F[Unit] = {
    connection.send {
        WSFrame.Text {
          InstrumentInfoRequest("instrument_info:subscribe", figi).asJson.noSpaces
        }
      }
  }

  override def unsubscribeCandle(figi: String, interval: String): F[Unit] = {
    connection.send {
        WSFrame.Text {
          CandleRequest("candle:subscribe", figi, interval).asJson.noSpaces
        }
      }
  }

  override def unsubscribeOrderbook(figi: String, depth: Int): F[Unit] = {
    connection.send {
        WSFrame.Text {
          OrderBookRequest("orderbook:unsubscribe", figi, depth).asJson.noSpaces
        }
      }
  }

  override def unsubscribeInstrumentInfo(figi: String): F[Unit] = {
    connection.send {
        WSFrame.Text {
          InstrumentInfoRequest("instrument_info:unsubscribe", figi).asJson.noSpaces
        }
      }
  }

  def listen(): F[List[String]] = {
    connection
      .receiveStream
      .collect { case WSFrame.Text(str, _) => {
          //handler(str)
          str
        }
      }
      .evalTap(_ => connection.sendPing())
      .compile
      .toList
  }

  implicit val decodeEvent: Decoder[TInvestWSResponse] =
    List[Decoder[TInvestWSResponse]](
      Decoder[CandleResponse].widen,
      Decoder[OrderBookResponse].widen,
      Decoder[InstrumentInfoResponse].widen,
    ).reduceLeft(_ or _)
}
