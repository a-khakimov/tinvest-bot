import cats.effect.IO
import cats.effect.IO.ioEffect
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.ExecutionContexts
import github.ainr.db.DbAccess
import github.ainr.domain.{CoreImpl, NotificationRepo}
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import mocks.{DbAccessMock, NotificationRepoMock, TInvestApiGoodMock, TInvestWSApiMock}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.specs2.matcher.Matchers


class CoreSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  implicit val tm = IO.timer(ExecutionContexts.synchronous)

  implicit val dbAccess: DbAccess[IO] = new DbAccessMock[IO]
  implicit val tinvestApi: TInvestApi[IO] = new TInvestApiGoodMock[IO]
  implicit val tinvestWSApi: TInvestWSApi[IO] = new TInvestWSApiMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoMock[IO]

  val core = new CoreImpl[IO]()

  "Core Good Tests" - {

    "portfolio" in {
      core.portfolioMsg().asserting(_ shouldBe "`figi instrumentType [name] balance 0.2`")
    }

    "etfs command should works" in {
      core.marketInstrumentMsg("etfs")
        .asserting(_ shouldBe "`FIGI Some etf`")
    }

    "currencies command should works" in {
      core.marketInstrumentMsg("currencies")
        .asserting(_ shouldBe "`FIGI Some currency`")
    }

    "stocks command should works" in {
      core.marketInstrumentMsg("stocks")
        .asserting(_ shouldBe "`FIGI Some stock`")
    }

    "bonds command should works" in {
      core.marketInstrumentMsg("bonds")
        .asserting(_ shouldBe "`FIGI Some bond`")
    }

    "orderbook command with bad depth" in {
      core.doOrderbook("/orderbook.FIGI.badDepth")
        .asserting(_ shouldBe "Wrong command")
    }

    "orderbook command with good depth" in {
      core.doOrderbook("/orderbook.FIGI.2")
        .asserting(_ shouldBe
         """|`lastPrice:5000.0`
            |`closePrice:5000.0`
            |`limitUp:5000.0`
            |`limitDown:5000.0`
            |`tradeStatus:NormalTrading`
            |`bids:(0.0 0),(1.0 1)`
            |`asks:(0.0 0),(1.0 1)`
            |""".stripMargin)
    }

    "limit order with bad command" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.figi.lots.price")
        .asserting(_ shouldBe "Wrong command")
    }

    "limit order with bad bad command" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.some_bad_text")
        .asserting(_ shouldBe "Wrong command")
    }

    "limit order with bad lots" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.BBG005HLSZ23.badlots.5000")
        .asserting(_ shouldBe "Wrong command")
    }

    "limit order with bad price" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.BBG005HLSZ23.10.badprice")
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

    "activeOperations command should work" in {
      core.activeOperations
        .asserting(_ shouldBe "`UserID[42] [1] SomeFIGI Lots=10 TakeProfit=6000.0 StopLoss=4500.0`\n")
    }

    "stopOperations command should work" in {
      core.stopOperations
        .asserting(_ shouldBe "`Stop 1 SomeFIGI 4500.0 6000.0`")
    }
  }
}