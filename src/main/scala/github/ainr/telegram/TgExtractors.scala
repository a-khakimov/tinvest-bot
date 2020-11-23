package github.ainr.telegram

import telegramium.bots.{Location, Message}

trait TgExtractors {
  object Location {
    def unapply(msg: Message): Option[Location] = msg.location
  }
  object Text {
    def unapply(msg: Message): Option[String] = msg.text
  }
}
