package github.ainr.domain

import cats.effect.{Sync, Timer}
import cats.implicits._
import github.ainr.telegram.TgBot
import org.slf4j.LoggerFactory

class TelegramUserNotifier[F[_]: Sync : Timer](implicit tgbot: TgBot[F],
                                               implicit val notificationRepo: NotificationRepo[F])
  extends Notifier[F] {

  private val log = LoggerFactory.getLogger("TelegramUserNotifier")

  override def notify(notifications: List[Notification]): F[Unit] = {
    for {
      _ <- notifications.traverse {
        notification => tgbot.send(notification.userId, notification.message)
      }
    } yield ()
  }

  override def start(): F[Unit] = {
    for {
      notifications <- notificationRepo.pull()
      _ <- notify(notifications)
    } yield ()
  }
}
