package github.ainr.tinvest.client
import cats.MonadError
import cats.effect.{ConcurrentEffect, ContextShift}
import github.ainr.TinvestBotMain.tinvest_token
import github.ainr.tinvest.Portfolio
import org.http4s.{AuthScheme, Credentials}
import org.http4s.Method
import org.http4s.client.Client
import org.http4s.headers.{Accept, Authorization}
import org.http4s.client.dsl.io._
import org.http4s.MediaType
import org.http4s.implicits.http4sLiteralsSyntax
import io.circe.generic.auto.exportDecoder
import org.http4s.circe.jsonOf
import cats.implicits._
import org.http4s._
import io.circe.generic.auto._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze._
import cats.effect.IO
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto.exportDecoder
import io.circe.generic.auto.exportEncoder
import org.http4s.dsl.io.GET

class TInvestApiHttp4s[F[_] : ConcurrentEffect: ContextShift](client: Client[F])(
  implicit F: MonadError[F, Throwable]
) extends TInvestApi[F] {

  val baseUrl = "https://api-invest.tinkoff.ru/openapi/sandbox"

  override def getPortfolio(): F[Portfolio] = {
    for {
      uri <- F.fromEither[Uri](
        Uri.fromString(s"$baseUrl/portfolio")
      )
      req = Request[F]()
        .putHeaders(
          Authorization(Credentials.Token(AuthScheme.Bearer, tinvest_token)),
          Accept(MediaType.application.json))
        .withMethod(Method.GET)
        .withUri(uri)
      res <- client.expect(req)(jsonOf[F, Portfolio])
    } yield res
  }
}
