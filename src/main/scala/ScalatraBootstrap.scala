import javax.servlet.ServletContext

import akka.actor.ActorSystem
import com.briehman.errorregistry.dispatcher.RabbitMqReceiveErrorDispatcher
import com.briehman.errorregistry.interactor.{GetAppErrorDetailInteractor, GetErrorSummaryInteractor, ReceiveErrorInteractor}
import com.briehman.errorregistry.repository.db.{DatabaseErrorOccurrenceRepository, DatabaseErrorRepository}
import com.briehman.errorregistry.service.FakeNotificationService
import com.briehman.errorregistry.web.{AppErrorDetailServlet, HomePageServlet}
import com.briehman.errorregistry.web.api.ErrorApiResource
import com.rabbitmq.client.ConnectionFactory
import org.scalatra.LifeCycle
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class ScalatraBootstrap extends LifeCycle {
  val connFactory = new ConnectionFactory()
  connFactory.setUri("amqp://guest:guest@localhost/%2F")

  val db = Database.forConfig("mysql")

  val errorRepository = new DatabaseErrorRepository(db)
  val occurrenceRepository = new DatabaseErrorOccurrenceRepository(db)

  val notificationService = new FakeNotificationService
  var system: ActorSystem = ActorSystem("receiveSystem")

  val receiveInteractor: ReceiveErrorInteractor = new ReceiveErrorInteractor(
    errorRepository,
    occurrenceRepository,
    notificationService
  )

  val errorSummaryInteractor = new GetErrorSummaryInteractor(occurrenceRepository)

  val receiveDispatcher = new RabbitMqReceiveErrorDispatcher(system, connFactory, receiveInteractor)

  val appErrorDetailBoundary = new GetAppErrorDetailInteractor(errorRepository, occurrenceRepository)

  override def init(context: ServletContext) {
    receiveDispatcher.start()

    // mount servlets like this:
    context mount (new ErrorApiResource(receiveInteractor), "/api/error/*")
    context mount (new HomePageServlet(errorSummaryInteractor), "/")
    context mount (new AppErrorDetailServlet(appErrorDetailBoundary), "/error")
  }

  override def destroy(context: ServletContext): Unit = {
    receiveDispatcher.stop()
    Await.result(system.terminate(), 1 minute)
    super.destroy(context)
  }
}