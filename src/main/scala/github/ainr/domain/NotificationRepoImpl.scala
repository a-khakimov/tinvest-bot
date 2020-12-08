package github.ainr.domain

import cats.effect.Sync
import cats.implicits._
import github.ainr.db.DbAccess
import org.slf4j.LoggerFactory

class NotificationRepoImpl[F[_]: Sync](implicit dbAccess: DbAccess[F]) extends NotificationRepo[F] {

  private val log = LoggerFactory.getLogger("NotificationRepo")

  def push(notification: Notification): F[Int] = {
    for {
      dbResE <- dbAccess.insertNotification(notification)
      r <- dbResE match {
        case Left(e) => Sync[F].delay(log.error(s"$e")) >>
          e.getErrorCode.pure[F] // TODO: Лучше вернуть нормальную ошибку (сейчас так для того чтобы не менять интерфейс)
        case Right(value) => value.pure[F]
      }
    } yield r
  }


  def pull(): F[List[Notification]] = {
    for {
      dbResE <- dbAccess.getNotifications
      r <- dbResE match {
        case Left(e) => Sync[F].delay(log.error(s"$e")) >>
          List().pure[F]   // TODO: Лучше вернуть нормальную ошибку (сейчас так для того чтобы не менять интерфейс)
        case Right(value) => value.pure[F]
      }
    } yield r
  }
}