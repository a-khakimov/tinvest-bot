package github.ainr.db

import cats.effect.Bracket
import doobie.util.transactor.Transactor
import doobie.implicits._
import github.ainr.tinvest4s.websocket.response.CandlePayload


class DbAccess[F[_]: Bracket[*[_], Throwable]](transactor: Transactor[F]) {
  def getByID(id: Int): F[Option[String]] = Queries.getById(id).option.transact(transactor)
  def insertCandle(candle: CandlePayload): F[Int] = {
    Queries.insertCandle(candle).run.transact(transactor)
  }
}
