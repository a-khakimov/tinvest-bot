package github.ainr

import cats.effect.IO.ioConcurrentEffect
import doobie.ExecutionContexts
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import github.ainr.config.Config
import org.http4s.client.blaze.BlazeClientBuilder
import github.ainr.telegram.TgBot
import github.ainr.tinvest.client.ws.{Request, TInvestWSApiHttp4s}
import org.http4s.{Header, Headers}
import org.http4s.implicits.http4sLiteralsSyntax
import org.slf4j.LoggerFactory
import telegramium.bots.high.BotApi

object Main extends IOApp {

  type F[+T] = IO[T]

  private val log = LoggerFactory.getLogger("Main")
  val iohi = IO { println("hi there") }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- iohi
      configFile <- IO { Option(System.getProperty("application.conf")) }
      config <- Config.load[IO](configFile)
      _ <- IO { log.info(s"loaded config: $config") }
      _ <- resources(config).use {
        case (httpClient, blocker, wsClient) => {
          implicit val tgBotApi = new BotApi[IO](httpClient, s"https://api.telegram.org/bot${config.tgBotApiToken}", blocker)
          implicit val tgBot = new TgBot[IO]()
          implicit val tinvestWSApi = new TInvestWSApiHttp4s[IO](wsClient)


          for {
            _ <- tinvestWSApi.subscribeCandle(Request())
            f1 <- tgBot.start().start
            f2 <- tinvestWSApi.listen().start
            _ <- f1.join
            _ <- f2.join
          } yield ()
        }
      }
    } yield ExitCode.Success
  }

  def resources(config: Config) = {
    import java.net.http.HttpClient
    import org.http4s.client.jdkhttpclient.{JdkWSClient, WSFrame, WSRequest}

    val wsUri = uri"wss://api-invest.tinkoff.ru/openapi/md/v1/md-openapi/ws"
    val wsHeaders = Headers.of(Header("Authorization", s"Bearer ${config.tinkoffInvestApiToken}"))

    for {
      wsClient <- JdkWSClient[F](HttpClient.newHttpClient()).connectHighLevel(WSRequest(wsUri, wsHeaders))
      ec <- ExecutionContexts.cachedThreadPool[F]
      httpClient <- BlazeClientBuilder[F](ec).resource
      blocker <- Blocker[F]
    } yield (httpClient, blocker, wsClient)
  }
}
