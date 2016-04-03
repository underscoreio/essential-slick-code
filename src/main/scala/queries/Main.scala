package queries

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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



  // Example queries ----------------------------

  val createTableAction =
    AlbumTable.schema.create

  val insertAlbumsAction =
    AlbumTable ++= Seq(
      Album( "Keyboard Cat"  , "Keyboard Cat's Greatest Hits" , 2009 , Rating.Awesome ),
      Album( "Spice Girls"   , "Spice"                        , 1996 , Rating.Good    ),
      Album( "Rick Astley"   , "Whenever You Need Somebody"   , 1987 , Rating.NotBad  ),
      Album( "Manowar"       , "The Triumph of Steel"         , 1992 , Rating.Meh     ),
      Album( "Justin Bieber" , "Believe"                      , 2013 , Rating.Aaargh  ))

  val selectAllQuery: Query[AlbumTable, Album, Seq] =
    AlbumTable

  val selectWhereQuery: Query[AlbumTable, Album, Seq] =
    AlbumTable
      .filter(_.rating === (Rating.Awesome : Rating))

  val selectSortedQuery1: Query[AlbumTable, Album, Seq] =
    AlbumTable
      .sortBy(_.year.asc)

  val selectSortedQuery2: Query[AlbumTable, Album, Seq] =
    AlbumTable
      .sortBy(a => (a.year.asc, a.rating.asc))

  val selectPagedQuery: Query[AlbumTable, Album, Seq] =
    AlbumTable
      .drop(2).take(1)

  val selectColumnsQuery1: Query[Rep[String], String, Seq] =
    AlbumTable
      .map(_.title)

  val selectColumnsQuery2: Query[(Rep[String], Rep[String]), (String, String), Seq] =
    AlbumTable
      .map(a => (a.artist, a.title))

  val selectCombinedQuery: Query[Rep[String], String, Seq] =
    AlbumTable
      .filter(_.artist === "Keyboard Cat")
      .map(_.title)

  val selectExerciseQuery1: Query[AlbumTable, Album, Seq] =
    AlbumTable
      .filter(_.year > 1990)
      .filter(_.rating >= (Rating.NotBad : Rating))
      .sortBy(_.artist.asc)

  val selectExerciseQuery2: Query[Rep[String], String, Seq] =
    AlbumTable
      .sortBy(_.year.desc)
      .map(_.title)
      .take(1)



  // Returning single/multiple results ----------

  val selectPagedAction1: DBIOAction[Seq[Album], NoStream, Effect.Read] =
    selectPagedQuery
      .result

  val selectPagedAction2: DBIOAction[Option[Album], NoStream, Effect.Read] =
    selectPagedQuery
      .result
      .headOption

  val selectPagedAction3: DBIOAction[Album, NoStream, Effect.Read] =
    selectPagedQuery
      .result
      .head



  // Database -----------------------------------

  val db = Database.forConfig("musicdb")



  // Let's go! ----------------------------------

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  def createTestAlbums() = {
    exec(createTableAction)
    exec(insertAlbumsAction)
  }

  def main(args: Array[String]): Unit = {
    createTestAlbums()
    exec(selectCombinedQuery.result).foreach(println)
  }

}
