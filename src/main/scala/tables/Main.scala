package tables

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.H2Driver.api._

object Main {

  // Tables -------------------------------------

  class Album(
    val artist : String,
    val title  : String,
    val year   : Int,
    val rating : Rating,
    val id     : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artist = column[String]("artist")
    def title  = column[String]("title")
    def year   = column[Int]("year")
    def rating = column[Rating]("rating")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artist, title, year, rating, id) <> ((createAlbum _).tupled, extractAlbum)
  }

  def createAlbum(artist: String, title: String, year: Int, rating: Rating, id: Long = 0L) =
    new Album(artist, title, year, rating, id)

  def extractAlbum(album: Album) =
    Some((album.artist, album.title, album.year, album.rating, album.id))

  lazy val AlbumTable = TableQuery[AlbumTable]



  // Actions ------------------------------------

  val createTableAction =
    AlbumTable.schema.create

  val insertAlbumsAction =
    AlbumTable ++= Seq(
      createAlbum( "Keyboard Cat"  , "Keyboard Cat's Greatest Hits" , 2009 , Rating.Awesome ),
      createAlbum( "Spice Girls"   , "Spice"                        , 1996 , Rating.Good    ),
      createAlbum( "Rick Astley"   , "Whenever You Need Somebody"   , 1987 , Rating.NotBad  ),
      createAlbum( "Manowar"       , "The Triumph of Steel"         , 1992 , Rating.Meh     ),
      createAlbum( "Justin Bieber" , "Believe"                      , 2013 , Rating.Aaargh  ))

  val selectAlbumsAction =
    AlbumTable.result



  // Database -----------------------------------

  val db = Database.forConfig("musicdb.db")



  // Let's go! ----------------------------------

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  def main(args: Array[String]): Unit = {
    exec(createTableAction)
    exec(insertAlbumsAction)
    exec(selectAlbumsAction).foreach(println)
  }

}
