package io.reco

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol._
import spray.json._

final case class TokenResult(access_token: String, token_type: String, expires_in: String)

object TokenResultJsonSupport {
  implicit val tokenFormat = jsonFormat3(TokenResult)
  implicit val tokenFormatOpt = jsonFormat1(Option[TokenResult])
}

class Oauth(conf: Conf) {
  import TokenResultJsonSupport._
  private val AUTH_URI = "/v2.9/dialog/oauth"
  private val AUTH_TOKEN_URI = "/v2.9/oauth/access_token"
  private val params = Map(("client_id", conf.fbId),
    ("redirect_uri", "http://localhost:8080/callback"))
  val uri = Uri(AUTH_URI).withHost(conf.fbUrl).withQuery(Query(params))

  def getToken(code: String)(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext): Future[String] = {
    val token_prams = params ++  Map(
      ("client_secret", conf.fbSecret),
      ("code", code))
    val httpRequest = HttpRequest(uri = Uri(AUTH_TOKEN_URI).withHost("graph.facebook.com").withQuery(Query(token_prams)))
    Http().singleRequest(httpRequest.copy(uri = s"https:${httpRequest.uri}")).flatMap {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        Unmarshal(entity).to[String]
      case r =>
        println(s"Unexpected status code ${r.status}")
        Future.successful(s"Unexpected status code ${r.status}")
    }.recover {
      case th => println(s"An error has accured $th")
        s"An error has accured $th"
    }
  }
}

object Oauth {
  def apply(conf: Conf): Oauth = new Oauth(conf)
}