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


class CoreSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  implicit val tm = IO.timer(ExecutionContexts.synchronous)

  implicit val dbAccess: DbAccess[IO] = new DbAccessMock[IO]
  implicit val tinvestApi: TInvestApi[IO] = new TInvestApiMock[IO]
  implicit val tinvestWSApi: TInvestWSApi[IO] = new TInvestWSApiMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoMock[IO]

  val core = new CoreImpl[IO]()

  "Core Good Tests" - {

    "portfolio" in {
      core.portfolioMsg().asserting(_ shouldBe "`figi instrumentType [name] balance 0.2`")
    }

    "limit order with bad command" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.figi.lots.price")
        .asserting(_ shouldBe "Wrong command")
    }

    "limit order with good command" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.BBG005HLSZ23.10.5000")
        .asserting(_ shouldBe "`Success: orderId - SomeOrderId`")
    }

    "market order with bad command" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.figi.lots")
        .asserting(_ shouldBe "Wrong command")
    }

    "market order with good command" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10")
        .asserting(_ shouldBe "`Success: orderId - SomeOrderId`")
    }

    "market order with bad stoploss and takeprofit command" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10.stoploss.takeprofit")
        .asserting(_ shouldBe "Wrong command")
    }

    "market order with bad stoploss value" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10.5050.6000")
        .asserting(_ shouldBe "StopLoss(5050.0) выше чем стоимость акции (5000.0)")
    }

    "market order with bad takeprofit value" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10.4500.4900")
        .asserting(_ shouldBe "TakeProfit(4900.0) ниже чем стоимость акции (5000.0)")
    }

    "market order with good stoploss and takeprofit values" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10.4500.6000")
        .asserting(_ shouldBe
          """|Текущая стоимость акции (5000.0)
             |Выполнена покупка акций BBG005HLSZ23, количество 10
             |""".stripMargin)
    }
  }
}