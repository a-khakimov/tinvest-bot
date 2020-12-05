package github.ainr.telegram

case class TgAuth() {
  def withToken(token: String) = s"https://api.telegram.org/bot$token"
}
