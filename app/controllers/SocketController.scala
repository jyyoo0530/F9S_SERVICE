package controllers

import actors.ChatServiceActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}

@Singleton
class SocketController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  def socket: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out => ChatServiceActor.props(out) }
  }
}
