package com.briehman.errorregistry.interactor

import com.briehman.errorregistry.boundary.{AppErrorDetail, GetAppErrorDetailBoundary}
import com.briehman.errorregistry.repository.{ErrorOccurrenceRepository, ErrorRepository}

class GetAppErrorDetailInteractor(errorRepository: ErrorRepository,
                                  errorOccurrenceRepository: ErrorOccurrenceRepository)
  extends GetAppErrorDetailBoundary {
  override def getDetails(errorId: Int): Option[AppErrorDetail] = {
    errorRepository.find(errorId)
      .map { e =>
        val stats = errorOccurrenceRepository.getStatsByAppError(e.id).get
        AppErrorDetail(e.id, e.code, e.error, stats)
      }
  }

  override def getDetails(code: String): Option[AppErrorDetail] = {
    errorRepository.find(code)
      .map { e =>
        val stats = errorOccurrenceRepository.getStatsByAppError(e.id).get
        AppErrorDetail(e.id, e.code, e.error, stats)
      }
  }
}
