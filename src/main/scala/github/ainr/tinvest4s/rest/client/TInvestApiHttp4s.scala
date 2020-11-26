package github.ainr.tinvest4s.rest.client

import cats.MonadError
import cats.effect.{ConcurrentEffect, ContextShift}
import cats.implicits._
import github.ainr.tinvest4s.rest.orders.{LimitOrder, LimitOrderRequest}
import github.ainr.tinvest4s.rest.portfolio.Portfolio
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.http4s.{AuthScheme, Credentials, MediaType, Method, _}


class TInvestApiHttp4s[F[_] : ConcurrentEffect: ContextShift](client: Client[F])(
  implicit F: MonadError[F, Throwable]
) extends TInvestApi[F] {

  val baseUrl = "https://api-invest.tinkoff.ru/openapi/sandbox"

  override def getPortfolio(): F[Portfolio] = {
    for {
      uri <- F.fromEither[Uri](
        Uri.fromString(s"$baseUrl/portfolio")
      )
      req = Request[F]()
        .putHeaders(
          Authorization(Credentials.Token(AuthScheme.Bearer, "tinvest_token")),
          Accept(MediaType.application.json))
        .withMethod(Method.GET)
        .withUri(uri)
      res <- client.expect(req)(jsonOf[F, Portfolio])
    } yield res
  }

  override def limitOrder(figi: String, request: LimitOrderRequest): F[LimitOrder] = {
    for {
      uri <- F.fromEither[Uri](
        Uri.fromString(s"$baseUrl/orders/limit-order?figi=${figi}")
      )
      req = Request[F]()
        .putHeaders(
          Authorization(Credentials.Token(AuthScheme.Bearer, "tinvest_token")),
          Accept(MediaType.application.json))
        .withMethod(Method.POST)
        .withEntity(request)
        .withUri(uri)
      res <- client.expect(req)(jsonOf[F, LimitOrder])
    } yield res
  }
}

