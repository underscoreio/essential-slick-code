package chapter04

import org.joda.time._
import chapter04.MessagingSchema._

object JoinsExample extends App {

  val schema = new Schema(scala.slick.driver.H2Driver)
  import schema._, profile.simple._
  def db = Database.forURL("jdbc:h2:mem:chapter04", driver="org.h2.Driver")

  db.withSession {
    implicit session =>

      populate

      val daveId:  PK[UserTable] = users.filter(_.name === "Dave").map(_.id).first
      val halId:   PK[UserTable] = users.filter(_.name === "HAL").map(_.id).first
      val elenaId: PK[UserTable] = users.filter(_.name === "Elena").map(_.id).first
      val frankId: PK[UserTable] = users.filter(_.name === "Frank").map(_.id).first

      val airLockId: PK[RoomTable] = rooms.filter(_.title === "Air Lock").map(_.id).first
      val podId:     PK[RoomTable] = rooms.filter(_.title === "Pod").map(_.id).first

      /*
      val implicitJoin = for {
        msg <- messages
        usr <- msg.sender
      } yield (usr.name, msg.content)

      implicitJoin.run.foreach(result => println(result))
      */

      // Dave's messages example:

      val davesMessages = for {
        message <- messages
        user    <- users
        room    <- rooms
        if message.senderId === user.id &&
           message.roomId   === room.id &&
           user.id          === daveId  &&
           room.id          === airLockId
      } yield message

      davesMessages.run.foreach(result => println(result))

      val davesMessagesWithFKs =  for {
        message <- messages
        user    <- message.sender
        room    <- message.room
        if user.id === daveId &&
           room.id === airLockId
      } yield message

      davesMessagesWithFKs.run.foreach(result => println(result))



/*


TODO....

      //explicit join
      lazy val leftJoinJoin = messages.
        leftJoin(users).
        leftJoin(rooms).
        on { case ((m, u), r) => m.senderId === u.id && m.roomId === r.id }.
        filter { case ((m, u), r) => u.id === daveId && r.id === airLockId }.
        map { case ((m, u), r) => m }

      lazy val left = messages.
        leftJoin(users).on(_.senderId === _.id).
        leftJoin(rooms).on{ case ((m,u),r) => m.roomId === r.id}.
        filter { case ((m, u), r) => u.id === daveId && r.id === airLockId }.
        map { case ((m, u), r) => m }

      lazy val right = for {
        ((msgs, usrs), rms) <- messages rightJoin users on (_.senderId === _.id) rightJoin rooms on { case ((m,u),r) =>  m.roomId === r.id}
        if usrs.id === daveId && rms.id === airLockId && rms.id === msgs.roomId
      } yield msgs

      lazy val inner = for {
        ((msgs, usrs), rms) <- messages innerJoin users on (_.senderId === _.id) innerJoin rooms on (_._1.roomId === _.id)
        if usrs.id === daveId && rms.id === airLockId && rms.id.? === msgs.roomId
      } yield msgs

      // H2 doesn't support FULL OUTER JOINS at the time of writing.
      //lazy val outer = for {
    //    (msg, usr) <- messages outerJoin users on (_.senderId.? === _.id.?)
    //  } yield msg -> usr



      lazy val userRooms = for {
        ((u, o), r) <- users.
          rightJoin(occupants).
          rightJoin(rooms).
          on { case ((u, o), r) => u.id === o.userId && r.id === o.roomId }
      } yield (u.name, r.title)

      lazy val firstAndLastMessage = messages.filter(_.senderId === daveId).groupBy { _ => true }.map {
        case (_, group) => (group.map(_.id).max, group.map(_.id).min)
      }

      val zip = for {
       (u,r) <-  users zip rooms
     } yield u.name -> r.title


     List(left,right,inner).foreach{ q =>
       println(q.selectStatement)
       println(q.list.mkString("\n","\n","\n"))
     }
*/

  }

}