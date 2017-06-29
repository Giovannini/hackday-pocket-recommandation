name := "pocketreco"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "com.twitter" %% "finagle-http" % "6.44.0"
)

fork in run := true
