package io.reco

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import io.reco.model.Entity
import spray.json.{RootJsonFormat, _}
import spray.json.DefaultJsonProtocol._


class FriendsLikes(conf: Conf)(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext) {
  val URI_LIKES = "/v2.9/me"
  private val params = Map(("fields", "friends{likes{name},first_name,last_name}"), ("access_token", conf.token))
  val uri: Uri = Uri(URI_LIKES).withHost("graph.facebook.com").withQuery(Query(params))

  case class Data[A](data: Seq[A])

  implicit def dataFormat[A](implicit a: RootJsonFormat[A]): RootJsonFormat[Data[A]] = jsonFormat1(Data[A])

  case class Like(name: String)

  implicit val likeFormat: RootJsonFormat[Like] = jsonFormat1(Like)

  case class Friend(first_name: String, last_name: String, likes: Option[Data[Like]])

  implicit val friendFormat: RootJsonFormat[Friend] = jsonFormat3(Friend)

  case class Cursors(before: Option[String], after: Option[String])

  implicit val cursorsFormat: RootJsonFormat[Cursors] = jsonFormat2(Cursors)

  case class Paging(cursors: Cursors)

  implicit val pagingFormat: RootJsonFormat[Paging] = jsonFormat1(Paging)

  case class Response(friends: Data[Friend])

  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat1(Response)

  def sendRequestToFacebook(filter: Seq[Entity]): Future[Seq[String]] = {
    val httpRequest = HttpRequest(uri = uri)
    val url = s"https:${httpRequest.uri}"
    println(s"URL: $url")
    Http().singleRequest(httpRequest.copy(uri = url))
      .flatMap { response =>
        Unmarshal(response.entity).to[String].map { jsonString =>
          responseFormat.read(jsonString.parseJson)
//            .asJsObject
//            .getFields("friends") match {
//            case Seq(jsObj: JsObject) => jsObj.getFields("data") match {
//              case Seq(JsArray(friends)) => friends.map { friend =>
//                friend.asJsObject.getFields("likes", "first_name", "last_name") match {
//                  case Seq(friendLikes : JsObject, JsString(firstName), JsString(lastName)) =>
//                    Friend(firstName, lastName, Data(
//                    friendLikes.getFields("data") match {
//                      case Seq(JsArray(likeData)) =>
//                        likeData.map { like =>
//                          like.asJsObject.getFields("name") match {
//                            case Seq(JsString(name)) => Like(name)
//                          }
//                        }
//                    }))
//                }
//              }
//            }
//          }
        }
      }.map(_.friends.data.filter(_.likes.getOrElse(Data(Nil)).data.exists(like => filter.exists { entity =>
      entity.word.contains(like.name) || like.name.contains(entity.word)
    })).map(f => s"${f.first_name} ${f.last_name}"))
  }

  def getFriendsLiking(filter: Seq[Entity]): Future[JsValue] = {
    for {
      response <- sendRequestToFacebook(filter)
    } yield {
      JsObject(
        "friends" -> JsArray(response.toVector.map(JsString(_)))
      )
    }
  }

}

object FriendsLikes {
  def apply(conf: Conf)(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext): FriendsLikes = new FriendsLikes(conf)
}