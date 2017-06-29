package io.reco

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

object Main extends App {

  implicit val system = ActorSystem("pocket-reco")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val configuration = ConfigFactory.load()
  Conf.loadConfig(configuration)

  case class UrlReq(url: String)

  implicit val urlRequestFormat = jsonFormat1(UrlReq)

  val route =
    path("url") {
      post {
        entity(as[UrlReq]) { url =>
          println("okok")
          println(url)
          complete(ReadFromUrl.extractTextFromUrl(url.url))
        }
      }
    } ~
      path("") {
        complete("Le serveur est ON.")
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }

}
