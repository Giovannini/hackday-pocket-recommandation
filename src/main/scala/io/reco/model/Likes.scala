package io.reco.model

  case class Like(name: String, id: String, created_time: String)
  case class Cursors(before: String, after: String)
  case class Paging(cursors: Cursors, next: String)
  case class Likes(data: Seq[Like], paging: Paging)

  case class Data(id: String, name: String, likes: Seq[Likes])

