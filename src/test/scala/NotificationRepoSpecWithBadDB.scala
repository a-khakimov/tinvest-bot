import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import github.ainr.db.DbAccess
import github.ainr.domain.{Notification, NotificationRepo, NotificationRepoImpl}
import mocks.{DbAccessBadMock, DbAccessMock}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.specs2.matcher.Matchers


class NotificationRepoSpecWithBadDB extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val dbAccess: DbAccess[IO] = new DbAccessBadMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoImpl[IO]

  "NotificationRepo Tests with bad database" - {

    "NotificationRepo push should get error" in {
      notificationRepo.push(Notification(42, "Some notifications message")).asserting(_ shouldBe 42)
    }

    "NotificationRepo pull should get empty list" in {
      notificationRepo.pull().asserting(_ shouldBe List())
    }
  }
}
