package controllers.stats.post

import java.util.concurrent.TimeUnit

import scala.io.Source
import conf.MongoConf
import javax.inject.Inject
import org.reactivestreams.Subscription
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import org.mongodb.scala.{Document, Observable}

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.{and, equal}
import play.api.libs.json.{JsObject, JsValue, Json}
import services.ChartHelper

import scala.collection.mutable.ListBuffer

class MI_SUM @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  // Observer Pattern
  def postResponse: Action[AnyContent] = Action { request =>

    val json = request.body.asJson.get
    val idxSubject = (json \ "idxSubject").as[String]
    val idxCategory = (json \ "idxCategory").as[String]
    val idxCd = (json \ "idxCd").as[String]
    val interval = (json \ "interval").as[String]
    val query = and(
      equal("idxSubject", idxSubject),
      equal("idxCategory", idxCategory),
      equal("idxCd", idxCd),
      equal("interval", interval)
    )

    // Create Observable Object
    val observable: Observable[Document] =
      MongoConf.database
        .getCollection("F9S_MI_SUM")
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
    val mongoRaw = Await
      .result(observable.toFuture, Duration(10, TimeUnit.SECONDS))
      .map(x => x.toJson(MongoConf.jsonWriterSettings))

    // Read xAxis indices from the CSV file
    val rootPath = new java.io.File(".").getCanonicalPath + "/app/resources/timetables/"

    val dayTable = Source.fromFile(rootPath + "daytable.csv")("UTF-8").getLines.toArray.map(_.trim)
    val dayIdx = dayTable(0).split(",")
    val dayXAxis = dayTable(1).split(",")
    val dayTimestamp = dayTable(2).split(",")

    val wkTable = Source.fromFile(rootPath + "weektable.csv")("UTF-8").getLines.toArray.map(_.trim)
    val wkIdx = wkTable(0).split(",")
    val wkXAxis = wkTable(1).split(",")
    val wkTimestamp = wkTable(2).split(",")

    val mnTable = Source.fromFile(rootPath + "monthtable.csv")("UTF-8").getLines.toArray.map(_.trim)
    val mnIdx = mnTable(0).split(",")
    val mnXAxis = mnTable(1).split(",")
    val mnTimestamp = mnTable(2).split(",")

    val yrTable = Source.fromFile(rootPath + "yeartable.csv")("UTF-8").getLines.toArray.map(_.trim)
    val yrIdx = yrTable(0).split(",")
    val yrXAxis = yrTable(1).split(",")
    val yrTimestamp = yrTable(2).split(",")

    // Run data refinery before dispatch
    val mongoJsValue: JsValue = Json.parse(mongoRaw.head)
    var mongoJsObject: JsObject = mongoJsValue.as[JsObject]
    val cell = (mongoJsObject \ "Cell").as[List[JsValue]]
    val mongoInterval: String = (mongoJsObject \ "interval").as[String]
    var mongoXAxis = ListBuffer[String]()
    for (i <- cell.indices) mongoXAxis.append((cell(i) \ "xAxis").as[String])

    var newCell = ListBuffer[JsObject]()
    var freqXAxis = Array[String]()
    val wkDetailHelper = new ChartHelper
    mongoInterval match {
      case "monthly" => freqXAxis = mnXAxis
        newCell = (wkDetailHelper.generateAxis(mongoInterval, freqXAxis, mongoXAxis, cell, "F9S_MI_SUM"))
      case "weekly" => freqXAxis = wkXAxis
        newCell = (wkDetailHelper.generateAxis(mongoInterval, freqXAxis, mongoXAxis, cell, "F9S_MI_SUM"))
      case "daily" => freqXAxis = dayXAxis
        newCell = (wkDetailHelper.generateAxis(mongoInterval, freqXAxis, mongoXAxis, cell, "F9S_MI_SUM"))
      case "yearly" => freqXAxis = yrXAxis
        newCell = (wkDetailHelper.generateAxis(mongoInterval, freqXAxis, mongoXAxis, cell, "F9S_MI_SUM"))
    }

    // Return result of subscribed contents with 200 OK
    mongoJsObject = mongoJsObject ++ Json.obj("Cell" -> newCell)
    Ok(mongoJsObject)


    //        Ok(dayTable)
  }
}
