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

final case class TokenResult(access_token: String, token_type: String, expires_in: Long)

object TokenResultJsonSupport {
  implicit val tokenFormat = jsonFormat3(TokenResult)
}

class Oauth(conf: Conf) {
  private val AUTH_URI = "/v2.9/dialog/oauth"
  private val AUTH_TOKEN_URI = "/v2.9/oauth/access_token"
  private val params = Map(("client_id", conf.fbId),
    ("redirect_uri", "http://localhost:8080/callback"))
  val uri = Uri(AUTH_URI).withHost(conf.fbUrl).withQuery(Query(params))

  def getToken(code: String)(implicit sys: ActorSystem, mat: Materializer, ec: ExecutionContext): Future[JsValue] = {
    val token_prams = params ++  Map(
      ("client_secret", conf.fbSecret),
      ("code", code))
    val httpRequest = HttpRequest(uri = Uri(AUTH_TOKEN_URI).withHost("graph.facebook.com").withQuery(Query(token_prams)))

    for {
      response <- Http().singleRequest(httpRequest.copy(uri = s"https:${httpRequest.uri}"))
      entity <- Unmarshal(response.entity).to[String]
    } yield entity.parseJson
  }
}

object Oauth {
  def apply(conf: Conf): Oauth = new Oauth(conf)
}