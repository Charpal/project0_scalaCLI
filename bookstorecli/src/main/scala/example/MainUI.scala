package example;

import scala.io.StdIn.readLine
import scala.collection.Seq
import scala.io.Source
import org.mongodb.scala._
import scala.collection.JavaConverters._
import play.api.libs.json._
import example.Helpers._
import java.io.{FileNotFoundException, IOException}
import scala.jdk.Accumulator
import com.mongodb.client.model.Accumulators

object MainUI {
  var bookidmax = 499
  var orderidmax = 999
  case class BookData(name: Seq[JsValue], genre: Seq[JsValue], publish_date: Seq[JsValue])
  case class OrderData(firstname: Seq[JsValue], lastname: Seq[JsValue], orderdate: Seq[JsValue],
  email: Seq[JsValue], phone: Seq[JsValue], address: Seq[JsValue], city: Seq[JsValue], state: Seq[JsValue],
  productid: Seq[JsValue], salesamount: Seq[JsValue])

  def main(args: Array[String]) {
    val booklistfile = Source.fromFile("src/main/scala/example/booklist.json")
    val bookjson = try{
      Json.parse(booklistfile.getLines.mkString)
    } finally{
      booklistfile.close
    }
    val bookdata = BookData(bookjson \\ "name", bookjson \\ "genre", bookjson \\ "publish_date")

    val orderfile = Source.fromFile("src/main/scala/example/orders.json")("UTF-8")
    val orderjson = try{
      Json.parse(orderfile.getLines.mkString)
    } finally{
      orderfile.close
    }
    val orderdata = OrderData(orderjson \\ "first_name", orderjson \\ "last_name", orderjson \\ "order_date",
    orderjson \\ "email", orderjson \\ "phone", orderjson \\ "address", orderjson \\ "city",
    orderjson \\ "state", orderjson \\ "product_id", orderjson \\ "sales_amount")

    val client: MongoClient = MongoClient()
    val database: MongoDatabase = client.getDatabase("bookstore")
    val bookcollection: MongoCollection[Document] = database.getCollection("booklist")
    val ordercollection: MongoCollection[Document] = database.getCollection("orders")
    insertBooksToDB(bookcollection, bookdata.name, bookdata.genre, bookdata.publish_date)
    insertOrdersToDB(ordercollection, orderdata.firstname, orderdata.lastname, orderdata.orderdate, 
    orderdata.email, orderdata.phone, orderdata.address, orderdata.city, orderdata.state, 
    orderdata.productid, orderdata.salesamount)

    
    try {
      displayGreeting()
  } finally{
      Thread.sleep(5000)
      client.close()
  }
  }

  //Insert Books JSON file into database
 def insertBooksToDB(collection:MongoCollection[Document], name: Seq[JsValue], genre: Seq[JsValue], 
  publishdate: Seq[JsValue]):Unit = {
    var i = 0
    var nameIndex = 0

    //Loop through all of the data and store them into database
    for (i <- 0 to bookidmax){
      val doc = convertBookToDoc(name, genre, publishdate, nameIndex, i)
      nameIndex += 2
      collection.insertOne(doc).results()
    }
  }

  def convertBookToDoc(name: Seq[JsValue], genre: Seq[JsValue], 
  publishdate: Seq[JsValue], nameIndex: Int, normalIndex: Int): Document = {
    //Convert JSON into String
    val title = Json.stringify(name(nameIndex))
    val author = Json.stringify(name(nameIndex + 1))
    val bookgenre = Json.stringify(genre(normalIndex))
    val date = Json.stringify(publishdate(normalIndex))

    //Create document from data
    val doc: Document = Document(
      "_id" -> normalIndex,
      "title" -> trimDoubleQuotes(title),
      "author" -> trimDoubleQuotes(author),
      "genre" -> trimDoubleQuotes(bookgenre),
      "publish_date" -> trimDoubleQuotes(date),
    )

    //Return document
    doc
  }

  //Remove the double quotes at the beginning and end of string
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

    //Loop through data and store them into database
    for (i <- 0 to orderidmax){
      val doc = convertOrderToDocFromJsValue(firstname, lastname, orderdate, email, 
      phone, address, city, state, productid, sales, i)
      collection.insertOne(doc).results()
    }
  }

   def convertOrderToDocFromJsValue(firstname: Seq[JsValue], lastname: Seq[JsValue], 
      orderdate: Seq[JsValue], email: Seq[JsValue], phone: Seq[JsValue], 
      address: Seq[JsValue], city: Seq[JsValue], state: Seq[JsValue],
      productid: Seq[JsValue], sales: Seq[JsValue], index: Int): Document = {
    //Convert JSON into String
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

    //Create a document from data
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

    //Return document
    doc
  }

  def displayGreeting(): Unit = {
    println("Hello! Welcome to your bookstore database manager!\n")
  }

}
