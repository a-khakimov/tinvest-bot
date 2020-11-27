package github.ainr.db

import cats.effect.Bracket
import doobie.util.transactor.Transactor
import doobie.implicits._


class DbAccess[F[_]: Bracket[*[_], Throwable]](transactor: Transactor[F]) {
  def getByID(id: Int): F[Option[String]] = Queries.getById(id).option.transact(transactor)
}
