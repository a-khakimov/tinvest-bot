package github.ainr

import cats.effect.IO.ioConcurrentEffect
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits.catsSyntaxFlatMapOps
import com.typesafe.scalalogging.LazyLogging
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import fs2.Stream
import github.ainr.config.Config
import github.ainr.db.{DB, DbAccess, DbAccessImpl}
import github.ainr.domain._
import github.ainr.telegram.{TgAuth, TgBot}
import github.ainr.tinvest4s.rest.client.{TInvestApi, TInvestApiHttp4s}
import github.ainr.tinvest4s.websocket.client.{TInvestWSApi, TInvestWSApiHttp4s, TInvestWSAuthorization, TInvestWSHandler}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.jdkhttpclient.WSConnectionHighLevel
import telegramium.bots.high.{Api, BotApi}

import scala.concurrent.duration.DurationInt

// Composable Resource Management in Scala: https://hackernoon.com/composable-resource-management-in-scala-0g7b3y5u
// Circular dependency in Scala: https://stackoverflow.com/questions/37037550/circular-dependency-in-scala
// https://blog.softwaremill.com/9-tips-about-using-cats-in-scala-you-might-want-to-know-e1bafd365f88


object Main extends IOApp with LazyLogging {

  type F[+T] = IO[T]

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      configFile <- IO { Option(System.getProperty("application.conf")) }
      config <- Config.load[F](configFile)
      _ <- resources(config).use {
        case (tgHttpClient, tinvestHttpClient, blocker, wsClient, transactor) => {
          implicit val dbAccess: DbAccess[F] = new DbAccessImpl[F](transactor)
          implicit val notificationRepo: NotificationRepo[F] = new NotificationRepoImpl[F]()
          implicit val tgBotApi: Api[F] = new BotApi[F](tgHttpClient, TgAuth().withToken(config.tgBotApiToken), blocker)
          implicit val wsHandler: TInvestWSHandler[F] = new WSHandler[F]()
          implicit val tinvestWSApi: TInvestWSApi[F] = new TInvestWSApiHttp4s[F](wsClient, wsHandler)
          implicit val tinvestApi: TInvestApi[F] = new TInvestApiHttp4s[F](tinvestHttpClient, config.tinkoffInvestApiToken)
          implicit val core: Core[F] = new CoreImpl[F]()
          implicit val tgBot: TgBot[F] = new TgBot[F]()
          implicit val notifier: Notifier[F] = new TelegramUserNotifier[F]()

          for {
            tinvestWsApiFiber <- tinvestWSApi.listen().start
            tgBotFiber <- tgBot.start().start
            _ <- core.init()
            _ <- (Stream.emit(()) ++ Stream.fixedRate[F](5.second))
              .evalTap {
                _ => {
                  core.start() >>
                    notifier.start()
                }
              }.compile.drain
            _ <- tgBotFiber.join
            _ <- tinvestWsApiFiber.join
          } yield ()
        }
      }
    } yield ExitCode.Success
  }

  def resources(config: Config): Resource[F, (Client[F], Client[F], Blocker, WSConnectionHighLevel[F], HikariTransactor[F])] = {
    import java.net.http.HttpClient

    import org.http4s.client.jdkhttpclient.JdkWSClient

    for {
      wsClient <- JdkWSClient[F](HttpClient.newHttpClient())
        .connectHighLevel(
          TInvestWSAuthorization()
            .withToken(config.tinkoffInvestApiToken)
        )
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
