package profiles

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.{JdbcProfile, H2Driver}

trait DatabaseProfile {
  val profile: JdbcProfile

  import profile.api._
}



trait ArtistDatabaseModule {
  self: DatabaseProfile =>

  import profile.api._

  case class Artist(
    name   : String,
    id     : Long = 0L)

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artists") {
    def name   = column[String]("name")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, id) <> (Artist.tupled, Artist.unapply)
  }

  lazy val ArtistTable = TableQuery[ArtistTable]
}



trait AlbumDatabaseModule {
  self: DatabaseProfile with ArtistDatabaseModule =>

  import profile.api._

  case class Album(
    artistId : Long,
    title    : String,
    id       : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artistId = column[Long]("artistId")
    def title    = column[String]("title")
    def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (artistId, title, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]

  val selectAllAction: DBIO[Seq[(Artist, Album)]] =
    ArtistTable.join(AlbumTable)
      .on     { case (artist, album) => artist.id === album.artistId }
      .sortBy { case (artist, album) => artist.name.asc }
      .result
}



trait TestDataModule {
  self: DatabaseProfile with ArtistDatabaseModule with AlbumDatabaseModule =>

  import profile.api._

  val createTablesAction =
    ArtistTable.schema.create andThen
    AlbumTable.schema.create

  val dropTablesAction =
    AlbumTable.schema.drop andThen
    ArtistTable.schema.drop

  val insertAllAction: DBIOAction[Unit, NoStream, Effect.Write] =
    for {
      keyboardCatId  <- ArtistTable returning ArtistTable.map(_.id) += Artist( "Keyboard Cat"   )
      spiceGirlsId   <- ArtistTable returning ArtistTable.map(_.id) += Artist( "Spice Girls"    )
      rickAstleyId   <- ArtistTable returning ArtistTable.map(_.id) += Artist( "Rick Astley"    )
      _              <- AlbumTable ++= Seq(
                          Album( keyboardCatId , "Keyboard Cat's Greatest Hits" ),
                          Album( spiceGirlsId  , "Spice"                        ),
                          Album( rickAstleyId  , "Whenever You Need Somebody"   ))
    } yield ()

  val doEverythingAction: DBIO[Seq[(Artist, Album)]] = (
    createTablesAction andThen
    insertAllAction andThen
    selectAllAction
  ).transactionally
}



class DatabaseLayer[A <: JdbcProfile](val profile: JdbcProfile) extends DatabaseProfile
  with ArtistDatabaseModule
  with AlbumDatabaseModule
  with TestDataModule {

  import profile.api._

  val db = Database.forConfig("musicdb")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)
}


object Main {
  val databaselayer = new DatabaseLayer(H2Driver)

  import databaselayer._

  def main(args: Array[String]): Unit = {

    exec(doEverythingAction).foreach(println)
  }
}
