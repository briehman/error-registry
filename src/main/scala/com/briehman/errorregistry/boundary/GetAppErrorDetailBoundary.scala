package com.briehman.errorregistry.boundary

import java.time.LocalDateTime

trait GetAppErrorDetailBoundary {
  def getDetails(errorId: Int): Option[AppErrorDetail]

  def getDetails(code: String): Option[AppErrorDetail]
}

case class AppErrorDetail(errorId: Int,
                          code: String,
                          message: String,
                          summary: AppErrorDetailStats)

case class AppErrorDetailStats(firstSeen: LocalDateTime,
                               lastSeen: LocalDateTime,
                               totalOccurrences: Int)
