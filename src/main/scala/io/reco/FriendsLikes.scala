package io.reco

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import spray.json._

import io.reco.model._

class FriendsLikes(conf: Conf)(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext) {
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

  def getLikesAsJsValue: Future[JsValue] = {
    val httpRequest = HttpRequest(uri = uri)
    for {
      response <- Http().singleRequest(httpRequest.copy(uri = s"https:${httpRequest.uri}"))
      entity <- Unmarshal(response.entity).to[String]
    } yield entity.parseJson
  }

  def suggestedFriends(entities: Seq[Entity]): Future[Seq[Data]] = {
    import ImplicitJsonHandler._
    getLikesAsJsValue.map{ jsValue =>
      val item = jsValue.convertTo[Item]
      item.data.filter {
        case Data(_, _, Some(likes)) =>
          likes.data.exists(elem => entities.map(_.word).exists(word => elem.name.contains(word)))
        case _ => false
      }
    }
  }

}
object FriendsLikes {
  def apply(conf: Conf)(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext): FriendsLikes = new FriendsLikes(conf)
}