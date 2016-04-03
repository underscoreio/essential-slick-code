package actions

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.dbio.DBIOAction
import slick.profile.SqlAction

import slick.driver.H2Driver.api._

object Main {

  // Tables -------------------------------------

  case class Album(
    artist : String,
    title  : String,
    year   : Int,
    rating : Rating,
    id     : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artist = column[String]("artist")
    def title  = column[String]("title")
    def year   = column[Int]("year")
    def rating = column[Rating]("rating")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artist, title, year, rating, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]



  // Schema actions -----------------------------

  val createTableAction: DBIOAction[Unit, NoStream, Effect.Schema] =
    AlbumTable.schema.create

  val dropTableAction: DBIOAction[Unit, NoStream, Effect.Schema] =
    AlbumTable.schema.drop



  // Select actions -----------------------------

  val selectAction: SqlAction[Seq[String], NoStream, Effect.Read] =
    AlbumTable
      .filter(_.artist === "Keyboard Cat")
      .map(_.title)
      .result



  // Update actions -----------------------------

  val updateAction: SqlAction[Int, NoStream, Effect.Write] =
    AlbumTable
      .filter(_.artist === "Keyboard Cat")
      .map(_.title)
      .update("Even Greater Hits")

  val updateAction2: SqlAction[Int, NoStream, Effect.Write] =
    AlbumTable
      .filter(_.artist === "Keyboard Cat")
      .map(a => (a.title, a.year))
      .update(("Even Greater Hits", 2010))



  // Delete actions -----------------------------

  val deleteAction: SqlAction[Int, NoStream, Effect.Write] =
    AlbumTable
      .filter(_.artist === "Justin Bieber")
      .delete



  // Insert actions -----------------------------

  val insertOneAction: SqlAction[Int, NoStream, Effect.Write] =
    AlbumTable += Album("Pink Floyd", "Dark Side of the Moon", 1978, Rating.Awesome )

  val insertAllAction: SqlAction[Option[Int], NoStream, Effect.Write] =
    AlbumTable ++= Seq(
      Album( "Keyboard Cat"  , "Keyboard Cat's Greatest Hits" , 2009 , Rating.Awesome ),
      Album( "Spice Girls"   , "Spice"                        , 1996 , Rating.Good    ),
      Album( "Rick Astley"   , "Whenever You Need Somebody"   , 1987 , Rating.NotBad  ),
      Album( "Manowar"       , "The Triumph of Steel"         , 1992 , Rating.Meh     ),
      Album( "Justin Bieber" , "Believe"                      , 2013 , Rating.Aaargh  ))



  // Exercise actions ---------------------------

  val insertThreeFaves: SqlAction[Option[Int], NoStream, Effect.Write] =
    AlbumTable ++= Seq(
      Album( "Chroma Key" , "Dead Air for Radios"     , 1998 , Rating.Awesome ),
      Album( "Chroma Key" , "You Go Now"              , 2000 , Rating.Awesome ),
      Album( "Chroma Key" , "Graveyard Mountain Home" , 2004 , Rating.Awesome ))

  def updateRecentAlbums(year: Int): SqlAction[Int, NoStream, Effect.Write] =
    AlbumTable
      .filter(_.year >= year)
      .map(_.rating)
      .update(Rating.Good)

  def deleteArtistsAlbums(artist: String): SqlAction[Int, NoStream, Effect.Write] =
    AlbumTable
      .filter(_.artist === artist)
      .delete

  def insertAndRateAutomatically(artist: String, title: String, year: Int): DBIOAction[Seq[Album], NoStream, Effect.All] =
    for {
      existing   <- AlbumTable
                      .filter(a => a.artist === artist && a.year < year)
                      .result
      rating      = existing.length match {
                      case 0 => Rating.Awesome
                      case _ => Rating.Meh
                    }
      _          <- AlbumTable += Album(artist, title, year, rating)
      results    <- AlbumTable
                      .filter(_.artist === artist)
                      .sortBy(_.year.asc)
                      .result
    } yield results



  // Database -----------------------------------

  val db = Database.forConfig("musicdb.db")



  // Let's go! ----------------------------------

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  def main(args: Array[String]): Unit = {
    val everythingAction =
      createTableAction andThen
      insertAllAction andThen
      insertOneAction andThen
      insertThreeFaves andThen
      deleteArtistsAlbums("Justin Bieber") andThen
      insertAndRateAutomatically("Keyboard Cat", "Keyboard Cat's Slightly Less Great Hits", 2010)

    exec(everythingAction.transactionally).foreach(println)
  }

}
