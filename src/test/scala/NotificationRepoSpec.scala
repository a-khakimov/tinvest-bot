import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.ExecutionContexts
import github.ainr.db.DbAccess
import github.ainr.domain.{Notification, NotificationRepo, NotificationRepoImpl}
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import mocks.{DbAccessMock, NotificationRepoMock, TInvestApiGoodMock, TInvestWSApiMock}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.specs2.matcher.Matchers


class NotificationRepoSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val dbAccess: DbAccess[IO] = new DbAccessMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoImpl[IO]

  "NotificationRepo Tests" - {

    "NotificationRepo push should be work" in {
      notificationRepo.push(Notification(42, "Some notifications message")).asserting(_ shouldBe 0)
    }

    "NotificationRepo pull should be work" in {
      notificationRepo.pull().asserting(_ shouldBe List(
        Notification(42, "First notifications message"),
        Notification(73, "Second notifications message"),
      ))
    }
  }
}
