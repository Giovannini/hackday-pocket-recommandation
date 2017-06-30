package io.reco

import scala.collection.JavaConverters._
import scala.util.Try

import de.jetwick.snacktory.HtmlFetcher

object ReadFromUrl {

  private final val fetcher = new HtmlFetcher()
  private final val timeout = 5000

  def extractTextFromUrl(url: String): Either[String, (String, Seq[String])] = {
    if (url.isEmpty) {
      Left("An enpty URL was given.")
    } else {
      Try {
        fetcher.fetchAndExtract(url, timeout, true)
      } match {
        case util.Success(parsingResult) => Right((parsingResult.getText, parsingResult.getKeywords.asScala.toSeq))
        case util.Failure(t) => Left(t.getMessage)
      }
    }
  }
}
