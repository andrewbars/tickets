import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{ Session, SessionFactory }
import play.api.db.DB
import play.api.libs.concurrent.Akka
import scala.concurrent.Future
import play.api.{ Application, GlobalSettings }
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import akka.actor.{ Actor, Props }
import models.{ Booking, Event }
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    SessionFactory.concreteFactory = Some(() =>
      Session.create(DB.getConnection()(app), new MySQLAdapter))

    import scala.concurrent.duration._
    import play.api.Play.current
    val actor = Akka.system.actorOf(
      Props(new bookingExpireActor))
    Akka.system.scheduler.schedule(0.seconds, 1.minutes, actor, "check")
  }
  
  override def onBadRequest(request:RequestHeader, error:String)={
    play.api.Logger.warn(error)
    Future.successful(BadRequest(views.html.badRequest(request.flash)))
  }
  
  override def onHandlerNotFound(request:RequestHeader)={
    Future.successful(NotFound(views.html.notFound(request.flash)))
  }
}

class bookingExpireActor extends Actor {
  def receive = {
    case "check" => Booking.expire
    case _ => play.api.Logger.warn("bookingExpireActor: Unsupported message!")
  }
}