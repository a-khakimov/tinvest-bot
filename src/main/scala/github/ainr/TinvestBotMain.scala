package github.ainr

import java.util.concurrent.TimeUnit

import cats.effect.{ExitCode, IO, IOApp, Resource}
import github.ainr.tinvest.client.{TInvestApi, TInvestApiHttp4s}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.duration.FiniteDuration
import doobie.ExecutionContexts
import github.ainr.tinvest.orders.LimitOrderRequest

object TinvestBotMain extends IOApp {

  type F[+T] = IO[T]
  val iohi = IO { println("hi there") }
  val tinvest_token = "t.5in2aATiub3iHxFg7jHI0t2jpaUgEAlJNVV2di3QkGukmDkRw6dJAYitMb9S7yjyKSGLVgdoXc-sy4VN1YoD3Q"

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- iohi
      portfolio <- resources().use {
        case httpClient => {
          implicit val tinvestApiHttp4s: TInvestApi[F] = new TInvestApiHttp4s[F](httpClient)
          val limitOrder = tinvestApiHttp4s.limitOrder("BBG009S39JX6", LimitOrderRequest(1, "Buy", 1))
          val portfolio = tinvestApiHttp4s.getPortfolio()
          portfolio
        }
      }
      _ = println(portfolio)
    } yield ExitCode.Success
  }

  private def resources() : Resource[F, Client[F]] = {
    for {
      httpCp <- ExecutionContexts.cachedThreadPool[F]
      httpClient <- BlazeClientBuilder[F](httpCp)
        .withResponseHeaderTimeout(FiniteDuration(60, TimeUnit.SECONDS))
        .resource
    } yield httpClient
  }
}
