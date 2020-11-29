package github.ainr.domain

import cats.implicits._
import cats.effect.{Sync, Timer}

import scala.concurrent.duration.DurationInt
import org.slf4j.LoggerFactory
import fs2.Stream
import github.ainr.db.DbAccess
import github.ainr.tinvest4s.models.portfolio.ExpectedYield
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
      portfolio <- tinvestRestApi.getPortfolio
      _ <- Sync[F].delay(log.info(portfolio.toString))
      msg <- portfolio match {
        case Right(p) => s"${p.payload.positions.map {
          pos => {
              val ey = pos.expectedYield.getOrElse(ExpectedYield("", 0))
              s"`${pos.instrumentType} [${pos.name} ${pos.figi} ${ey.value} ${ey.currency}] balance ${pos.balance}, lots ${pos.lots}`"
            }
          }.mkString("\n")
        }".pure[F]
        case Left(e) => s"Error: ${e}".pure[F]
      }
    } yield msg
  }

  def marketInstrumentMsg(instrument: String): F[String] = {
    for {
      currencies <- instrument match {
        case "stocks" => tinvestRestApi.stocks()
        case "bonds" => tinvestRestApi.bonds()
        case "etfs" => tinvestRestApi.etfs()
        case "currencies" => tinvestRestApi.currencies()
      }
      _ <- Sync[F].delay(log.info(currencies.toString))
      msg <- currencies match {
        case Right(p) => s"${p.payload.instruments.map {
            pos => {
              s"`${pos.figi} ${pos.name}`"
            }
          }.mkString("\n")
        }".pure[F]
        case Left(e) => s"Error: ${e}".pure[F]
      }
    } yield msg
  }

  def handleTgMessage(text: String): F[String] = {
    for {
      reply <- text match {
        case "/help" => helpMsg()
        case "/portfolio" => portfolioMsg()
        case "/stocks" => marketInstrumentMsg("stocks") /* TODO: слишком длинное сообщение */
        case "/bonds" => marketInstrumentMsg("bonds") /* TODO: слишком длинное сообщение */
        case "/etfs" => marketInstrumentMsg("etfs")
        case "/currencies" => marketInstrumentMsg("currencies")
        case _ => helpMsg()
      }
    } yield reply
  }

  def helpMsg(): F[String] = {
    s"""
       |Commands:
       |/help
       |/portfolio
       |/etfs
       |/currencies
       |""".stripMargin.pure[F]
    /*
    * - /stocks
    * - /bonds
    */
  }
}
