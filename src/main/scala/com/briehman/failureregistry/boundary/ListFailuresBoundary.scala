package com.briehman.failureregistry.boundary

import com.briehman.failureregistry.models.Failure

trait ListFailuresBoundary {
  def findFailure(code: String): Option[Failure]
}
