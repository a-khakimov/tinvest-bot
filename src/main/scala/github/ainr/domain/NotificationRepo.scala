package github.ainr.domain

trait NotificationRepo[F[_]] {
  def push(notification: Notification): F[Int]
  def pull(): F[List[Notification]]
}
