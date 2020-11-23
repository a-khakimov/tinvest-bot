package github.ainr.telegram

import cats.implicits._
import cats.effect.{Async, Sync, Timer}
import org.slf4j.{Logger, LoggerFactory}
import telegramium.bots.{ChatIntId, Message}
import telegramium.bots.high.implicits._
import telegramium.bots.high.{Api, LongPollBot, Methods}

class TgBot[F[_]: Async : Timer](implicit bot: Api[F]) extends LongPollBot[F](bot) with TgExtractors {

  val log: Logger = LoggerFactory.getLogger("TgBot")

  override def onMessage(msg: Message): F[Unit] = {
    Sync[F].delay {
      log.info(s"got message: $msg")
    } >> (msg match {
      case Text(text) => for {
        _ <- send(msg.chat.id, text)
      } yield ()
      case _ => send(msg.chat.id, "_")
    })
  }


  def send(chatId: Long, text: String): F[Unit] = {
    Methods
      .sendMessage(chatId = ChatIntId(chatId), text = text)
      .exec
      .void >> Sync[F].delay {
      log.info(s"send message[$chatId]: $text")
    }
  }
}
