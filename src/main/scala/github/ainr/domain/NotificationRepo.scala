package github.ainr.domain

import github.ainr.db.DbAccess

class NotificationRepo[F[_]](implicit dbAccess: DbAccess[F]) {
  def push(notification: Notification): F[Int] = {
      dbAccess.insertNotification(notification)
  }

  def pull(): F[List[Notification]] = {
    dbAccess.getNotifications
  }
}
