import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile
import slick.collection.heterogeneous.HNil

// Code relating to 5.2.3 "Heterogeneous Lists"

object HListExampleApp extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    case class UserClass(
       name         : String,
       age          : Int,
       gender       : Char,
       height       : Float,
       weight       : Float,
       shoeSize     : Int,
       email        : String,
       phone        : String,
       accepted     : Boolean,
       sendNews     : Boolean,
       street       : String,
       city         : String,
       country      : String,
       faveColor    : String,
       faveFood     : String,
       faveDrink    : String,
       faveTvShow   : String,
       faveMovie    : String,
       faveSong     : String,
       lastPurchase : String,
       lastRating   : Int,
       tellFriends  : Boolean,
       petName      : String,
       partnerName  : String,
       id           : Long
     )

    final class UserTable(tag: Tag) extends Table[UserClass](tag, "user") {
      def id           = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name         = column[String]("name")
      def age          = column[Int]("age")
      def gender       = column[Char]("gender")
      def height       = column[Float]("height_m")
      def weight       = column[Float]("weight_kg")
      def shoeSize     = column[Int]("shoe_size")
      def email        = column[String]("email_address")
      def phone        = column[String]("phone_number")
      def accepted     = column[Boolean]("terms")
      def sendNews     = column[Boolean]("newsletter")
      def street       = column[String]("street")
      def city         = column[String]("city")
      def country      = column[String]("country")
      def faveColor    = column[String]("fave_color")
      def faveFood     = column[String]("fave_food")
      def faveDrink    = column[String]("fave_drink")
      def faveTvShow   = column[String]("fave_show")
      def faveMovie    = column[String]("fave_movie")
      def faveSong     = column[String]("fave_song")
      def lastPurchase = column[String]("sku")
      def lastRating   = column[Int]("service_rating")
      def tellFriends  = column[Boolean]("recommend")
      def petName      = column[String]("pet")
      def partnerName  = column[String]("partner")

      def * = (name :: age :: gender :: height :: weight :: shoeSize ::
            email :: phone :: accepted :: sendNews ::
            street :: city :: country ::
            faveColor :: faveFood :: faveDrink :: faveTvShow :: faveMovie :: faveSong ::
            lastPurchase :: lastRating :: tellFriends ::
            petName :: partnerName :: id :: HNil).mapTo[UserClass]
    }

    lazy val users = TableQuery[UserTable]
  }

  class Schema(val profile: JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter05")

  val caseClassDave = UserClass(
      "Dr. Dave Bowman", 43, 'M', 1.7f, 74.2f, 11, "dave@example.org", "+1555740122", true, true,
      "123 Some Street", "Any Town", "USA",
      "Black", "Ice Cream", "Coffee", "Sky at Night", "Silent Running", "Bicycle made for Two",
      "Acme Space Helmet", 10, true,
      "HAL", "Betty", 0L)

  val program = for {
    _ <- users.schema.create
    _ <- users += caseClassDave
    folks  <- users.result
  } yield folks

  println("\nThe contents of the users table:")
  exec(program).foreach { println }
}
