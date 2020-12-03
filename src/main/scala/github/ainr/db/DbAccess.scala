package github.ainr.db

import cats.effect.Bracket
import doobie.implicits._
import doobie.util.transactor.Transactor
import github.ainr.domain.BotOperation
import github.ainr.domain.OperationStatus.OperationStatus
import github.ainr.tinvest4s.websocket.response.CandlePayload


class DbAccess[F[_]: Bracket[*[_], Throwable]](transactor: Transactor[F]) {

  def insertCandle(candle: CandlePayload): F[Int] = {
    Queries.insertCandle(candle).run.transact(transactor)
  }

  def insertOperation(operation: BotOperation): F[Int] = {
    Queries.insertOperation(operation).run.transact(transactor)
  }

  def getOperationsByStatus(operationStatus: OperationStatus): F[Option[BotOperation]] = {
    Queries.getOperationsByStatus(operationStatus).option.transact(transactor)
  }
}
