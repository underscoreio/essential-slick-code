package chapter05

import org.joda.time._
import ChatSchema._

object JoinsExample extends App {

  val schema = new Schema(scala.slick.driver.H2Driver)
  import schema._, profile.simple._
  def db = Database.forURL("jdbc:h2:mem:chapter05", driver="org.h2.Driver")

  db.withSession {
    implicit session =>

     populate

      val daveId:  PK[UserTable] = users.filter(_.name === "Dave").map(_.id).first
      val halId:   PK[UserTable] = users.filter(_.name === "HAL").map(_.id).first
      val elenaId: PK[UserTable] = users.filter(_.name === "Elena").map(_.id).first
      val frankId: PK[UserTable] = users.filter(_.name === "Frank").map(_.id).first

      val airLockId: PK[RoomTable] = rooms.filter(_.title === "Air Lock").map(_.id).first
      val podId:     PK[RoomTable] = rooms.filter(_.title === "Pod").map(_.id).first

      //
      // Implicit Joins
      //

      val implicitJoin = for {
        msg <- messages
        usr <- msg.sender
      } yield (usr.name, msg.content)

      implicitJoin.run.foreach(result => println(result))


      // Further examples from the book appear below.
      // Remove the comments around what you need.

      /*
      // Dave's messages example:

      val davesMessages = for {
        message <- messages
        user    <- users
        room    <- rooms
        if message.senderId === user.id &&
           message.roomId   === room.id &&
           user.id          === daveId  &&
           room.id          === airLockId
      } yield (message.content, user.name, room.title)

      davesMessages.run.foreach(result => println(result))

      val davesMessagesWithFKs =  for {
        message <- messages
        user    <- message.sender
        room    <- message.room
        if user.id === daveId &&
           room.id === airLockId fs
      } yield (message.content, user.name, room.title)

      davesMessagesWithFKs.run.foreach(result => println(result))
      */


      //
      // Explicit Joins
      //

      /*
      // Taste of the syntax:
      val syntax = messages innerJoin users on (_.senderId === _.id)
      val syntaxQuery = syntax.map { case (msg, user) => (msg.content, user.name)  }
      syntaxQuery.run.foreach(result => println(result))
      */


      // Inner join:

      /*
      // A version reaching into the tuple...
      val inner0 =
        messages.
        innerJoin(users).on(_.senderId === _.id).
        innerJoin(rooms).on(_._1.roomId === _.id)

      // ... or naming the tuple elements:
      val inner =
        messages.
        innerJoin(users).on(_.senderId === _.id).
        innerJoin(rooms).on{ case ((msg,user), room) => msg.roomId === room.id}


      val innerQ = for {
        ((msgs, usrs), rms) <- inner
        if usrs.id === daveId && rms.id === airLockId
      } yield (msgs.content, usrs.name, rms.title)

      innerQ.run.foreach(result => println(result))
      */

      // Left outer:

      /*
      val left = messages.
        leftJoin(users).on(_.toId === _.id).
        map { case (m, u) => (m.content, u.name.?) }

      left.run.foreach(result => println(result))
      */

      // Right outer:

      /*
      val right = for {
        (msg, user) <- messages.rightJoin(users).on(_.toId === _.id)
      } yield (user.name, msg.content.?)

      right.run.foreach(result => println(result))
      */


      // Without an `on`, you have a cross join:
      //(messages leftJoin users).run.foreach(println)


      // H2 doesn't support FULL OUTER JOINS at the time of writing.
      /*
      val outer = for {
        (room, msg) <- rooms outerJoin messages on (_.id === _.roomId)
      } yield room.title.? -> msg.content.?

      outer.run.foreach(println)
      */

      // Zip Join

      /*
      val msgs = messages.sortBy(_.ts asc)
      val conversations = msgs zip msgs.drop(1)

      val zipResults: List[(String,String)] =
        conversations.map{ case (fst, snd) =>  fst.content -> snd.content }.list

      println(zipResults)
     */
  }
}