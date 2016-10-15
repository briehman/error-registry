import com.briehman.failureregistry.repository.{InMemoryFailureOccurrenceRepository, InMemoryFailureRepository}
import com.briehman.failureregistry.web.SendFailureResource
import org.scalatra.LifeCycle
import javax.servlet.ServletContext
import com.briehman.failureregistry.ScalatraTestServlet

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {

    // mount servlets like this:
    context mount (new ScalatraTestServlet, "/scalatra")

    implicit val failureRepository = new InMemoryFailureRepository
    implicit val occurrenceRepository = new InMemoryFailureOccurrenceRepository(failureRepository)
    context mount (new SendFailureResource, "/*")

  }
}