package chapter03

object NestedCaseClassExampleApp extends App {

  import scala.slick.driver.H2Driver.simple._

//  case class Demographics(name: String, age: Int, gender: Char, height: Float, weight: Float, shoeSize: Int)
//  case class Contact(email: String, phone: String, accepted: Boolean, sendNews: Boolean)
//  case class Address(street: String, city: String, country: String)
//  case class Preferences(color:String, food: String, drink: String, tvShow: String, movie: String, song: String)
//  case class Purchases(last: String, rating: Int, recommend: Boolean)
//  case class Family(pet: String, partner: String)
//
//  case class User(
//      demo: Demographics, contact: Contact, address: Address, prefs: Preferences,
//      purchases: Purchases, family: Family, id: Long = 0L)

  case class EmailContact(name: String, email: String)
  case class Address(street: String, city: String, country: String)

  case class User(contact: EmailContact, address: Address, id: Long = 0L)


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

    def pack(row: (String, String, String, String, String, Long)): User =
      User(
        EmailContact(row._1, row._2),
        Address(row._3, row._4, row._5), row._6)

    def unpack(user: User): Option[(String, String, String, String, String, Long)] =
      Some((user.contact.name, user.contact.email, user.address.street, user.address.city, user.address.country, user.id))

     def * = (name, email, street, city, country, id) <> (pack, unpack)



    }

  lazy val users = TableQuery[UserTable]

  // Database connection details:
  def db = Database.forURL("jdbc:h2:mem:chapter03", driver = "org.h2.Driver")

  db.withSession {
    implicit session =>
      // Create the tables:
      users.ddl.create
      users += User(
        EmailContact("Dr. Dave Bowman", "dave@example.org"),
        Address("123 Some Street", "Any Town", "USA")
      )


//      users +=
//      User(
//          Demographics("Dr. Dave Bowman", 43, 'M', 1.7f, 74.2f, 11),
//          Contact("dave@example.org", "+1555740122", true, true),
//          Address("123 Some Street", "Any Town", "USA"),
//          Preferences("Black", "Ice Cream", "Coffee", "Sky at Night", "Silent Running", "Bicycle made for Two"),
//          Purchases("Acme Space Helmet", 10, true),
//          Family("HAL", "Betty"),  0L)



            //println(users.run)
          println(users.map(_.faveColor).run)


  }
}