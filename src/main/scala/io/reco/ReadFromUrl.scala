package io.reco

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import de.jetwick.snacktory.{HtmlFetcher, JResult}
import io.reco.routes.Extractor
import spray.json._
import scala.collection.JavaConverters._
import scala.util.Try

import scala.concurrent.{ExecutionContext, Future}

object ReadFromUrl {
  def extractTextFromUrl(url: String)(implicit ec:ExecutionContext, system: ActorSystem, mat: Materializer): Future[Either[String, (String, Seq[String])]] = {
    val textFromSnacktoryRead = SnacktoryRead.extractTextFromUrl(url)
    if (textFromSnacktoryRead.isLeft) {
      DiffBot.extractTextFromUrl(url).map(a => Right(a, Seq.empty[String]))
    } else {
      Future.successful(textFromSnacktoryRead)
    }
  }
}


object SnacktoryRead {
  private final val fetcher = new HtmlFetcher()
  private final val timeout = 5000

  def extractTextFromUrl(url: String): Either[String, (String, Seq[String])] = {
    if (url.isEmpty) {
      Left("An enpty URL was given.")
    } else {
      Try {
        fetcher.fetchAndExtract(url, timeout, true)
      } match {
        case util.Success(parsingResult) => {
          if (parsingResult.getText.isEmpty){
            Left("Impossible de parser le fichier")
          } else{
            Right((parsingResult.getText, parsingResult.getKeywords.asScala.toSeq))
          }
        }
        case util.Failure(t) => Left(t.getMessage)
      }
    }
  }
}

object DiffBot{

  private final val token = "eeef89ca26321ac129b7b0faf728daa4"
  private final val baseUrl = "https://api.diffbot.com/v3/article"

  def extractTextFromUrl(url: String)(implicit ec:ExecutionContext, system: ActorSystem, mat: Materializer): Future[String] = {
    for {
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = s"$baseUrl?token=$token&url=$url" ))
      entity <- Unmarshal(response.entity).to[String]
    } yield Extractor.extractTextFromDiffBot(entity.parseJson)
  }


}