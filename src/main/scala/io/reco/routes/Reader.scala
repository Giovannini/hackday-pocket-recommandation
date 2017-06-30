package io.reco.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.server.Route
import io.reco.ReadFromUrl
import io.reco.model.UrlReq
import akka.http.scaladsl.server.Directives._

object Reader {

  val route: ((String, Seq[String]) => Route) => Route = f => {
    entity(as[UrlReq]) { url =>
      ReadFromUrl.extractTextFromUrl(url.url) match {
        case Right((text, keywords)) =>
          f(text, keywords)
        case Left(error) => complete(HttpResponse(
          status = StatusCodes.BadRequest,
          entity = HttpEntity.apply(error)
        ))
      }
    }
  }

}
