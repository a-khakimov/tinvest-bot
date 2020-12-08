import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import github.ainr.db.DbAccess
import github.ainr.domain.{BotOperation, Notification, NotificationRepo, NotificationRepoImpl, WSHandler}
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import github.ainr.tinvest4s.websocket.response.CandlePayload
import mocks.{DbAccessMock, TInvestApiGoodMock, TInvestWSApiMock}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.specs2.matcher.Matchers


class StreamingHandlerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val dbAccess: DbAccess[IO] = new DbAccessMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoImpl[IO]
  implicit val wsHandler = new WSHandler[IO]

  "StreamingHandlerSpec Tests" - {

    //"StreamingHandlerSpec should be work" in {
      //wsHandler.stopLoss(BotOperation(), CandlePayload())
    //}

  }
}
