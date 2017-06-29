name := "pocketreco"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "com.twitter" %% "finagle-http" % "6.44.0",
  //Dependencies for snacktory to parse page and take back content
  "org.log4s" %% "log4s" % "1.3.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",
  "org.jsoup" % "jsoup" % "1.7.2"
)
fork in run := true