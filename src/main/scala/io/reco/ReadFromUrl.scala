package io.reco

import de.jetwick.snacktory.{HtmlFetcher, JResult}

object ReadFromUrl {

  private final val fetcher = new HtmlFetcher()
  private final val timeout = 5000

  def extractTextFromUrl(url: String): String = {
    if (url.isEmpty) {
      ""
    } else {
      val parsingResult: JResult = fetcher.fetchAndExtract(url, timeout, true)
      parsingResult.getText
    }
  }
}
