package github.ainr.domain

import cats.implicits._
import cats.effect.{Sync, Timer}

import scala.concurrent.duration.DurationInt
import org.slf4j.LoggerFactory
import fs2.Stream
import github.ainr.db.DbAccess
import github.ainr.tinvest4s.rest.client.TInvestApiHttp4s

class Core[F[_]: Sync : Timer](implicit dbAccess: DbAccess[F],
                               implicit val tinvestRestApi: TInvestApiHttp4s[F]) {

  private val log = LoggerFactory.getLogger("Core")

  def start: F[Unit] = {
    (Stream.emit(()) ++ Stream.fixedRate[F](5.second))
      .evalTap { _ =>
        for {
          _ <- Sync[F].delay(log.info("Some actions..."))
        } yield ()
      }.compile.drain
  }

  def portfolioMsg(): F[String] = {
    for {
      portfolio <- tinvestRestApi.getPortfolio()
      msg <- s"${portfolio.payload.positions.map {
        pos =>
          s"${pos.name}: figi ${pos.figi}, balance ${pos.balance}, lots ${pos.lots}"
        }.mkString("\n")
      }".pure[F]
    } yield msg
  }

  def helpMsg(): F[String] = {
    s"""
       |Commands:
       |/help
       |/portfolio
       |""".stripMargin.pure[F]
  }

  def handleTgMessage(text: String): F[String] = {
    for {
      reply <- text match {
        case "/portfolio" => portfolioMsg()
        case "/help" => helpMsg()
        case _ => helpMsg()
      }
    } yield reply
  }
}
