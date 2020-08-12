package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}


class HomeController @Inject() (cc:ControllerComponents) extends AbstractController(cc){
  def index: Action[AnyContent] =Action{
    Ok(views.html.index("Hello, World!"))
  }
}