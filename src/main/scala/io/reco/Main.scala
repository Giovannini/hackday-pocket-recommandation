package io.reco

import scala.collection.immutable
import scala.concurrent.Future
import scala.util.control.NonFatal

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.Directives.{path, _}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.ConfigFactory
import io.reco.routes.{Extractor, Reader}
import akka.http.scaladsl.model.StatusCodes

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
  val conf = Conf.loadConfig(configuration)

  val route =
    path("url") {
      post(Reader.route((a, _) => complete(a)))
    } ~ path("toto") {
      get(Extractor.route(ArticleExample.value, Nil))
    } ~ path("recommand") {
      post {
        extractRequest { request =>
          val vlsOrigin: String = request.headers.find(h => h.name() == "Origin").map(_.value()).getOrElse("")
          val headers: immutable.Seq[HttpHeader] = collection.immutable.Seq(
            HttpHeader.parse("Access-Control-Allow-Origin", vlsOrigin),
            HttpHeader.parse("Access-Control-Allow-Credentials", "true"),
            HttpHeader.parse("Access-Control-Max-Age", "1728000"),
            HttpHeader.parse("Cache-Control", "no-cache")
          ).collect { case ParsingResult.Ok(h, _) => h }
          respondWithHeaders(headers) {
            Reader.route(Extractor.route)
          }
        }
      } ~
        options {
          extractRequest { request =>
            val vlsOrigin: String = request.headers.find(h => h.name() == "Origin").map(_.value()).getOrElse("")
            val headers: immutable.Seq[HttpHeader] = collection.immutable.Seq(
              HttpHeader.parse("Access-Control-Allow-Origin", "*"),
              HttpHeader.parse("Access-Control-Allow-Methods", "POST, OPTIONS"),
              HttpHeader.parse("Access-Control-Allow-Headers", "accept, content-type, access-control-allow-origin"),
              HttpHeader.parse("Access-Control-Max-Age", "1728000")
            ).collect { case ParsingResult.Ok(h, _) => h }
            respondWithHeaders(headers) {
              complete(StatusCodes.OK)
            }
          }
        }
    } ~ path("partialrecommand") {
      post(Reader.route(Extractor.partialRoute))
    } ~ path("login") {
      redirect(Oauth(conf.get).uri, StatusCodes.PermanentRedirect)
    } ~ path("language") {
      post(Reader.route { case (a, _) =>
        println(a.take(50))
        Extractor.language(a)
      })
    } ~ path("callback") {
      extractUri {
        import TokenResultJsonSupport._
        uri =>
          val code = uri.query().get("code")
          val likes = for {
            conf <- Future.fromTry(conf)
            token <- Oauth(conf).getToken(code.get)
            likes <- FriendsLikes(conf).getLikes
          } yield likes
          complete(likes)
      }
    } ~ path("")(complete("Le serveur est ON."))

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}
