package io.reco.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.reco.ReadFromUrl
import io.reco.model.UrlReq

import scala.concurrent.ExecutionContext

object Reader {

  def route(f: ((String, Seq[String]) => Route))(implicit ec:ExecutionContext, system: ActorSystem, mat: Materializer): Route = {
    entity(as[UrlReq]) { url: UrlReq =>
      onComplete(ReadFromUrl.extractTextFromUrl(url.url)) {
        case util.Success(s) => s match {
          case Right((text, keywords)) =>
            f(text, keywords)
          case Left(error: String) => complete(
            HttpResponse(
              status = StatusCodes.BadRequest,
              entity = HttpEntity.apply(error)
            )
          )
        }
        case util.Failure(_) => complete(
          HttpResponse(
            status = StatusCodes.BadRequest,
            entity = HttpEntity.apply("")
          )
        )
      }

    }
  }
}
