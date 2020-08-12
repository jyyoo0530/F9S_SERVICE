package controllers.stats.post

import java.util.concurrent.TimeUnit

import conf.MongoConf
import javax.inject.Inject
import org.reactivestreams.Subscription
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import org.mongodb.scala.{Document, Observable}

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.{and, equal}

class MW_BIDASK @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  // Observer Pattern
  def postResponse: Action[AnyContent] = Action { request =>

    val json = request.body.asJson.get
    val containerTypeCode = (json \ "containerTypeCode").as[String]
    val marketTypeCode = (json \ "marketTypeCode").as[String]
    val paymentTermCode = (json \ "paymentTermCode").as[String]
    val polCode = (json \ "polCode").as[String]
    val podCode = (json \ "podCode").as[String]
    val rdTermCode = (json \ "rdTermCode").as[String]
    val baseYearWeek = (json \ "baseYearWeek").as[String]
    val offerTypeCode = (json \ "offerTypeCode").as[String]
    val query = and(
      equal("containerTypeCode", containerTypeCode),
      equal("marketTypeCode", marketTypeCode),
      equal("paymentTermCode", paymentTermCode),
      equal("polCode", polCode),
      equal("podCode", podCode),
      equal("rdTermCode", rdTermCode),
      equal("baseYearWeek", baseYearWeek),
      equal("offerTypeCode", offerTypeCode)
    )

    // Create Observable Object
    val observable: Observable[Document] =
      MongoConf.database
        .getCollection("F9S_MW_BIDASK")
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
    //    Ok(test)
  }
}
