package conf

//import reactivemongo.api._
import org.bson.json.{JsonMode, JsonWriterSettings}
import org.mongodb.scala.{MongoClient, MongoDatabase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoConf{
    private val uri: String = "mongodb://data.freight9.com:27017"
    System.setProperty("org.mongodb.async.type", "netty")
    private val mongoClient:MongoClient = MongoClient(uri)
    val database: MongoDatabase = mongoClient.getDatabase("f9s")

    def closeConn(): Unit = mongoClient.close()

    val jsonWriterSettings: JsonWriterSettings = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()

}


//import org.mongodb.scala.{MongoClient, MongoDatabase}

//object mongoConf{
//  private val uri: String = "mongodb://data.freight9.com:27017"
//  System.setProperty("org.mongodb.async.type", "netty")
//  private val mongoClient:MongoClient = MongoClient(uri)
//  val database: MongoDatabase = mongoClient.getDatabase("f9s")
//}


//val uri = "mongodb://data.freight9.com:27017/f9s"
//val driver = new AsyncDriver
//
//val database: Future[DB] = for {
//  uri <- MongoConnection.fromString(uri)
//  con <- driver.connect(uri)
//  dn <- Future(uri.db.get)
//  db <- con.database(dn)
//} yield db
//
//  database.onComplete {
//  resolution =>
//  println(s"DB resulotion: $resolution")
//  driver.close()
//}