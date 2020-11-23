package github.ainr

import doobie.ExecutionContexts
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import github.ainr.config.Config
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import github.ainr.tinvest.client.TInvestApiHttp4s
import github.ainr.service.TInvestBotRoutes
import github.ainr.telegram.TgBot
import org.slf4j.LoggerFactory
import telegramium.bots.high.BotApi

import scala.concurrent.duration.DurationInt
import scala.util.Success

object Main extends IOApp {

  private val log = LoggerFactory.getLogger("Main")

  type F[+T] = IO[T]
  val iohi = IO { println("hi there") }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- iohi
      configFile <- IO { Option(System.getProperty("application.conf")) }
      config <- Config.load[IO](configFile)
      _ <- IO { log.info(s"loaded config: $config") }
      _ <- resources.use {
        case (httpClient, blocker) => {
          implicit val tgBotApi = new BotApi[IO](httpClient, s"https://api.telegram.org/bot${config.tgBotApiToken}", blocker)
          implicit val tgBot = new TgBot[IO]()
          for {
            _ <- tgBot.start()
          } yield ()
        }
      }
    } yield ExitCode.Success
  }

  def resources = {
    for {
      ec <- ExecutionContexts.cachedThreadPool[F]
      httpClient <- BlazeClientBuilder[F](ec).resource
      blocker <- Blocker[IO]
    } yield (httpClient, blocker)
  }
}
