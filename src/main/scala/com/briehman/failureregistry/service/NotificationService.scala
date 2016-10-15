package com.briehman.failureregistry.service

import com.briehman.failureregistry.models.Failure

trait NotificationService {
  def notify(failure: Failure): Unit
}

class FakeNotificationService extends NotificationService {
  var notifications = Map[Failure, Int]()

  override def notify(failure: Failure): Unit = {
    notifications = notifications + (failure -> notifications.getOrElse(failure, 0))
  }
}