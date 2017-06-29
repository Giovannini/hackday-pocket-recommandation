name := "pocketreco"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.twitter" %% "finagle-http" % "6.44.0"
libraryDependencies += "org.log4s" %% "log4s" % "1.3.5"
libraryDependencies += "org.jsoup" % "jsoup" % "1.7.2"

fork in run := true

