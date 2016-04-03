package plainsql

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.dbio.DBIOAction
import slick.profile.SqlAction

import slick.driver.H2Driver.api._

object Main {

  // Tables -------------------------------------

  case class Artist(
    name   : String,
    id     : Long = 0L)

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artists") {
    def name   = column[String]("name")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (name, id) <> (Artist.tupled, Artist.unapply)
  }

  lazy val ArtistTable = TableQuery[ArtistTable]

  case class Album(
    artistId : Long,
    title    : String,
    year     : Int,
    rating   : Rating,
    id       : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artistId = column[Long]("artist_id")
    def title    = column[String]("title")
    def year     = column[Int]("year")
    def rating   = column[Rating]("rating")
    def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artistId, title, year, rating, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]



  // Setup --------------------------------------

  import slick.jdbc.GetResult

  implicit val ratingGetResult: GetResult[Rating] = GetResult { r =>
    Rating.fromInt(r.nextInt)
  }

  implicit val artistGetResult: GetResult[Artist] =
    GetResult { r =>
      Artist(
        name = r.<<,
        id   = r.<<
      )
    }

  // TODO: Write a GetResult for Album
  implicit val albumGetResult: GetResult[Album] = GetResult { r =>
    Album(
      artistId = r.nextInt,
      title    = r.nextString,
      year     = r.nextInt,
      rating   = r.<<[Rating],
      id       = r.nextInt
    )
  }



  // Setup --------------------------------------

  val createTablesAction =
    ArtistTable.schema.create andThen
    AlbumTable.schema.create

  val dropTablesAction =
    AlbumTable.schema.drop andThen
    ArtistTable.schema.drop

  val insertArtistsAction =
    sqlu"""
    insert into "artists" ("name") values
      ( 'Keyboard Cat'    ),
      ( 'Spice Girls'     ),
      ( 'Rick Astley'     ),
      ( 'My Mate''s Band' );
    """

  def findArtistId(name: String): DBIO[Int] =
    sql"""
    select "id" from "artists" where "name" = $name
    """.as[Int].head

  def insertAlbumsAction =
    for {
      _       <- insertArtistsAction
      catId   <- findArtistId("Keyboard Cat")
      spiceId <- findArtistId("Spice Girls")
      rickId  <- findArtistId("Rick Astley")
      _       <- sqlu"""
                 insert into "albums" ("artist_id", "title", "year", "rating") values
                   ( $catId   , 'Keyboard Cat''s Greatest Hits' , 2009 , 5 ),
                   ( $spiceId , 'Spice'                         , 1996 , 4 ),
                   ( $spiceId , 'Forever'                       , 2000 , 3 ),
                   ( $rickId  , 'Whenever You Need Somebody'    , 1987 , 5 ),
                   ( $rickId  , 'Hold Me in Your Arms'          , 1988 , 4 ),
                   ( $rickId  , 'Free'                          , 1991 , 3 ),
                   ( $rickId  , 'Body & Soul'                   , 1993 , 3 ),
                   ( $rickId  , 'Keep It Turned On'             , 2001 , 3 ),
                   ( $rickId  , 'Portrait'                      , 2005 , 2 ),
                   ( $rickId  , 'My Red Book'                   , 2013 , 3 );
                 """
    } yield ()

  val insertAllAction =
    insertArtistsAction andThen
    insertAlbumsAction



  // Select Queries -----------------------------

  val selectAllAction =
    sql"""
    select * from "artists", "albums"
    where "artists"."id" = "albums"."artist_id"
    """.as[(Artist, Album)]

  val selectAlbumsAction =
    sql""" select * from "albums" """.as[Album]

  val selectArtistsAction =
    sql""" select * from "artists" """.as[Artist]



  // Database -----------------------------------

  val db = Database.forConfig("musicdb.db")



  // Let's go! ----------------------------------

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  def main(args: Array[String]): Unit = {
    val everythingAction =
      createTablesAction andThen
      insertAllAction andThen
      selectAllAction

    exec(everythingAction.transactionally).foreach(println)
  }

}
