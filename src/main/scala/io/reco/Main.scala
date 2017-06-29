package io.reco

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}

object Main extends App {
  val client: Service[http.Request, http.Response] = Http.newService("www.scala-lang.org:80")
  val request = http.Request(http.Method.Get, "/")
  request.host = "www.scala-lang.org"

  val service = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = client(request)
  }
  val server = Http.serve(":8080", service)
  Await.ready(server)
}
