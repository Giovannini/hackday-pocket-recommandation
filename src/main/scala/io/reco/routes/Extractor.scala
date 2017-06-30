package io.reco.routes

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.reco.MeaningExtractor
import io.reco.model.Entity
import spray.json.{JsObject, _}


object Extractor {

  def meaningExtractor(
    implicit
    actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
  ): MeaningExtractor = {
    new MeaningExtractor
  }

  def route(textFromArticle: String, keywords: Seq[String])(
    implicit
    actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
  ): Route = {
    onComplete(meaningExtractor.getEntitiesFromText(textFromArticle)) {
      case util.Success(entities) => complete(JsObject(
        "entities" -> JsArray(
          entities
            .filter(_.relevance > 55)
            .sortBy(-_.relevance)
            .map(Entity.entityFormat.write): _*
        ),
        "keywords" -> JsArray(keywords.map(JsString(_)).toVector)
      ))
      case util.Failure(_) => complete(StatusCodes.InternalServerError)
    }
  }

  def partialRoute(textFromArticle: String, keywords: Seq[String])(
    implicit
    actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
  ): Route = {
    onComplete(meaningExtractor.getMeaningForText(textFromArticle)) {
      case util.Success(entities) => complete(entities)
      case util.Failure(_) => complete(StatusCodes.InternalServerError)
    }
  }

  def extractTextFromDiffBot(text: JsValue): String = {
    text.asJsObject.getFields("objects") match {
      case Seq(JsArray(objects)) =>
        objects.map{ ob =>
          ob.asJsObject.getFields("text") match {
            case Seq(JsString(a)) => a
          }
        }.mkString("")
    }
  }

  def language(textFromArticle: String)(
    implicit
    actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
  ): Route = onComplete(meaningExtractor.findLanguage(textFromArticle)) {
    case util.Success(response) => complete(response)
    case util.Failure(e) =>
      println("Error in language: " + e.getMessage)
      complete(StatusCodes.InternalServerError)
  }

}
