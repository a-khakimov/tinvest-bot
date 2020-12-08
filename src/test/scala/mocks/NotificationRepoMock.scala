package mocks

import github.ainr.domain.{Notification, NotificationRepo}

class NotificationRepoMock[F[_]] extends NotificationRepo[F] {

  override def push(notification: Notification): F[Int] = ???

  override def pull(): F[List[Notification]] = ???
}
