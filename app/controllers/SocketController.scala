package controllers

import java.net.URI

import javax.inject._
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * A very simple chat client using websockets.
 */
@Singleton
class SocketController @Inject()(val controllerComponents: ControllerComponents)
                                (implicit actorSystem: ActorSystem,
                                 mat: Materializer,
                                 executionContext: ExecutionContext)
  extends BaseController {

  // many clients -> merge hub -> broadcasthub -> many clients
  private val (msgSink, msgSource) = {
    val source = MergeHub.source[String]
      .log("source")

    val sink = BroadcastHub.sink[String]

    source.toMat(sink)(Keep.both).run()
  }
  private val userFlow: Flow[String, String, _] = {
    Flow.fromSinkAndSource(msgSink, msgSource)
  }
  def msgRoom: Action[AnyContent] = Action { implicit request: RequestHeader =>
    Ok(views.html.wstest("WSTEST"))
  }
  def msg(): WebSocket = WebSocket.accept[String, String] { request =>
    userFlow
  }

}

