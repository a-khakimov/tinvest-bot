package github.ainr.tinvest4s.rest.client

import cats.MonadError
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.implicits._
import github.ainr.tinvest4s.models.TInvestError
import github.ainr.tinvest4s.models.portfolio.Portfolio
import github.ainr.tinvest4s.models.orders.{LimitOrderRequest, MarketOrderRequest, OrderResponse}
import io.circe.generic.auto.exportDecoder
//import io.circe.generic.auto.{exportDecoder, exportEncoder}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
//import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.http4s._


class TInvestApiHttp4s[F[_] : ConcurrentEffect: ContextShift](client: Client[F],
                                                              token: String,
                                                              sandbox: Boolean = true)(
  implicit F: MonadError[F, Throwable]
) extends TInvestApi[F] {

  private val baseUrl: String = "https://api-invest.tinkoff.ru/openapi/sandbox"
  private val auth = Authorization(Credentials.Token(AuthScheme.Bearer, token))
  private val mediaTypeJson = Accept(MediaType.application.`route-apd+xml`)

  override def getPortfolio: F[Either[String, Portfolio]] = {
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$baseUrl/portfolio"))
      result <- client run {
        Request[F]()
          .putHeaders(auth, mediaTypeJson)
          .withMethod(Method.GET)
          .withUri(uri)
      } use {
        case Status.Successful(response) => response.attemptAs[Portfolio].leftMap(_.message).value
        case error => error.as[String].map(body => {
          val msg: Either[String, Portfolio] = Left(s"Request failed with status ${error.status.code} and body $body")
          msg
        })
      }
    } yield result
  }

  override def limitOrder(figi: String, request: LimitOrderRequest): F[Either[String, OrderResponse]] = {
    /*
    for {
      uri <- F.fromEither[Uri](Uri.fromString(s"$baseUrl/orders/limit-order?figi=${figi}"))
      res <- client.expect(
        Request[F]()
          .putHeaders(auth, mediaTypeJson)
          .withMethod(Method.POST)
          .withEntity(request)
          .withUri(uri)
      )(jsonOf[F, OrderResponse])
    } yield Either.right(res)*/
    ???
  }

  override def marketOrder(figi: String, request: MarketOrderRequest): F[Either[String, OrderResponse]] = ???
}

