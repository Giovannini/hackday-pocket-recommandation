package io.reco

import scala.util.control.NonFatal

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.ConfigFactory
import io.reco.routes.{Extractor, Reader}

object Main extends App {

  implicit val system = ActorSystem("pocket-reco")
  val decider: Supervision.Decider = {
    case NonFatal(e) =>
      println("Error in stream at the higher level. Stopping the supervision.", e)
      Supervision.Stop
  }
  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )(system)
  // needed for the future flatMap/onComplete in the end
  implicit val ec = system.dispatcher

  val configuration = ConfigFactory.load()
  Conf.loadConfig(configuration)

  val route =
    path("url") {
      post(Reader.route(complete(_)))
    } ~ path("toto") {
      get(Extractor.route(ArticleExample.value))
    } ~ path("recommand") {
      post(Reader.route(Extractor.route))
    } ~ path("")(complete("Le serveur est ON."))

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
