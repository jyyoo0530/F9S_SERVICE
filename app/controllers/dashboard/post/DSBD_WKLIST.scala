package controllers.dashboard.post

import java.util.concurrent.TimeUnit

import conf.MongoConf
import javax.inject.Inject
import org.bson.conversions.Bson
import org.reactivestreams.Subscription
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import org.mongodb.scala.{Document, Observable}

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions
import org.mongodb.scala.model.Filters._

class DSBD_WKLIST @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  // Observer Pattern
  def postResponse: Action[AnyContent] = Action { request =>

    val json = request.body.asJson.get
    val userId = (json \ "userId").as[String]
    val offerTypeCode = (json \ "offerTypeCode").as[String]
    var polCode = (json \ "polCode").as[String]
    var podCode = (json \ "podCode").as[String]
    if (polCode == "") polCode = "all"
    if (podCode == "") podCode = "all"

    val query =
      and(
        equal("userId", userId),
        equal("offerTypeCode", offerTypeCode),
        equal("polCode", polCode),
        equal("podCode", podCode)
      )


    // Create Observable Object
    val observable: Observable[Document] =
      MongoConf.database
        .getCollection("F9S_DSBD_WKLIST")
        .find(query)

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
    Ok(Await
      .result(observable.toFuture, Duration(10, TimeUnit.SECONDS))
      .map(x => x.toJson(MongoConf.jsonWriterSettings))
      .mkString(",")
    )
    //            Ok(baseYearWeek.mkString(","))
  }
}
