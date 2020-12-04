package github.ainr.domain

import scala.concurrent.duration.FiniteDuration


trait Notifier[F[_]] {
  def notify(notifications: List[Notification]): F[Unit]
  def start(every: FiniteDuration): F[Unit]
}
