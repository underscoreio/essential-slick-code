package chapter04

import org.joda.time._
import ChatSchema._

object AggregatesExample extends App {

  val schema = new Schema(scala.slick.driver.H2Driver)
  import schema._, profile.simple._
  def db = Database.forURL("jdbc:h2:mem:chapter04", driver="org.h2.Driver")

  db.withSession {
    implicit session =>

     populate
     
     // Count:
     val numRows: Int = messages.length.run
     println(s"Total messages: $numRows")
     
     // Number of message senders:
     val senders: Int = messages.map(_.senderId).countDistinct.run
     println(s"Unique message senders: $senders")
     
     // First message date:
     val first: Option[DateTime] = messages.map(_.ts).min.run
     println(s"First sent: $first")
     
     // Last message date:
     val last: Option[DateTime] = messages.map(_.ts).max.run
     println(s"Last sent: $last")
          
    // Group by:
        
     val msgsPerUser = 
        messages.join(users).on(_.senderId === _.id).
        groupBy { case (msg, user)  => user.name }.
        map     { case (name, group) => name -> group.length }.
        run
     println(s"Messages per user: $msgsPerUser")
        
        
     // More involved grouping:
     val stats = 
        messages.join(users).on(_.senderId === _.id).
        groupBy { case (msg, user)   => user.name }.
        map     { case (name, group) => (name, group.length, group.map{ case (msg, user) => msg.ts}.min) }
     
     println(s"Stats: ${stats.run}")
     
     // Extracting functions:
     import scala.language.higherKinds
     def timestampOf[S[_]](group: Query[(MessageTable,UserTable), (Message,User), S]) =
       group.map{ case (msg, user) => msg.ts}
     
     val nicerStats = 
        messages.join(users).on(_.senderId === _.id).
        groupBy { case (msg, user)   => user.name }.
        map     { case (name, group) => (name, group.length, timestampOf(group).min) }
     
  }
}