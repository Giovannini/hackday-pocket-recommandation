package io.reco.model

import spray.json._
import spray.json.DefaultJsonProtocol._

case class Entity(word: String, relevance: Int)

object Entity {
  implicit val entityFormat: RootJsonFormat[Entity] = jsonFormat2[String, Int, Entity](Entity.apply)
}
