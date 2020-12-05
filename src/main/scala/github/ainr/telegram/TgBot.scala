package github.ainr.telegram

import cats.effect.{Async, Sync, Timer}
import cats.implicits._
import github.ainr.domain.Core
import org.slf4j.{Logger, LoggerFactory}
import telegramium.bots.high.implicits._
import telegramium.bots.high.{Api, LongPollBot, Methods}
import telegramium.bots.{ChatIntId, Markdown, Message}


class TgBot[F[_]: Async : Timer](implicit val bot: Api[F], implicit val core: Core[F])
  extends LongPollBot[F](bot) with TgExtractors {

  val log: Logger = LoggerFactory.getLogger("TgBot")

  override def onMessage(msg: Message): F[Unit] = {
    Sync[F].delay {
      log.info(s"got message: $msg")
    } >> (msg match {
      case Text(text) => for {
        reply <- core.handleTgMessage(msg.chat.id, text)
        _ <- send(msg.chat.id, reply)
      } yield ()
      case _ => send(msg.chat.id, "_")
    })
  }

  def send(chatId: Long, text: String): F[Unit] = {
    Methods
      .sendMessage(chatId = ChatIntId(chatId), text = text, parseMode = Some(Markdown))
      .exec
      .void >> Sync[F].delay {
      log.info(s"send message[$chatId]:\n$text")
    }
  }
}
