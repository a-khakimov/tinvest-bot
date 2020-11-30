package github.ainr

import cats.effect.IO.ioConcurrentEffect
import cats.effect.{Async, Blocker, ExitCode, IO, IOApp, Resource, Sync, Timer}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import github.ainr.config.Config
import github.ainr.db.{DB, DbAccess}
import github.ainr.domain.Core
import github.ainr.telegram.TgBot
import github.ainr.tinvest4s.rest.client.{TInvestApi, TInvestApiHttp4s}
import github.ainr.tinvest4s.websocket.client.{TInvestWSApi, TInvestWSApiHttp4s, TInvestWSHandler}
import github.ainr.tinvest4s.websocket.response.{CandleResponse, InstrumentInfoResponse, OrderBookResponse, TInvestWSResponse}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.jdkhttpclient.WSConnectionHighLevel
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Header, Headers}
import org.slf4j.LoggerFactory
import telegramium.bots.high.{Api, BotApi}

// Composable Resource Management in Scala: https://hackernoon.com/composable-resource-management-in-scala-0g7b3y5u
// Circular dependency in Scala: https://stackoverflow.com/questions/37037550/circular-dependency-in-scala

/* For example */
class SomeHandler[F[_]: Async : Timer]() extends TInvestWSHandler[F] {

  override def handle(response: TInvestWSResponse): F[Unit] = {
    val foo = response match {
      case CandleResponse(event, time, payload) => s"$event: ${payload.figi}"
      case OrderBookResponse(event, time, payload) => s"$event: ${payload.figi}"
      case InstrumentInfoResponse(event, time, payload) => s"$event: ${payload.figi}"
      case _ => "Error"
    }

    Sync[F].delay { println(s"$foo") }
  }
}

object Main extends IOApp {

  type F[+T] = IO[T]

  private val log = LoggerFactory.getLogger("Main")

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      configFile <- IO { Option(System.getProperty("application.conf")) }
      config <- Config.load[F](configFile)
      _ <- resources(config).use {
        case (tgHttpClient, tinvestHttpClient, blocker, wsClient, transactor) => {
          implicit val tgBotApi: Api[F] = new BotApi[F](tgHttpClient, s"https://api.telegram.org/bot${config.tgBotApiToken}", blocker)
          implicit val tinvestApi: TInvestApi[F] = new TInvestApiHttp4s[F](tinvestHttpClient, config.tinkoffInvestApiToken)
          implicit val handler: TInvestWSHandler[F] = new SomeHandler[F]()
          implicit val dbAccess: DbAccess[F] = new DbAccess[F](transactor)
          implicit val core: Core[F] = new Core[F]()
          implicit val tgBot: TgBot[F] = new TgBot[F]()
          implicit val tinvestWSApi: TInvestWSApi[F] = new TInvestWSApiHttp4s[F](wsClient, handler)

          for {
            names <- dbAccess.getByID(1)
            _ <- IO { println(names) }
            _ <- tinvestWSApi.subscribeCandle("BBG009S39JX6", "1min")
            _ <- tinvestWSApi.subscribeOrderbook("BBG009S39JX6", 5)

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

  def resources(config: Config): Resource[F, (Client[F], Client[F], Blocker, WSConnectionHighLevel[F], HikariTransactor[F])] = {
    import java.net.http.HttpClient

    import org.http4s.client.jdkhttpclient.{JdkWSClient, WSRequest}

    val wsUri = uri"wss://api-invest.tinkoff.ru/openapi/md/v1/md-openapi/ws"
    val wsHeaders = Headers.of(Header("Authorization", s"Bearer ${config.tinkoffInvestApiToken}"))

    for {
      wsClient <- JdkWSClient[F](HttpClient.newHttpClient()).connectHighLevel(WSRequest(wsUri, wsHeaders))
      httpClEc <- ExecutionContexts.cachedThreadPool[F]
      tgHttpClEc <- ExecutionContexts.cachedThreadPool[F]
      dbEc <- ExecutionContexts.cachedThreadPool[F]
      blocker <- Blocker[F]
      transactor <- DB.transactor(config.database, dbEc, blocker)
      tgHttpClient <- BlazeClientBuilder[F](tgHttpClEc).resource
      tinvestHttpClient <- BlazeClientBuilder[F](httpClEc).resource
    } yield (tgHttpClient, tinvestHttpClient, blocker, wsClient, transactor)
  }
}
