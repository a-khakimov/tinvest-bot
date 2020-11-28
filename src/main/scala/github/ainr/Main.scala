package github.ainr

import cats.effect.IO.ioConcurrentEffect
import doobie.ExecutionContexts
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import github.ainr.config.Config
import github.ainr.db.{DB, DbAccess}
import github.ainr.domain.Core
import org.http4s.client.blaze.BlazeClientBuilder
import github.ainr.telegram.TgBot
import github.ainr.tinvest4s.rest.client.TInvestApiHttp4s
import github.ainr.tinvest4s.websocket.client.TInvestWSApiHttp4s
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.WSConnectionHighLevel
import org.http4s.{Header, Headers}
import org.http4s.implicits.http4sLiteralsSyntax
import org.slf4j.LoggerFactory
import telegramium.bots.high.{Api, BotApi}

// https://hackernoon.com/composable-resource-management-in-scala-0g7b3y5u

object Main extends IOApp {

  type F[+T] = IO[T]

  private val log = LoggerFactory.getLogger("Main")

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      configFile <- IO { Option(System.getProperty("application.conf")) }
      config <- Config.load[F](configFile)
      _ <- resources(config).use {
        case (httpClient, blocker, wsClient, transactor) => {
          implicit val tgBotApi: Api[F] = new BotApi[IO](httpClient, s"https://api.telegram.org/bot${config.tgBotApiToken}", blocker)
          implicit val tinvestApi: TInvestApiHttp4s[F] = new TInvestApiHttp4s[IO](httpClient, config.tinkoffInvestApiToken)
          implicit val tinvestWSApi: TInvestWSApiHttp4s[F] = new TInvestWSApiHttp4s[IO](wsClient)
          implicit val dbAccess: DbAccess[F] = new DbAccess[F](transactor)
          implicit val core: Core[F] = new Core[F]()
          implicit val tgBot: TgBot[F] = new TgBot[F]()

          for {
            names <- dbAccess.getByID(1)
            _ <- IO { println(names) }
            //_ <- tinvestWSApi.subscribeCandle("BBG009S39JX6", "1min")
            //_ <- tinvestWSApi.unsubscribeCandle("BBG009S39JX6", "1min")

            tinvestWsApiFiber <- tinvestWSApi.listen().start
            tgBotFiber <- tgBot.start().start
            _ <- core.start.start.void
            _ <- tgBotFiber.join
            _ <- tinvestWsApiFiber.join
          } yield ()
        }
      }
    } yield ExitCode.Success
  }

  def resources(config: Config): Resource[F, (Client[F], Blocker, WSConnectionHighLevel[F], HikariTransactor[F])] = {
    import java.net.http.HttpClient
    import org.http4s.client.jdkhttpclient.{JdkWSClient, WSRequest}

    val wsUri = uri"wss://api-invest.tinkoff.ru/openapi/md/v1/md-openapi/ws"
    val wsHeaders = Headers.of(Header("Authorization", s"Bearer ${config.tinkoffInvestApiToken}"))

    for {
      wsClient <- JdkWSClient[F](HttpClient.newHttpClient()).connectHighLevel(WSRequest(wsUri, wsHeaders))
      ec <- ExecutionContexts.cachedThreadPool[F]
      blocker <- Blocker[F]
      transactor <- DB.transactor(config.database, ec, blocker)
      httpClient <- BlazeClientBuilder[F](ec).resource
    } yield (httpClient, blocker, wsClient, transactor)
  }
}
