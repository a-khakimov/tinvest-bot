import cats.effect.IO
import cats.effect.IO.ioEffect
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.ExecutionContexts
import github.ainr.db.DbAccess
import github.ainr.domain.{CoreImpl, NotificationRepo}
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import mocks.{DbAccessMock, NotificationRepoMock, TInvestApiMock, TInvestWSApiMock}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.specs2.matcher.Matchers

class ExampleSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  implicit val tm = IO.timer(ExecutionContexts.synchronous)

  implicit val dbAccess: DbAccess[IO] = new DbAccessMock[IO]
  implicit val tinvestApi: TInvestApi[IO] = new TInvestApiMock[IO]
  implicit val tinvestWSApi: TInvestWSApi[IO] = new TInvestWSApiMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoMock[IO]

  val core = new CoreImpl[IO]()

  "Core Tests" - {
    "portfolio message" in {
      core.portfolioMsg().asserting(_ shouldBe "`figi instrumentType [name] balance 0.2, lots 10`")
    }
  }
}