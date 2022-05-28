package test.auctionsniper

import auctionsniper.SniperPortfolio.PortfolioListener
import auctionsniper.UserRequestListener.Item
import auctionsniper.{AuctionSniper, SniperPortfolio}
import org.jmock.Expectations
import test.fixtures.JMockSuite

class SniperPortfolioTest extends JMockSuite:

  test("notifies listeners of new snipers") {

    val listener = context().mock(classOf[PortfolioListener])
    val portfolio = SniperPortfolio()

    val sniper = AuctionSniper(Item("item id", 123), null)
    context().checking(new Expectations:
      oneOf(listener).sniperAdded(sniper)
    )
    portfolio.addPortfolioListener(listener)
    
    portfolio.addSniper(sniper)
  }
