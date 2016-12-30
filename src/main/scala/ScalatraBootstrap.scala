import javax.servlet.ServletContext

import akka.actor.ActorSystem
import com.briehman.failureregistry.ScalatraTestServlet
import com.briehman.failureregistry.dispatcher.RabbitMqReceiveFailureDispatcher
import com.briehman.failureregistry.interactor.{GetFailureSummaryInteractor, ReceiveFailureInteractor}
import com.briehman.failureregistry.repository.{InMemoryFailureOccurrenceRepository, InMemoryFailureRepository}
import com.briehman.failureregistry.service.FakeNotificationService
import com.briehman.failureregistry.web.HomePageServlet
import com.briehman.failureregistry.web.api.SendFailureResource
import com.rabbitmq.client.ConnectionFactory
import org.scalatra.LifeCycle

import scala.concurrent.Await
import scala.concurrent.duration._

class ScalatraBootstrap extends LifeCycle {
  val connFactory = new ConnectionFactory()
  connFactory.setUri("amqp://guest:guest@localhost/%2F")

  implicit val failureRepository = new InMemoryFailureRepository
  implicit val occurrenceRepository = new InMemoryFailureOccurrenceRepository(failureRepository)
  val notificationService = new FakeNotificationService
  var system: ActorSystem = ActorSystem("receiveSystem")

  val receiveInteractor: ReceiveFailureInteractor = new ReceiveFailureInteractor(
    failureRepository,
    occurrenceRepository,
    notificationService
  )

  val failureSummaryInteractor = new GetFailureSummaryInteractor(failureRepository, occurrenceRepository)

  val receiveDispatcher = new RabbitMqReceiveFailureDispatcher(system, connFactory, receiveInteractor)

  override def init(context: ServletContext) {
    receiveDispatcher.start()

    // mount servlets like this:
    context mount (new SendFailureResource(receiveInteractor), "/error/*")
    context mount (new HomePageServlet(failureSummaryInteractor), "/")
  }

  override def destroy(context: ServletContext): Unit = {
    receiveDispatcher.stop()
    Await.result(system.terminate(), 1 minute)
    super.destroy(context)
  }
}