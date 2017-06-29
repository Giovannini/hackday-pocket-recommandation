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

  def meaningExtractor(implicit
    actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
  ) = new MeaningExtractor

  def route(textFromArticle: String)(implicit
    actorSystem: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
  ): Route = {
    onComplete(meaningExtractor.getEntitiesFromText(textFromArticle)) {
      case util.Success(entities) => complete(JsObject("entities" -> JsArray(entities.map(Entity.entityFormat.write):_*)))
      case util.Failure(_) => complete(StatusCodes.InternalServerError)
    }
  }

}
