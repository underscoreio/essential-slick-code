import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

// To validate tsql during compilation we supply
// database connection details via an annotation:
import slick.backend.StaticDatabaseConfig
@StaticDatabaseConfig("file:src/main/resources/application.conf#tsql")
object TsqlExample extends App {

  import slick.driver.H2Driver.api._

  val query: DBIO[Seq[String]] =
    tsql""" select "content" from "message" """

  val insert: DBIO[Seq[Int]] =
    tsql"""insert into "message" ("content") values ('Hello') """

  // Using the TSQL configuration directly:
  import slick.backend.DatabaseConfig
  val conf: DatabaseConfig[slick.driver.H2Driver] = DatabaseConfig.forConfig("tsql")
  val db = conf.db
  println("Content is:")
  val future = db.run(insert andThen query).map { _ foreach println }
  Await.result(future, 2 seconds)

  db.close
}
