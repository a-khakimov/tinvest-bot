package github.ainr.domain


trait Notifier[F[_]] {
  def notify(notifications: List[Notification]): F[Unit]
  def start(): F[Unit]
}
