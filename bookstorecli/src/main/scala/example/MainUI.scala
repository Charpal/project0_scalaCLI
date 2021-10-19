package example;

// import scala.collection.JavaConverters._
// import org.mongodb.scala.connection.ClusterSettings

import scala.io.StdIn.readLine
import scala.util.Random
import scala.collection.Seq
import scala.io.Source
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.model.Aggregates._
import scala.collection.JavaConverters._
import play.api.libs.json._
import example.Helpers._
import java.io.{FileNotFoundException, IOException}
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.model.Projections
import scala.jdk.Accumulator
import com.mongodb.client.model.Accumulators

object MainUI {
  var bookidmax = 499
  var orderidmax = 999
  case class Book(name: String, author: String, genre: String, publish_date: String )

  def main(args: Array[String]) {
    val booklistfile = Source.fromFile("src/main/scala/example/booklist.json")
    val bookjson = try{
      Json.parse(booklistfile.getLines.mkString)
    } finally{
      booklistfile.close
    }
    val names = bookjson \\ "name"
    val bookgenre = bookjson \\ "genre"
    val bookpublishdate = bookjson \\ "publish_date"

    val orderfile = Source.fromFile("src/main/scala/example/orders.json")("UTF-8")
    val orderjson = try{
      Json.parse(orderfile.getLines.mkString)
    } finally{
      orderfile.close
    }
    val firstnames = orderjson \\ "first_name"
    val lastnames = orderjson \\ "last_name"
    val orderdate = orderjson \\ "order_date"
    val email = orderjson \\ "email"
    val phone = orderjson \\ "phone"
    val address = orderjson \\ "address"
    val city = orderjson \\ "city"
    val state = orderjson \\ "state"
    val productid = orderjson \\ "product_id"
    val sales = orderjson \\ "sales_amount"

    val client: MongoClient = MongoClient()
    val database: MongoDatabase = client.getDatabase("bookstore")
    val bookcollection: MongoCollection[Document] = database.getCollection("booklist")
    val ordercollection: MongoCollection[Document] = database.getCollection("orders")
    insertBooksToDB(bookcollection, names, bookgenre, bookpublishdate)
    insertOrdersToDB(ordercollection, firstnames, lastnames,orderdate, email, phone, address, city, state, productid, sales)

    displayGreeting()
    displayOptions()
    // val userAction = promptUser()
    // println(userAction)
    try {
    //printGrossSales(ordercollection)
    //removeBookFromDB(bookcollection, "Came Madness")
    //updateBookPrice(bookcollection, "A Clockwork Case On Armageddon", 10)
    //addOrderToDB(ordercollection, "Charles", "Dang", "10/20/20", "charles.dang@revature.net", "747-226-8688",
    //"7313 Hesperia Avenue", "Los Angeles", "United States", 19, 5, orderidmax)
    //println(searchDB(books, "Doors's Hall")) <-- Works
    //books.find().printResults() <-- Works but client will close before finishing
    //database.listCollectionNames().printResults() <-- Works
  } finally{
    Thread.sleep(5000)
    client.close()
  }
    // test.insertOne(testdoc).results()
    //client.close()
  }

  //Insert Books JSON file into database
 def insertBooksToDB(collection:MongoCollection[Document], name: Seq[JsValue], genre: Seq[JsValue], 
  publishdate: Seq[JsValue]):Unit = {
    var i = 0
    var nameIndex = 0
    // val random = new Random
    // val possiblePrices = Seq(5, 8, 10, 12, 15, 20)
    for (i <- 0 to bookidmax){
      //val price = possiblePrices(random.nextInt(possiblePrices.length))
      val doc = convertBookToDoc(name, genre, publishdate, nameIndex, i)
      nameIndex += 2
      collection.insertOne(doc).results()
    }
  }

  def convertBookToDoc(name: Seq[JsValue], genre: Seq[JsValue], 
  publishdate: Seq[JsValue], nameIndex: Int, normalIndex: Int): Document = {
    val title = Json.stringify(name(nameIndex))
    val author = Json.stringify(name(nameIndex + 1))
    val bookgenre = Json.stringify(genre(normalIndex))
    val date = Json.stringify(publishdate(normalIndex))

    val doc: Document = Document(
      "_id" -> normalIndex,
      "title" -> trimDoubleQuotes(title),
      "author" -> trimDoubleQuotes(author),
      "genre" -> trimDoubleQuotes(bookgenre),
      "publish_date" -> trimDoubleQuotes(date),
    )

    doc
  }

  def trimDoubleQuotes(data: String) : String = {
    data.substring(1, data.length() -1)
  }

