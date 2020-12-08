import cats.effect.IO
import cats.effect.IO.ioEffect
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.ExecutionContexts
import github.ainr.db.DbAccess
import github.ainr.domain.{CoreImpl, NotificationRepo}
import github.ainr.tinvest4s.rest.client.TInvestApi
import github.ainr.tinvest4s.websocket.client.TInvestWSApi
import mocks.{DbAccessMock, NotificationRepoMock, TInvestApiBadMock, TInvestApiGoodMock, TInvestWSApiMock}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.specs2.matcher.Matchers


class CoreSpecWithBadTinvestApi extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  implicit val tm = IO.timer(ExecutionContexts.synchronous)

  implicit val dbAccess: DbAccess[IO] = new DbAccessMock[IO]
  implicit val tinvestApi: TInvestApi[IO] = new TInvestApiBadMock[IO]
  implicit val tinvestWSApi: TInvestWSApi[IO] = new TInvestWSApiMock[IO]
  implicit val notificationRepo: NotificationRepo[IO] = new NotificationRepoMock[IO]

  val core = new CoreImpl[IO]()

  "Core Tests with bad TinvestApi responses" - {

    "portfolio command should return error" in {
      core.portfolioMsg().asserting(_ shouldBe "Error: getPortfolio error status")
    }

    "etfs command should return error" in {
      core.marketInstrumentMsg("etfs")
        .asserting(_ shouldBe "`Error: etfs error status`")
    }

    "currencies command should return error" in {
      core.marketInstrumentMsg("currencies")
        .asserting(_ shouldBe "`Error: currencies error status`")
    }

    "stocks command should return error" in {
      core.marketInstrumentMsg("stocks")
        .asserting(_ shouldBe "`Error: stocks error status`")
    }

    "bonds command should return error" in {
      core.marketInstrumentMsg("bonds")
        .asserting(_ shouldBe "`Error: bonds error status`")
    }

    "orderbook command should return error" in {
      core.doOrderbook("/orderbook.FIGI.2")
        .asserting(_ shouldBe "`Error: orderbook error status`")
    }

    "limit order command should return error" in {
      core.doLimitOrder("Buy", "/limitOrderBuy.BBG005HLSZ23.10.5000")
        .asserting(_ shouldBe "`Error: limitOrder error status`")
    }

    "market order command should return error" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10")
        .asserting(_ shouldBe "`Error: marketOrder error status`")
    }

    "market order with good stoploss and takeprofit values" in {
      core.doMarketOrderCmd("Buy", "/marketOrderBuy.BBG005HLSZ23.10.4500.6000")
        .asserting(_ shouldBe "`Error: orderbook error status`")
    }
  }
}