package github.ainr

import java.util.concurrent.TimeUnit
import doobie.ExecutionContexts
import scala.concurrent.duration.FiniteDuration
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import github.ainr.tinvest.client.{TInvestApiHttp4s}
import github.ainr.service.TInvestBotRoutes

object Main extends IOApp {

  type F[+T] = IO[T]
  val iohi = IO { println("hi there") }
  val tinvest_token = "t.5in2aATiub3iHxFg7jHI0t2jpaUgEAlJNVV2di3QkGukmDkRw6dJAYitMb9S7yjyKSGLVgdoXc-sy4VN1YoD3Q"

  override def run(args: List[String]): IO[ExitCode] = {
    resources.use(_ => IO.never).as(ExitCode.Success)
  }

  def resources = {
    for {
      ec <- ExecutionContexts.cachedThreadPool[F]
      httpClient <- BlazeClientBuilder[F](ec).resource
      tinvestApiHttp4s = new TInvestApiHttp4s[F](httpClient)
      httpApp = Router("/" -> TInvestBotRoutes[F](tinvestApiHttp4s)).orNotFound
      httpServer <- BlazeServerBuilder[F](ec)
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp).resource
    } yield (httpClient, httpServer)
  }
}
