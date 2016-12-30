package com.briehman.errorregistry.service

import com.briehman.errorregistry.models.AppError

trait NotificationService {
  def notify(error: AppError): Unit
}

class FakeNotificationService extends NotificationService {
  private var notifications = Map[AppError, Int]()

  override def notify(error: AppError): Unit = {
    notifications = notifications + (error -> notifications.getOrElse(error, 0))
  }
}