package io.reco

import com.typesafe.config.ConfigFactory
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}

object Main extends App {

  val configuration = ConfigFactory.load()
  Conf.loadConfig(configuration)


  val client: Service[http.Request, http.Response] = Http.newService("www.scala-lang.org:80")
  val request = http.Request(http.Method.Post, "/")
  request.host = "www.scala-lang.org"

  val default = "defaultUrl"

  val service = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = {
      val param: String = req.getParam("url", default)

      val content = param match {
        case `default` => "Impossible de parser l'url"
        case other => ReadFromUrl.extractTextFromUrl(other)
      }

      val r = req.response
      r.setStatusCode(200)
      r.setContentString(content)
      Future.value(r)
    }
  }

  val server = Http.serve(":8080", service)
  Await.ready(server)
}
