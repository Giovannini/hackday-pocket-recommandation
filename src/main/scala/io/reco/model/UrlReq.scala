package io.reco.model

import spray.json._
import spray.json.DefaultJsonProtocol._

case class UrlReq(url: String)

object UrlReq {
  implicit val urlRequestFormat: RootJsonFormat[UrlReq] = jsonFormat1[String, UrlReq](UrlReq.apply)
}
