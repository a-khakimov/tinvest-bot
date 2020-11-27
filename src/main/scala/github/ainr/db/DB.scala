package github.ainr.db

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import github.ainr.config.DbConfig

import scala.concurrent.ExecutionContext

object DB {
  def transactor[F[_]: Async: ContextShift](
    config: DbConfig,
    ec: ExecutionContext,
    blocker: Blocker
  ) : Resource[F, HikariTransactor[F]] = {
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      ec,
      blocker
    )
  }
}
