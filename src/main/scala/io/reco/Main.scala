package io.reco

import scala.concurrent.Future
import scala.util.control.NonFatal

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol._
import spray.json._

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
  implicit val executionContext = system.dispatcher

  val configuration = ConfigFactory.load()
  Conf.loadConfig(configuration)

  case class UrlReq(url: String)

  implicit val urlRequestFormat = jsonFormat1(UrlReq)

  case class Entity(word: String, relevance: Int)

  implicit val entityFormat = jsonFormat2(Entity)

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
        onComplete(getEntitiesFromText(ArticleExample.value)) {
          case util.Success(entities) => complete(JsObject("result" -> entities.toJson))
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

  private def getMeaningForText(text: String, lang: String = "en"): Future[JsValue] = {
    val formData = FormData(
      Map(
        "key" -> "36ab8f8c673d1c40064a75e19efb5ace",
        "of" -> "json",
        "lang" -> lang,
        "txt" -> text,
        "tt" -> "a",
        "uw" -> "n"
      )
    )
    for {
      request <- Marshal(formData).to[RequestEntity]
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://api.meaningcloud" +
        ".com/topics-2.0", entity = request))
      entity <- Unmarshal(response.entity).to[String]
    } yield entity.parseJson
  }

  private def getEntitiesFromText(text: String, lang: String = "en"): Future[Seq[Entity]] = {
    getMeaningForText(text, lang).map { json =>
      json.asJsObject.getFields("entity_list") match {
        case Seq(JsArray(entityList)) => entityList.map {
          _.asJsObject.getFields("relevance", "form") match {
            case Vector(JsString(relevance), JsString(word)) => Entity(word, relevance.toInt)
            case other =>
              println(s"Error, pas d'entity: $other")
              sys.error("Ca casse no1")
          }
        }
        case other =>
          println(s"Error, pas de jsARray: $other")
          sys.error("Ca casse no2")
      }
    }
  }

}
