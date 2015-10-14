package controllers

import play.api._
import play.api.mvc._

import java.text.SimpleDateFormat
import javax.inject.Inject
import models._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import ChatSchema._

import play.api.libs.json._


//TODO: Application is already used by Play, this will be confusing to those 
//      who know some play. Better to call it  `ApplicationController`?
object ApplicationController extends Controller {

  //Display index page
  def index = Action { Ok(views.html.index()) }
  
  //Send a stream of messages to the client.
  //Don't construct your JSON via tuples, use case classes and an encoder.
  def messages = Action.async  {
    Schema.msgs.map{s => 
      Ok( 
        JsArray(s.map(t =>Json.obj("sender" -> t._1, "content" -> t._2)))
        )
    }
  }

}
