package test.auctionsniper

import org.jmock.{Expectations, Mockery}
import org.jmock.integration.junit4.JMock
import org.junit.Test
import org.junit.runner.RunWith

import auctionsniper.{AuctionSniper, SniperPortfolio}
import auctionsniper.UserRequestListener.Item
import auctionsniper.SniperPortfolio.PortfolioListener

@RunWith(classOf[JMock])
class SniperPortfolioTest {
  val context = new Mockery
  val listener = context.mock(classOf[PortfolioListener])
  val portfolio = new SniperPortfolio
  
  @Test
  def notifiesListenersOfNewSnipers(): Unit = {
    val sniper = new AuctionSniper(new Item("item id", 123), null)
    context.checking(new Expectations {
      oneOf(listener).sniperAdded(sniper)
    })
    portfolio.addPortfolioListener(listener)
    
    portfolio.addSniper(sniper)
  }
}
