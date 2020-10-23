package services

import java.util.Calendar

import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue, Json}

import scala.collection.mutable.ListBuffer
import scala.io.Source

class ChartHelper {
  def generateAxis(mongoInterval: String, freqXAxis: Array[String], mongoXAxis: ListBuffer[String], existingCell: scala.List[JsValue], schemaModel: String): ListBuffer[JsObject] = {
    val newCell = ListBuffer[JsObject]()
    val now: Calendar = Calendar.getInstance()
    val maxSlice = mongoInterval match {
      case "daily" => (now.get(Calendar.YEAR) * 10000 + (now.get(Calendar.MONTH) + 1) * 100 + now.get(Calendar.DAY_OF_MONTH)).toString
      case "weekly" => (now.get(Calendar.YEAR) * 100 +now.get(Calendar.WEEK_OF_YEAR) * 100 ).toString
      case "monthly" => (now.get(Calendar.YEAR) * 100 + (now.get(Calendar.MONTH) + 1)).toString
      case "yearly" => (now.get(Calendar.YEAR)).toString
    }
    val test = mongoXAxis.min
    val tgXAxis = freqXAxis.slice(freqXAxis.indexOf(mongoXAxis.min), freqXAxis.indexOf(maxSlice))
    var mongoPos = 0
    schemaModel match {
      case "F9S_MW_WKDETAIL" =>
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
      case "F9S_MI_SUM" =>
        for (i <- tgXAxis) {
          var xAxis = JsObject.empty
          var intervalTimestamp = JsObject.empty
          var value = JsObject.empty
          var volume = JsObject.empty
          var changeValue = JsObject.empty
          var changeRate = JsObject.empty
          xAxis = Json.obj("xAxis" -> JsString(i))
          if (mongoXAxis.contains(i)) {
            mongoPos = mongoXAxis.indexOf(i)
            intervalTimestamp = Json.obj("intervalTimestamp" -> (existingCell(mongoPos) \ "intervalTimestamp").as[JsString])
            value = Json.obj("value" -> (existingCell(mongoPos) \ "value").as[JsNumber])
            volume = Json.obj("volume" -> (existingCell(mongoPos) \ "volume").as[JsNumber])
            changeValue = Json.obj("changeValue" -> (existingCell(mongoPos) \ "changeValue").as[JsNumber])
            changeRate = Json.obj("changeRate" -> (existingCell(mongoPos) \ "changeRate").as[JsNumber])
            mongoPos += 1
          }
          else {
            intervalTimestamp = Json.obj("intervalTimestamp" -> JsString(i + "010000000000"))
            value = Json.obj("value" -> (existingCell(mongoPos - 1) \ "value").as[JsNumber])
            volume = Json.obj("volume" -> JsNumber(0))
            changeValue = Json.obj("changeValue" -> JsNumber(0))
            changeRate = Json.obj("changeRate" -> JsNumber(0))
          }
          newCell.append(xAxis ++ intervalTimestamp ++ value ++ volume ++ changeValue ++ changeRate)
        }
        newCell
    }

  }

}
