package github.ainr.tinvest4s.rest.client

import cats.MonadError
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.implicits._
import github.ainr.tinvest4s.models.{MarketInstrumentListResponse, TInvestError}
import github.ainr.tinvest4s.models.portfolio.Portfolio
import github.ainr.tinvest4s.models.orders.{LimitOrderRequest, MarketOrderRequest, OrderResponse}
import io.circe.generic.auto._
import org.http4s.Status.Successful
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.http4s._
import org.http4s.circe._


class TInvestApiHttp4s[F[_] : ConcurrentEffect: ContextShift](client: Client[F],
                                                              token: String,
                                                              sandbox: Boolean = true)(
  implicit F: MonadError[F, Throwable]
) extends TInvestApi[F] {

  private val baseUrl: String = "https://api-invest.tinkoff.ru/openapi/sandbox"
  private val auth = Authorization(Credentials.Token(AuthScheme.Bearer, token))
  private val mediaTypeJson = Accept(MediaType.application.json)
  private val baseRequest = Request[F]().putHeaders(auth, mediaTypeJson)

  override def getPortfolio: F[Either[TInvestError, Portfolio]] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$baseUrl/portfolio"))
      result <- client run {
        baseRequest
          .withMethod(Method.GET)
          .withUri(uri)
      } use {
        case Successful(resp) => resp.as[Portfolio].map(Right(_).withLeft[TInvestError])
        case error => error.as[TInvestError].map(Left(_).withRight[Portfolio])
      }
    } yield result
  }

  override def limitOrder(figi: String, request: LimitOrderRequest): F[Either[TInvestError, OrderResponse]] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$baseUrl/orders/limit-order?figi=${figi}"))
      result <- client.run(
        baseRequest
          .withMethod(Method.POST)
          .withEntity(request)
          .withUri(uri)
        ) use {
          case Successful(resp) => resp.as[OrderResponse].map(Right(_).withLeft[TInvestError])
          case error => error.as[TInvestError].map(Left(_).withRight[OrderResponse])
      }
    } yield result
  }

  override def marketOrder(figi: String, request: MarketOrderRequest): F[Either[TInvestError, OrderResponse]] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$baseUrl/orders/market-order?figi=${figi}"))
      result <- client run {
        baseRequest
          .withMethod(Method.POST)
          .withEntity(request)
          .withUri(uri)
      } use {
        case Successful(resp) => resp.as[OrderResponse].map(Right(_).withLeft[TInvestError])
        case error => error.as[TInvestError].map(Left(_).withRight[OrderResponse])
      }
    } yield result
  }

  private def getMarketInstrumentList(instrument: String): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$baseUrl/market/$instrument"))
      result <- client run {
        baseRequest
          .withMethod(Method.GET)
          .withUri(uri)
      } use {
        case Successful(resp) => resp.as[MarketInstrumentListResponse].map(Right(_).withLeft[TInvestError])
        case error => error.as[TInvestError].map(Left(_).withRight[MarketInstrumentListResponse])
      }
    } yield result
  }

  override def stocks(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    getMarketInstrumentList("currencies")
  }

  override def bonds(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    getMarketInstrumentList("currencies")
  }

  override def etfs(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    getMarketInstrumentList("etfs")
  }

  override def currencies(): F[Either[TInvestError, MarketInstrumentListResponse]] = {
    getMarketInstrumentList("currencies")
  }
}

