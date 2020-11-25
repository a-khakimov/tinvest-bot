package github.ainr.tinvest.client.ws

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Timer}
import org.http4s.client.jdkhttpclient.{WSConnectionHighLevel, WSFrame}

/*
{
    "event": "candle:subscribe",
    "figi": "{{FIGI}}",
    "interval": "{{INTERVAL}}"
}
{
    "event": "candle:unsubscribe",
    "figi": "{{FIGI}}",
    "interval": "{{INTERVAL}}"
}
{
    "event": "orderbook:subscribe",
    "figi": "{{FIGI}}",
    "depth": {{DEPTH}}
}
{
    "event": "orderbook:unsubscribe",
    "figi": "{{FIGI}}",
    "depth": "{{DEPTH}}"
}
{
    "event": "instrument_info:subscribe",
    "figi": "{{FIGI}}"
}
{
    "event": "instrument_info:unsubscribe",
    "figi": "{{FIGI}}"
}
 */

case class Request()

trait TInvestWSApi[F[_]] {
  def subscribeCandle(request: Request): F[Unit]
}

class TInvestWSApiHttp4s[F[_] : ConcurrentEffect: Timer: ContextShift : Concurrent](connection: WSConnectionHighLevel[F]) extends TInvestWSApi[F] {

  override def subscribeCandle(request: Request): F[Unit] = {
    connection.send(WSFrame.Text("{\n\"event\":\"candle:subscribe\",\n\"figi\":\"BBG009S39JX6\",\n\"interval\":\"1min\"\n}"))
  }

  def listen() = {
    connection
      .receiveStream
      .collect { case WSFrame.Text(str, _) => handle(str) }
      .compile
      .toList
  }

  def handle(str: String): Unit = {
    println(str)
  }
}