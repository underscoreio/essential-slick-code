import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.{JdbcProfile, H2Driver}

trait DatabaseProfile {
  val profile: JdbcProfile

  import profile.api._

  // ...
}

// trait FooModule {
//   self: DatabaseProfile =>

//   import profile.api._

//   // ...
// }

class DatabaseLayer(val profile: JdbcProfile) extends DatabaseProfile
  /* with FooModule */ {

  import profile.api._

  val db = Database.forConfig("projectdb")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)
}


object Main {
  val databaselayer = new DatabaseLayer(H2Driver)

  import databaselayer._

  def main(args: Array[String]): Unit = {
    ???
  }
}
