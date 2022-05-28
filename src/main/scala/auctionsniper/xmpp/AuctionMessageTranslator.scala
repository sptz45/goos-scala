package auctionsniper.xmpp

import org.jivesoftware.smack.{Chat, MessageListener}
import org.jivesoftware.smack.packet.Message
import auctionsniper.{AuctionEventListener, PriceSource}
import auctionsniper.PriceSource.*

import scala.collection.mutable
import scala.util.chaining.scalaUtilChainingOps

class AuctionMessageTranslator(
  sniperId: String,
  listener: AuctionEventListener,
  failureReporter: XMPPFailureReporter) extends MessageListener:

  def processMessage(chat: Chat, message: Message): Unit =
    val messageBody = message.getBody
    try
      translate(messageBody)
    catch
      case parseException: Exception =>
        failureReporter.cannotTranslateMessage(sniperId, messageBody, parseException)
        listener.auctionFailed()
  
  def translate(messageBody: String): Unit =
    val event = AuctionEvent.from(messageBody)
    event.eventType match
      case "CLOSE" => listener.auctionClosed()
      case "PRICE" => listener.currentPrice(event.currentPrice, event.increment, event.isFrom(sniperId)) 
      case _ => ()

private class AuctionEvent:

  private val fields = mutable.HashMap[String, String]()

  def eventType: String = get("Event")
  def currentPrice: Int = get("CurrentPrice").toInt
  def increment: Int = get("Increment").toInt

  def isFrom(sniperId: String): PriceSource =
    if sniperId == bidder then FromSniper else FromOtherBidder

  private def bidder  = get("Bidder")

  private def get(fieldName: String) =
    val value = fields(fieldName)
    if value == null then
      throw new MissingValueException(fieldName)
    value

  private def addField(field: String): Unit =
    val pair = field.split(":")
    fields += (pair(0).trim -> pair(1).trim)


private object AuctionEvent:

  def from(messageBody: String): AuctionEvent =
    AuctionEvent().tap { event =>
      for field <- fieldsIn(messageBody) do event.addField(field)
    }

  def fieldsIn(messageBody: String): Array[String] = messageBody.split(";")

private class MissingValueException(field: String) extends Exception("Missing value for " + field)
