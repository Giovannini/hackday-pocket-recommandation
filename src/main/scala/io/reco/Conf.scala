package io.reco

import scala.util.{Failure, Try}
import com.typesafe.config.Config

case class Conf(
  fbUrl: String
)


object Conf {

  def loadConfig(config: Config): Try[Conf] = {
    val load = for {
      fbUrl <- Try(config.getString("facebook.api-url"))
    } yield Conf(fbUrl)

    load.recoverWith {
      case err =>
        Failure(new Exception(s"Fails to load configuration ${err.getMessage}", err))
    }
  }

}
