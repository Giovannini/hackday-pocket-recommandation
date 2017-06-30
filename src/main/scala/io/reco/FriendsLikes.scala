package io.reco

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer

class FriendsLikes(conf: Conf) {
  val URI_LIKES = "/v2.9/me/friends"
  private val params = Map(("fields", "id,name,likes"),
    ("access_token", conf.token))
  val uri = Uri(URI_LIKES).withHost("graph.facebook.com").withQuery(Query(params))

  def getLikes(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext) = {
    val httpRequest = HttpRequest(uri = uri)
    val response = Http().singleRequest(httpRequest.copy(uri = s"https:${httpRequest.uri}"))
    response
/*      .map {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        entity.dataBytes.via()
      case r =>
    }*/
  }

}
object FriendsLikes {
  def apply(conf: Conf): FriendsLikes = new FriendsLikes(conf)
}