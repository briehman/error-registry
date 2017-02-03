package com.briehman.errorregistry.boundary

import com.briehman.errorregistry.models.ErrorOccurrence

trait GetAppErrorOccurrenceBoundary {
  def getOccurrences(appErrorId: Int, startAt: Int, maxResults: Int): Seq[ErrorOccurrence]

  def countTotalOccurrences(appErrorId: Int): Int
}
