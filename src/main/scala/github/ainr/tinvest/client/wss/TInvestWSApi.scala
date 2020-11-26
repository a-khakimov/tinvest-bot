package github.ainr.tinvest.client.wss

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import io.circe.{Decoder, Encoder, Error, Json, ParsingFailure, parser}
import org.http4s.client.jdkhttpclient.{WSConnectionHighLevel, WSFrame}
import io.circe.generic.auto.exportDecoder
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait TInvestWSRequest
case class CandleRequest(event: String, figi: String, interval: String, request_id: Option[String] = None) extends TInvestWSRequest
case class OrderBookRequest(event: String, figi: String, depth: Int, request_id: Option[String] = None) extends TInvestWSRequest
case class InstrumentInfoRequest(event: String, figi: String, request_id: Option[String] = None) extends TInvestWSRequest

object GenericDerivation {
  implicit val encodeEvent: Encoder[TInvestWSRequest] = Encoder.instance {
    case cr @ CandleRequest(_, _, _, _) => cr.asJson
    case or @ OrderBookRequest(_, _, _, _) => or.asJson
    case ir @ InstrumentInfoRequest(_, _, _) => ir.asJson
  }

  implicit val decodeEvent: Decoder[TInvestWSRequest] =
    List[Decoder[TInvestWSRequest]](
      Decoder[CandleRequest].widen,
      Decoder[OrderBookRequest].widen,
      Decoder[InstrumentInfoRequest].widen,
    ).reduceLeft(_ or _)
}


case class CandleResponse(event: String,
                          time: String, // RFC3339Nano
                          playload: CandlePlayload)
case class CandlePlayload(o: Double, // Цена открытия
                          c: Double, // Цена закрытия
                          h: Double, // Наибольшая цена
                          l: Double, // Наименьшая цена
                          v: Double, // Объем торгов
                          time: String, // RFC3339
                          interval: String,
                          figi: String)

case class OrderBookResponse(event: String,
                             time: String, // RFC3339Nano
                             playload: OrderBookPlayload)
case class OrderBookPlayload(depth: Int,
                             bids: Seq[(Double, Double)], // Массив [Цена, количество] предложений цены
                             asks: Seq[(Double, Double)], // Массив [Цена, количество] запросов цены
                             figi: String)

case class InstrumentInfoResponse(event: String,
                                  time: String, // RFC3339Nano
                                  playload: InstrumentInfoPlayload)
case class InstrumentInfoPlayload(trade_status: String,             // Статус торгов
                                  min_price_increment: Double,      // Шаг цены
                                  lot: Double,                      // Лот
                                  accrued_interest: Option[Double], // НКД. Возвращается только для бондов
                                  limit_up: Option[Double],         // Верхняя граница заявки. Возвращается только для RTS инструментов
                                  limit_down: Option[Double],       // Нижняя граница заявки. Возвращается только для RTS инструментов
                                  figi: String)

case class WrongResponse(event: String,
                          time: String, // RFC3339Nano
                          playload: WrongResponsePlayload)
case class WrongResponsePlayload(error: String,
                                 request_id: Option[String])

trait TInvestWSApi[F[_]] {

  /* interval ("1min","2min","3min","5min","10min","15min","30min","hour","2hour","4hour","day","week","month") */
  def subscribeCandle(figi: String, interval: String): F[Unit]
  /* 0 < DEPTH <= 20 */
  def subscribeOrderbook(figi: String, depth: Int): F[Unit]
  def subscribeInstrumentInfo(figi: String): F[Unit]

  def unsubscribeCandle(figi: String, interval: String): F[Unit]
  def unsubscribeOrderbook(figi: String, depth: Int): F[Unit]
  def unsubscribeInstrumentInfo(figi: String): F[Unit]
}


class TInvestWSApiHttp4s[F[_] : ConcurrentEffect: Timer: ContextShift : Concurrent](connection: WSConnectionHighLevel[F]) extends TInvestWSApi[F] {

  override def subscribeCandle(figi: String, interval: String): F[Unit] = {
    connection
      .send {
        WSFrame.Text {
          CandleRequest("candle:subscribe", figi, interval).asJson.noSpaces
        }
      }
  }

  override def subscribeOrderbook(figi: String, depth: Int): F[Unit] = {
    connection
      .send {
        WSFrame.Text {
          OrderBookRequest("orderbook:subscribe", figi, depth).asJson.noSpaces
        }
      }
  }

  override def subscribeInstrumentInfo(figi: String): F[Unit] = {
    connection
      .send {
        WSFrame.Text {
          InstrumentInfoRequest("instrument_info:subscribe", figi).asJson.noSpaces
        }
      }
  }

  override def unsubscribeCandle(figi: String, interval: String): F[Unit] = {
    connection
      .send {
        WSFrame.Text {
          CandleRequest("candle:subscribe", figi, interval).asJson.noSpaces
        }
      }
  }

  override def unsubscribeOrderbook(figi: String, depth: Int): F[Unit] = {
    connection
      .send {
        WSFrame.Text {
          OrderBookRequest("orderbook:unsubscribe", figi, depth).asJson.noSpaces
        }
      }
  }

  override def unsubscribeInstrumentInfo(figi: String): F[Unit] = {
    connection
      .send {
        WSFrame.Text {
          InstrumentInfoRequest("instrument_info:unsubscribe", figi).asJson.noSpaces
        }
      }
  }

  def listen() = {
    connection
      .receiveStream
      .collect { case WSFrame.Text(str, _) => handle(str) }
      .compile
      .toList
  }

  def handle(str: String): Unit = {
    val j = parser.parse(str).getOrElse(Json.Null)
    println(j.as[CandleRequest])
  }

}