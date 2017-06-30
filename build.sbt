name := "pocketreco"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.9" ,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  //Dependencies for snacktory to parse page and take back content
  "org.log4s" %% "log4s" % "1.3.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",
  "org.jsoup" % "jsoup" % "1.7.2"
)
fork in run := true
cancelable in Global := true

enablePlugins(JavaAppPackaging)
