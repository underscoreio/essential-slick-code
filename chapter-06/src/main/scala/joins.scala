import org.joda.time._
import ChatSchema._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object JoinsExample extends App {

  val schema = Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter06")

  exec(populate)

  val daveId    = users.filter(_.name === "Dave").map(_.id)
  val halId     = users.filter(_.name === "HAL").map(_.id)
  val elenaId   = users.filter(_.name === "Elena").map(_.id)
  val frankId   = users.filter(_.name === "Frank").map(_.id)

  val airLockId = rooms.filter(_.title === "Air Lock").map(_.id)
  val podId     = rooms.filter(_.title === "Pod").map(_.id)

  //
  // Monadic Joins
  //
  //
  val monadicJoin = for {
    msg <- messages
    usr <- msg.sender
  } yield (usr.name, msg.content)

  exec(monadicJoin.result).foreach(println)

  // Further examples from the book appear below.
  // Remove the comments around what you need.

  /*
  // Dave's messages example:
  val davesMessages = for {
    dId     <- daveId
    rId     <- airLockId
    message <- messages
    user    <- users
    room    <- rooms
    if message.senderId === user.id &&
       message.roomId   === room.id &&
       user.id          === dId  &&
       room.id          === rId
  } yield (message.content, user.name, room.title)

  exec(davesMessages.result).foreach( println )

  val davesMessagesWithFKs =  for {
    dId     <- daveId
    rId     <- airLockId
    message <- messages
    user    <- message.sender
    room    <- message.room
    if user.id === dId &&
       room.id === rId
  } yield (message.content, user.name, room.title)

  exec(davesMessagesWithFKs.result).foreach(result => println(result))
  */

  //
  // Applicative Joins
  //

  // Taste of the syntax:
  /*
  val syntax = messages join users on (_.senderId === _.id)
  val syntaxQuery = syntax.map { case (msg, user) => (msg.content, user.name)  }
  exec(syntaxQuery.result).foreach( println )
  */

  // Inner join:

  /*
  // A version reaching into the tuple...
  val inner0 =
    messages.
    join(users).on(_.senderId === _.id).
    join(rooms).on(_._1.roomId === _.id)

  // ... or naming the tuple elements:
  val inner =
    messages.
    join(users).on(_.senderId === _.id).
    join(rooms).on{ case ((msg,user), room) => msg.roomId === room.id}


  val innerQ = for {
    dId <- daveId
    rId <- airLockId
    ((msgs, usrs), rms) <- inner
    if usrs.id === dId && rms.id === rId
  } yield (msgs.content, usrs.name, rms.title)

  println("Inner Join")
  exec(innerQ.result).foreach(println)
  */

  // Left outer:
  /*
  val left = messages.
    joinLeft(users).on(_.toId === _.id).
    map { case (m, u:Rep[Option[UserTable]]) => (m.content, u.map(_.name)) }

  println("left outer join")
  exec(left.result).foreach(println)
  */

  // Right outer:
  /*
  val right = for {
    (msg, user) <- messages.joinRight(users).on(_.toId === _.id)
  } yield (user.name, msg.map(_.content))

  exec(right.result).foreach(println)
  */

  // Without an `on`, you have a cross join:
  //exec(messages joinLeft users result).foreach(println)

  // H2 doesn't support FULL OUTER JOINS at the time of writing.
  /*
      val outer = for {
        (room, msg) <- rooms joinFull messages on (_.id === _.roomId)
      } yield room.map(_.title) -> msg.map(_.content)

      println(s"full ${outer.result.statements}")

      exec(outer.result).foreach(println)
  */

  // Zip Join
  lazy val msgs = messages.sortBy(_.ts asc)
  /*
      val conversations = msgs zip msgs.drop(1)

      val zipQuery =
        conversations.map{ case (fst, snd) =>  fst.content -> snd.content }

      println(zipQuery.result.statements)
      exec(zipQuery.result).foreach(println)
  */

  //ZipWith
  /*
      def combiner(fst: MessageTable, snd: MessageTable) = fst.content -> snd.content
      val query = msgs.zipWith(msgs.drop(1), combiner)
      exec(query.result).foreach(println)
  */

  //ZipWithIndex
  /*
      exec(messages.zipWithIndex.map {
        case (msg, index) => index -> msg.content
      }.result).foreach(println)
  */

  //Implicit cross join
  /*
      val query = messages joinLeft users
      exec(query.result).foreach(println)
  */


}
