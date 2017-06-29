package io.reco.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.server.Route
import io.reco.ReadFromUrl
import io.reco.model.UrlReq

object Reader {

  val route: (String => Route) => Route = f => {
    entity(as[UrlReq]) { url =>
      f(ReadFromUrl.extractTextFromUrl(url.url))
    }
  }

}
