package controllers


import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}



class WebsocketTester @Inject() (cc:ControllerComponents) extends AbstractController (cc){

  def tester:Action[AnyContent] = Action {
    Ok(views.html.wstest("wsTest"))
  }
}
