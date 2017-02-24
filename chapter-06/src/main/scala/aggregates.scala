import org.joda.time._
import ChatSchema._
import scala.language.higherKinds

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile

object AggregatesExample extends App {

  val schema = new Schema(slick.jdbc.H2Profile)
  import schema._, profile.api._
  val db = Database.forConfig("chapter06")
  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)

  exec(populate)

  // Count:
  val numRows:Int = exec(messages.length.result)
  println(s"Total messages: ${numRows}")

  // Number of message senders:
  val senders:Int = exec(messages.map(_.senderId).distinct.length.result)
  println(s"Unique message senders: ${senders}")

  // First message date:
  val first:Option[DateTime] = exec(messages.map(_.ts).min.result)
  println(s"First sent: ${first}")

  // Last message date:
  val last = messages.map(_.ts).max.result
  println(s"Last sent: ${exec(last)}")

  // Group by:
  val msgsPerUser =
    messages.join(users).on(_.senderId === _.id).
      groupBy { case (msg, user) => user.name }.
      map { case (name, group) => name -> group.length }.
      result
  println(s"Messages per user: ${exec(msgsPerUser)}")

  // Grouping by multiple columns:
  val msgsPerRoomPerUser =
    rooms.
      join(messages).on(_.id === _.roomId).
      join(users).on { case ((room, msg), user) => user.id === msg.senderId }.
      groupBy { case ((room, msg), user) => (room.title, user.name) }.
      map { case ((room, user), group) => (room, user, group.length) }.
      sortBy { case (room, user, group) => room }.
      result
  println(s"Messages per room per user: ${exec(msgsPerRoomPerUser)}")

  // More involved grouping:
  val stats =
    messages.join(users).on(_.senderId === _.id).
      groupBy { case (msg, user) => user.name }.
      map { case (name, group) => (name, group.length, group.map { case (msg, user) => msg.ts }.min) }

  println(s"Stats: ${exec(stats.result)}")

  // Extracting functions:
  import scala.language.higherKinds
  def timestampOf[S[_]](group: Query[(MessageTable, UserTable), (Message, User), S]) =
    group.map { case (msg, user) => msg.ts }

  val nicerStats =
    messages.join(users).on(_.senderId === _.id).
      groupBy { case (msg, user) => user.name }.
      map { case (name, group) =>
        (name, group.length, timestampOf(group).min) }

  println(s"Nicer Stats: ${exec(nicerStats.result)}")

}
