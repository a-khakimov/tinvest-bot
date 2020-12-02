package github.ainr.telegram

import telegramium.bots.{Location, Message}

trait TgExtractors {
  object Text {
    def unapply(msg: Message): Option[String] = msg.text
  }
}
