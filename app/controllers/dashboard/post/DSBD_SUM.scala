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
import play.api.libs.json._
import models._

import scala.collection.mutable.ListBuffer

class DSBD_SUM @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  // Observer Pattern
  def postResponse: Action[AnyContent] = Action { request =>

    val json = request.body.asJson.get
    val userId = (json \ "userId").as[String]
    val offerTypeCode = (json \ "offerTypeCode").as[String]
    var polCode = (json \ "polCode").as[String]
    var podCode = (json \ "podCode").as[String]
    var baseYearWeek = (json \ "baseYearWeek").as[String]

    var wkQuery = equal("cell.lineItem.baseYearWeek", baseYearWeek)
    if (polCode == "") polCode = "all"
    if (podCode == "") podCode = "all"
    if (baseYearWeek == "") baseYearWeek = "all"


    val query =
      and(
        equal("userId", userId),
        equal("offerTypeCode", offerTypeCode)
      )

    // Create Observable Object
    val observable: Observable[Document] =
      MongoConf.database
        .getCollection("F9S_DSBD_SUM")
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

    // Get result from subscription
    val mongoRaw = (Await
      .result(observable.toFuture, Duration(10, TimeUnit.SECONDS))
      .map(x => x.toJson(MongoConf.jsonWriterSettings)))


    // Run data refinery before dispatch
    val mongoJsValue = Json.parse(mongoRaw.head)
    var mongoJsObject = mongoJsValue.as[JsObject]
    val cell = (mongoJsObject \ "cell").as[List[JsObject]]

    val newCell = ListBuffer[JsObject]()
    for (i <- cell.indices) {
      val loopIdx = (cell(i) \ "routeItem").as[List[JsObject]]
      if (polCode == "all" && podCode == "all") newCell.append(cell(i)
        + ("headPolCode" -> ((cell(i) \ "routeItem") (0) \ "polCode").as[JsString])
        + ("headPolName" -> ((cell(i) \ "routeItem") (0) \ "polName").as[JsString])
        + ("headPodCode" -> ((cell(i) \ "routeItem") (0) \ "podCode").as[JsString])
        + ("headPodName" -> ((cell(i) \ "routeItem") (0) \ "podName").as[JsString])
      )
      if (polCode == "all" && podCode != "all")
        if (loopIdx.exists(x => x.toString.contains(podCode))) newCell.append(cell(i)
          + ("headPolCode" -> ((cell(i) \ "routeItem") (0) \ "polCode").as[JsString])
          + ("headPolName" -> ((cell(i) \ "routeItem") (0) \ "polName").as[JsString])
          + ("headPodCode" -> ((cell(i) \ "routeItem") (0) \ "podCode").as[JsString])
          + ("headPodName" -> ((cell(i) \ "routeItem") (0) \ "podName").as[JsString]))
      if (polCode != "all" && podCode == "all")
        if (loopIdx.exists(x => x.toString.contains(polCode))) newCell.append(cell(i)
          + ("headPolCode" -> ((cell(i) \ "routeItem") (0) \ "polCode").as[JsString])
          + ("headPolName" -> ((cell(i) \ "routeItem") (0) \ "polName").as[JsString])
          + ("headPodCode" -> ((cell(i) \ "routeItem") (0) \ "podCode").as[JsString])
          + ("headPodName" -> ((cell(i) \ "routeItem") (0) \ "podName").as[JsString]))
      if (polCode != "all" && podCode != "all")
        if (loopIdx.exists(x => x.toString.contains(polCode))
          && loopIdx.exists(x => x.toString.contains(podCode))) newCell.append(cell(i)
          + ("headPolCode" -> ((cell(i) \ "routeItem") (0) \ "polCode").as[JsString])
          + ("headPolName" -> ((cell(i) \ "routeItem") (0) \ "polName").as[JsString])
          + ("headPodCode" -> ((cell(i) \ "routeItem") (0) \ "podCode").as[JsString])
          + ("headPodName" -> ((cell(i) \ "routeItem") (0) \ "podName").as[JsString]))
    }

    val newCell2 = ListBuffer[JsObject]()
    for (i <- newCell.indices) {
      val loopIdx = (newCell(i) \ "lineItem").as[List[JsObject]]
      baseYearWeek match {
        case "all" =>
          System.out.println("!!!!Nothing to be filtered for baseYearWeek!!!!")
          val aggDealQty = loopIdx.map(x => (x \ "dealQty").as[Int]).sum
          val aggLeftQty = loopIdx.map(x => (x \ "leftQty").as[Int]).sum
          newCell(i) = newCell(i) + ("aggDealQty" -> JsNumber(aggDealQty))
          newCell(i) = newCell(i) + ("aggLeftQty" -> JsNumber(aggLeftQty))
          newCell2.append(newCell(i))
        case _ => if (loopIdx.exists(x => x.toString.contains(baseYearWeek))) {
          val aggDealQty = loopIdx.map(x => (x \ "dealQty").as[Int]).sum
          val aggLeftQty = loopIdx.map(x => (x \ "leftQty").as[Int]).sum
          newCell(i) = newCell(i) + ("aggDealQty" -> JsNumber(aggDealQty))
          newCell(i) = newCell(i) + ("aggLeftQty" -> JsNumber(aggLeftQty))
          newCell2.append(newCell(i))
        }
      }
    }

    mongoJsObject = mongoJsObject - "cell"
    mongoJsObject = mongoJsObject + ("cell" -> Json.toJson(newCell2).as[JsValue])
    val closeCount = (mongoJsObject \ "cell").as[List[JsValue]].count(x => (x \ "offerStatus").as[String].equals("0"))
    val openCount = (mongoJsObject \ "cell").as[List[JsValue]].count(x => (x \ "offerStatus").as[String].equals("1"))
    mongoJsObject = mongoJsObject + ("closedStsCount" -> JsNumber(closeCount))
    mongoJsObject = mongoJsObject + ("openStsCount" -> JsNumber(openCount))

    // Return result of subscribed contents with 200 OK
    Ok(mongoJsObject.toString)
  }
}
