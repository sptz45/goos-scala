package auctionsniper.xmpp

import org.jivesoftware.smack.{Chat, MessageListener}
import org.jivesoftware.smack.packet.Message

import auctionsniper.AuctionEventListener
import auctionsniper.PriceSource._

class AuctionMessageTranslator(
  sniperId: String,
  listener: AuctionEventListener,
  failureReporter: XMPPFailureReporter) extends MessageListener {

  def processMessage(chat: Chat, message: Message): Unit = {
    val messageBody = message.getBody
    try {
      translate(messageBody)
    } catch {
      case parseException: Exception =>
        failureReporter.cannotTranslateMessage(sniperId, messageBody, parseException)
        listener.auctionFailed()
    }
  }
  
  def translate(messageBody: String): Unit = {
    val event = AuctionEvent.from(messageBody)
    event.eventType match {
      case "CLOSE" => listener.auctionClosed()
      case "PRICE" => listener.currentPrice(event.currentPrice, event.increment, event.isFrom(sniperId)) 
      case _ => ()
    }
  }
}

private class AuctionEvent {
  
  private val fields = new scala.collection.mutable.HashMap[String, String]  

  def eventType = get("Event")
  def currentPrice = get("CurrentPrice").toInt
  def increment = get("Increment").toInt
  def isFrom(sniperId: String) =
    if (sniperId == bidder) FromSniper else FromOtherBidder

  private def bidder  = get("Bidder")

  private def get(fieldName: String) = { 
    val value = fields(fieldName)
    if (value == null) 
      throw new MissingValueException(fieldName)
    value
  }

  private def addField(field: String): Unit = {
    val pair = field.split(":")
    fields += (pair(0).trim -> pair(1).trim)
  }
 }

private object AuctionEvent {
  def from(messageBody: String) = {
    val event = new AuctionEvent
    for (field <- fieldsIn(messageBody)) event.addField(field)
    event
  }

  def fieldsIn(messageBody: String) =  messageBody.split(";")
}

private class MissingValueException(field: String) extends Exception("Missing value for " + field)