  //Insert Orders JSON file into database
  def insertOrdersToDB(collection:MongoCollection[Document], firstname: Seq[JsValue], lastname: Seq[JsValue], 
      orderdate: Seq[JsValue], email: Seq[JsValue], phone: Seq[JsValue], 
      address: Seq[JsValue], city: Seq[JsValue], state: Seq[JsValue],
      productid: Seq[JsValue], sales: Seq[JsValue]):Unit = {
    var i = 0
    var index = 0
    // val random = new Random
    // val possibleCopies = Seq(1, 2, 3, 4, 5, 6, 7)
    for (i <- 0 to orderidmax){
      //val copies = possibleCopies(random.nextInt(possibleCopies.length))
      val doc = convertOrderToDocFromJsValue(firstname, lastname, orderdate, email, 
      phone, address, city, state, productid, sales, i)
      collection.insertOne(doc).results()
    }
  }

   def convertOrderToDocFromJsValue(firstname: Seq[JsValue], lastname: Seq[JsValue], 
  orderdate: Seq[JsValue], email: Seq[JsValue], phone: Seq[JsValue], 
  address: Seq[JsValue], city: Seq[JsValue], state: Seq[JsValue],
  productid: Seq[JsValue], sales: Seq[JsValue], index: Int): Document = {
    val o_firstname = Json.stringify(firstname(index))
    val o_lastname = Json.stringify(lastname(index))
    val o_orderdate = Json.stringify(orderdate(index))
    val o_email = Json.stringify(email(index))
    val o_phone = Json.stringify(phone(index))
    val o_address = Json.stringify(address(index))
    val o_city = Json.stringify(city(index))
    val o_state = Json.stringify(state(index))
    val o_productid = Json.stringify(productid(index))
    val o_sales = Json.stringify(sales(index))

    val doc: Document = Document(
      "_id" -> index,
      "first_name" -> trimDoubleQuotes(o_firstname),
      "last_name" -> trimDoubleQuotes(o_lastname),
      "order_date" -> trimDoubleQuotes(o_orderdate),
      "email" -> trimDoubleQuotes(o_email),
      "phone" -> trimDoubleQuotes(o_phone),
      "address" -> trimDoubleQuotes(o_address),
      "city" -> trimDoubleQuotes(o_city),
      "state" -> trimDoubleQuotes(o_state),
      "product_id" -> o_productid.toInt,
      "sales_amount" -> o_sales.toInt,
    )

    doc
  }

  def displayGreeting(): Unit = {
    println("Hello! Welcome to your bookstore database manager!\n")
  }

  def displayOptions(): Unit = {
    println("\nPlease choose an action.\n" +
      "Add an Order (ADD)\nSearch For a Book (SEARCH)\nUpdate Book Price (UPDATE)\n" +
      "Delete a Book (DELETE)\nExit App (EXIT)\n")
  }

  //Prompt user for a string and return it
  def promptUser(): String = {
    val option = readLine("Enter your action here: " )
    option.toUpperCase() match {
      case "ADD" => "ADD"
      case "SEARCH" => "SEARCH"
      case "UPDATE" => "UPDATE"
      case "DELETE" => "DELETE"
      case "EXIT" => "EXIT"
      case _ => "ERROR"
    }
  }

//TODO: 
//Popular books
//Trending books
//Books under $

  //Add an order to the database
  def addOrderToDB(collection:MongoCollection[Document], firstname: String, lastname: String, 
  orderdate: String, email: String, phone: String, 
  address: String, city: String, country: String,
  productid: Int, copies: Int, index: Int): Unit = {
    orderidmax += 1
    val doc = convertOrderToDocFromString(firstname, lastname, 
        orderdate, email, phone, 
        address, city, country,
        productid, copies, index + 1)
    collection.insertOne(doc).results()
  }

  def convertOrderToDocFromString(firstname: String, lastname: String, 
  orderdate: String, email: String, phone: String, 
  address: String, city: String, country: String,
  productid: Int, copies: Int, index: Int): Document = {

    val doc: Document = Document(
      "_id" -> index,
      "first_name" -> firstname,
      "last_name" -> lastname,
      "order_date" -> orderdate,
      "email" -> email,
      "phone" -> phone,
      "address" -> address,
      "city" -> city,
      "country" -> country,
      "product_id" -> productid,
      "copies" -> copies,
    )

    //println(doc)
    doc
  }

  def searchDB (collection:MongoCollection[Document], title: String): String = {
    //Read
    Thread.sleep(3000)
    val searchresult = collection.find(equal("title", title)).printResults().toString()
    searchresult
  }

  def updateBookPrice(collection:MongoCollection[Document], title: String, price: Int): Unit = {
    collection.updateOne(equal("title", title), set("price", price)).printResults()
  }

  def searchAction(): Unit = {
    println("What book are you searching for?\n")
    val title = readLine("Enter title here: ")

  }

  def removeBookFromDB(collection: MongoCollection[Document], title: String): Unit = {
    collection.deleteOne(equal("title", title)).printResults()
  }

  def printGrossSales(collection: MongoCollection[Document]) : Unit = {
      
  }

}
