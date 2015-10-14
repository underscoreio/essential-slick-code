import models._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import models.ChatSchema._

import play.api.Application
import play.api.GlobalSettings

object Global extends GlobalSettings {

  //When the application starts up, populate the schema.
  override def onStart(app: Application):Unit = {
    Await.result(Schema.populate, Duration.Inf)
  }

}
