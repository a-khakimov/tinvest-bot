package github.ainr.domain

import github.ainr.db.DbAccess

class NotificationRepoImpl[F[_]](implicit dbAccess: DbAccess[F]) extends NotificationRepo[F] {
  def push(notification: Notification): F[Int] = {
    dbAccess.insertNotification(notification)
  }

  def pull(): F[List[Notification]] = {
    dbAccess.getNotifications
  }
}