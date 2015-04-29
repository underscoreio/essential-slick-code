package chapter04
package multiple

import scala.slick.driver.JdbcDriver
import ChatSchema._

object DifferentDatabases extends App {
 /*

  def printStatements: DB ⇒ Unit = { info ⇒
    val schema = new Schema(info.driver)

    import schema._, profile.simple._

    def db = Database.forURL(info.url, driver = info.clazz)

    db.withSession {
      implicit session ⇒

        lazy val left = messages.
          leftJoin(users).on(_.senderId === _.id).
          leftJoin(rooms).on { case ((m, u), r) ⇒ m.roomId === r.id }.
          filter { case ((m, u), r) ⇒ u.id === Id[UserTable](1L) && r.id === Id[RoomTable](1L) }.
          map { case ((m, u), r) ⇒ m }

        lazy val right = for {
          ((msgs, usrs), rms) ← messages rightJoin users on (_.senderId === _.id) rightJoin rooms on { case ((m, u), r) ⇒ m.roomId === r.id }
          if usrs.id === Id[UserTable](1) && rms.id === Id[RoomTable](1L) && rms.id === msgs.roomId
        } yield msgs

        lazy val inner = for {
          ((msgs, usrs), rms) ← messages innerJoin users on (_.senderId === _.id) innerJoin rooms on (_._1.roomId === _.id)
          if usrs.id === Id[UserTable](1) && rms.id === Id[RoomTable](1L) && rms.id.? === msgs.roomId
        } yield msgs

        List(left, right, inner).foreach { q ⇒ println(q.selectStatement) }

    }

  }

  val h2 = DB(scala.slick.driver.H2Driver, "jdbc:h2:mem:chapter04", "org.h2.Driver")
  val ms = DB(scala.slick.driver.MySQLDriver, "jdbc:mysql:database", "com.mysql.jdbc.Driver")
  val pg = DB(scala.slick.driver.PostgresDriver, "jdbc:postgresql:database", "org.postgresql.Driver")

  List(h2, ms, pg).foreach { db ⇒
    println("============================================")
    printStatements(db)
    println("============================================")
  }

*/

}