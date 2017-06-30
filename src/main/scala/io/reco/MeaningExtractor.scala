package io.reco

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.{HttpHeader, RequestEntity, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.Materializer
import akka.util.ByteString
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
            if (currentWords.contains(e.word.toLowerCase)) acc
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

  def findLanguage(text: String): Future[HttpResponse] = {
    val url = "https://api.rosette.com/rest/v1/language"
    val key = "07c46da2f4dea3f9f50e0bac9b6206b5"

    val json: String = JsObject("content" -> JsString(text)).toString()
    println(json)

    Http().singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      headers = collection.immutable.Seq(
        HttpHeader.parse("X-RosetteAPI-Key", key),
        HttpHeader.parse("Content-Type", "application/json"),
        HttpHeader.parse("Accept", "application/json"),
        HttpHeader.parse("Cache-Control", "no-cache")
      ).collect { case ParsingResult.Ok(h, _) => h },
      entity = HttpEntity.apply(contentType = ContentTypes.`application/json`, data = ByteString(json))
    ))
  }

}