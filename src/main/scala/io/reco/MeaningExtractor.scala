package io.reco

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest, RequestEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import io.reco.model.Entity
import spray.json.{JsArray, JsString, JsValue, _}

class MeaningExtractor(
  implicit
  actorSystem: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
) {

  def getEntitiesFromText(text: String, lang: String = "en"): Future[Seq[Entity]] = {
    getMeaningForText(text, lang).map { json =>
      json.asJsObject.getFields("entity_list", "concept_list") match {
        case Seq(JsArray(entityList), JsArray(conceptList)) =>
          val result = (entityList ++ conceptList).map {
            _.asJsObject.getFields("relevance", "form") match {
              case Vector(JsString(relevance), JsString(word)) => Entity(word, relevance.toInt)
            }
          }
          result.foldLeft(Seq.empty[Entity]) { case (acc, e) =>
            val currentWords = acc.map(_.word.toLowerCase)
            if(currentWords.contains(e.word.toLowerCase)) acc
            else acc :+ e
          }
      }
    }
  }

  def getMeaningForText(text: String, lang: String = "en"): Future[JsValue] = {
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

}