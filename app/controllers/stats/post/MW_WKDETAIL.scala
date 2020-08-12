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
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue, Json}

import scala.collection.mutable.ListBuffer
import scala.io.Source

class MW_WKDETAIL @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
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
    val interval = (json \ "interval").as[String]
    val query = and(
      equal("containerTypeCode", containerTypeCode),
      equal("marketTypeCode", marketTypeCode),
      equal("paymentTermCode", paymentTermCode),
      equal("polCode", polCode),
      equal("podCode", podCode),
      equal("rdTermCode", rdTermCode),
      equal("baseYearWeek", baseYearWeek),
      equal("interval", interval)
    )

    // Create Observable Object
    val observable: Observable[Document] =
      MongoConf.database
        .getCollection("F9S_MW_WKDETAIL")
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

    val dayTable = Source.fromFile(rootPath + "daytable.csv").getLines.toArray.map(_.trim)
    val dayIdx = dayTable(0).split(",")
    val dayXAxis = dayTable(1).split(",")
    val dayTimestamp = dayTable(2).split(",")

    val wkTable = Source.fromFile(rootPath + "weektable.csv").getLines.toArray.map(_.trim)
    val wkIdx = wkTable(0).split(",")
    val wkXAxis = wkTable(1).split(",")
    val wkTimestamp = wkTable(2).split(",")

    val mnTable = Source.fromFile(rootPath + "monthtable.csv").getLines.toArray.map(_.trim)
    val mnIdx = mnTable(0).split(",")
    val mnXAxis = mnTable(1).split(",")
    val mnTimestamp = mnTable(2).split(",")

    // Run data refinery before dispatch
    val mongoJsValue = Json.parse(mongoRaw.head)
    var mongoJsObject = mongoJsValue.as[JsObject]
    val cell = (mongoJsObject \ "Cell").as[List[JsValue]]
    val mongoInterval = (mongoJsObject \ "interval").as[String]
    var mongoXAxis = ListBuffer[String]()
    for (i <- cell.indices) mongoXAxis.append((cell(i) \ "xAxis").as[String])

    var newCell = ListBuffer[JsObject]()
    var freqXAxis = Array[String]()
    mongoInterval match {
      case "monthly" => freqXAxis = mnXAxis
        newCell = (cellGenerator(freqXAxis, mongoXAxis, cell))
      case "weekly" => freqXAxis = wkXAxis
        newCell = (cellGenerator(freqXAxis, mongoXAxis, cell))
      case "daily" => freqXAxis = dayXAxis
        newCell = (cellGenerator(freqXAxis, mongoXAxis, cell))
        }

    def cellGenerator(freqXAxis: Array[String], mongoXAxis: ListBuffer[String], existingCell: scala.List[JsValue]): ListBuffer[JsObject] = {
      val newCell = ListBuffer[JsObject]()
      val tgXAxis = freqXAxis.slice(freqXAxis.indexOf(mongoXAxis.min), freqXAxis.indexOf("20200811"))
      var mongoPos = 0
      for (i <- tgXAxis) {
        var xAxis = JsObject.empty
        var intervalTimestamp = JsObject.empty
        var open = JsObject.empty
        var low = JsObject.empty
        var high = JsObject.empty
        var close = JsObject.empty
        var volume = JsObject.empty
        var changeValue = JsObject.empty
        var changeRate = JsObject.empty
        xAxis = Json.obj("xAxis" -> JsString(i))
        if (mongoXAxis.contains(i)) {
          mongoPos = mongoXAxis.indexOf(i)
          intervalTimestamp = Json.obj("intervalTimestamp" -> (existingCell(mongoPos) \ "intervalTimestamp").as[JsString])
          open = Json.obj("open" -> (existingCell(mongoPos) \ "open").as[JsNumber])
          low = Json.obj("low" -> (existingCell(mongoPos) \ "low").as[JsNumber])
          high = Json.obj("high" -> (existingCell(mongoPos) \ "high").as[JsNumber])
          close = Json.obj("close" -> (existingCell(mongoPos) \ "close").as[JsNumber])
          volume = Json.obj("volume" -> (existingCell(mongoPos) \ "volume").as[JsNumber])
          changeValue = Json.obj("changeValue" -> (existingCell(mongoPos) \ "changeValue").as[JsNumber])
          changeRate = Json.obj("changeRate" -> (existingCell(mongoPos) \ "changeRate").as[JsNumber])
          mongoPos += 1
        }
        else {
          intervalTimestamp = Json.obj("intervalTimestamp" -> JsString(i + "010000000000"))
          open = Json.obj("open" -> (existingCell(mongoPos - 1) \ "close").as[JsNumber])
          low = Json.obj("low" -> (existingCell(mongoPos - 1) \ "close").as[JsNumber])
          high = Json.obj("high" -> (existingCell(mongoPos - 1) \ "close").as[JsNumber])
          close = Json.obj("close" -> (existingCell(mongoPos - 1) \ "close").as[JsNumber])
          volume = Json.obj("volume" -> JsNumber(0))
          changeValue = Json.obj("changeValue" -> JsNumber(0))
          changeRate = Json.obj("changeRate" -> JsNumber(0))
        }
        newCell.append((xAxis ++ intervalTimestamp ++ open ++ low ++ high ++ close ++ volume ++ changeValue ++ changeRate))
      }
      newCell
    }
    // Return result of subscribed contents with 200 OK
    //    Ok(Await
    //      .result(observable.toFuture, Duration(10, TimeUnit.SECONDS))
    //      .map(x => x.toJson(MongoConf.jsonWriterSettings))
    //      .mkString(",")
    //    )
    Ok("[" + newCell.mkString(",") + "]")
  }

}
