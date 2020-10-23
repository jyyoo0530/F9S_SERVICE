package controllers.stats.get

import java.util.concurrent.TimeUnit

import conf.MongoConf
import javax.inject.Inject
import org.reactivestreams.Subscription
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import org.mongodb.scala.{Document, Observable}

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import org.mongodb.scala._


class IDX_LST @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Observer Pattern
  def responseAction: Action[AnyContent] = Action {

    // Create Observable Object
    val observable: Observable[Document] = MongoConf.database.getCollection("F9S_IDX_LST").find()

    // Create Observer Object
    val observer: Observer[Document] = new Observer[Document] {
      override def onSubscribe(subscription: Subscription): Unit = subscription.request(Long.MaxValue)

      override def onNext(result: Document): Unit = println(result.toJson(MongoConf.jsonWriterSettings))

      override def onError(e: Throwable): Unit = println("!!!!!!! Error: " + e.toString)

      override def onComplete(): Unit = println("////////////////////COMPLETE///////////////////////")
    }

    // Make Observer subscribe on Observable
    observable.subscribe(observer)

    // Return result of subscribed contents with 200 OK
    Ok("[" +
      Await.result(observable.toFuture, Duration(10, TimeUnit.SECONDS)).map(x => x.toJson(MongoConf.jsonWriterSettings)).mkString(",")
      + "]"
    )
  }

}
