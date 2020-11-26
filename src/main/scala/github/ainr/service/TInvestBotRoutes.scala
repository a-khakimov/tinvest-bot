package github.ainr.service

import cats.effect.Async
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.circe.jsonEncoder
import cats.implicits._
import github.ainr.tinvest4s.rest.client.TInvestApi
import org.http4s.dsl.Http4sDsl

/* for testing */

class TInvestBotRoutes[F[_]: Async] extends Http4sDsl[F] {

  private def endpoints(service: TInvestApi[F]): HttpRoutes[F] = routePortfolio(service)

  def routePortfolio(service: TInvestApi[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "portfolio" => {
      for {
        portfolio <- service.getPortfolio()
        resp <- Ok(portfolio.asJson)
      } yield resp
    }
  }
}

object TInvestBotRoutes {
  def apply[F[_] : Async](service: TInvestApi[F]): HttpRoutes[F] = new TInvestBotRoutes[F].endpoints(service)
}
