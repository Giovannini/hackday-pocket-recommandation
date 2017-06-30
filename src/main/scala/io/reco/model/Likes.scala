package io.reco.model

import spray.json.DefaultJsonProtocol._

  case class Like(name: String, id: String, created_time: String)
  case class Cursors(before: String, after: String)
  case class Paging(cursors: Cursors, next: Option[String])
  case class Likes(data: Seq[Like], paging: Paging)
  case class Summary(total_count: Int)

  case class Data(id: String, name: String, likes: Option[Likes])
  case class Item(data: Seq[Data], paging: Paging, summary: Summary)

object ImplicitJsonHandler {
  implicit val likeFormat = jsonFormat3(Like)
  implicit val cursorFormat = jsonFormat2(Cursors)
  implicit val pagingFormat = jsonFormat2(Paging)
  implicit val summaryFormat = jsonFormat1(Summary)
  implicit val likesFormat = jsonFormat2(Likes)
  //implicit val likesOptFmt = jsonFormat1(Option[Likes])
  implicit val dataFormat = jsonFormat3(Data)
  implicit val ItemFormat = jsonFormat3(Item)
}

