package io.reco

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol._

import spray.json._
import DefaultJsonProtocol._

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
          println("boumboum")
          println(url)
          complete(ReadFromUrl.extractTextFromUrl(url.url))
        }
      }
    } ~ path("toto") {
      get {
        val formData = FormData(
          Map(
            "key" -> "36ab8f8c673d1c40064a75e19efb5ace",
            "of" -> "json",
            "lang" -> "en",
            "txt" -> ArticleExample.value,
            "tt" -> "a",
            "uw" -> "n"
          )
        )
        onComplete(for {
         request <- Marshal(formData).to[RequestEntity]
         response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://api.meaningcloud.com/topics-2.0", entity = request))
         entity <- Unmarshal(response.entity).to[String]
        } yield entity) {
          case util.Success(f) => complete(f.parseJson)
          case util.Failure(e) => complete(StatusCodes.InternalServerError)
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
