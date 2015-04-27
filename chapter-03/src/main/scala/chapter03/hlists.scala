package chapter03

object HListExampleApp extends App {

  import scala.slick.driver.H2Driver.simple._
  import scala.slick.collection.heterogenous.{ HList, HCons, HNil, Nat }
  import scala.slick.collection.heterogenous.syntax._

  type User =  String :: Int :: Char :: Float :: Float :: Int :: String :: String :: Boolean :: Boolean :: String :: String ::
  String :: String :: String :: String :: String :: String :: String :: String :: Int :: Boolean :: String :: String  :: Long :: HNil

  case class User2(
      name: String, age: Int, gender: Char, height: Float, weight: Float, shoeSize: Int,
      email: String, phone: String, accepted: Boolean, sendNews: Boolean,
      street: String, city: String, country: String,
      faveColor: String, faveFood: String, faveDrink: String, faveTvShow: String, faveMovie: String, faveSong: String,
      lastPurchase: String, lastRating: Int, tellFriends: Boolean,
      petName: String, partnerName: String, id: Long)

  final class UserTable(tag: Tag) extends Table[User](tag, "user") {
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

    def * = name :: age :: gender :: height :: weight :: shoeSize ::
          email :: phone :: accepted :: sendNews ::
          street :: city :: country ::
          faveColor :: faveFood :: faveDrink :: faveTvShow :: faveMovie :: faveSong ::
          lastPurchase :: lastRating :: tellFriends ::
          petName :: partnerName :: id :: HNil
  }

  lazy val users = TableQuery[UserTable]

  // Database connection details:
  def db = Database.forURL("jdbc:h2:mem:chapter03", driver = "org.h2.Driver")

  db.withSession {
    implicit session ⇒

      users.ddl.create

      users +=
          "Dr. Dave Bowman" :: 43 :: 'M' :: 1.7f :: 74.2f :: 11 :: "dave@example.org" :: "+1555740122" :: true :: true ::
            "123 Some Street" :: "Any Town" :: "USA" ::
            "Black" :: "Ice Cream" :: "Coffee" :: "Sky at Night" :: "Silent Running" :: "Bicycle made for Two" ::
            "Acme Space Helmet" :: 10 :: true ::
            "HAL" :: "Betty" :: 0L :: HNil


      println(
        users.list
       )
    }
}