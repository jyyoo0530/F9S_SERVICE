name := "F9S_SERVICE"
 
version := "1.0" 
      
lazy val `f9s_service` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  specs2 % Test,
  guice,
  "io.vertx" % "vertx-stomp" % "3.9.2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.5",
  "org.mongodb.scala" %% "mongo-scala-bson" % "4.0.5",
  "io.swagger" %% "swagger-play2" % "1.7.1",
  "org.apache.activemq" % "artemis-server" % "2.14.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
  "org.webjars" %% "webjars-play" % "2.8.0",
  "org.webjars" % "flot" % "0.8.3-1",
  "org.webjars" % "bootstrap" % "3.3.7",
  "org.jsoup" % "jsoup" % "1.12.1"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")