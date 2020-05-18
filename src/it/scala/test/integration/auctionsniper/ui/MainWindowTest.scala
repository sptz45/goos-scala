package test.integration.auctionsniper.ui

import auctionsniper.SniperPortfolio
import auctionsniper.UserRequestListener.Item
import auctionsniper.ui.MainWindow
import com.objogate.wl.swing.probe.ValueMatcherProbe
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import test.endtoend.auctionsniper.AuctionSniperDriver

class MainWindowTest {
  private val mainWindow = new MainWindow(new SniperPortfolio) 
  private val driver = new AuctionSniperDriver(100) 
  
  @Test 
  def makesUserRequestWhenJoinButtonClicked(): Unit = { 
    val itemProbe = new ValueMatcherProbe[Item](equalTo(Item("an item-id", 789)), "item request")
    mainWindow.addUserRequestListener(itemProbe.setReceivedValue(_))
    
    driver.startBiddingWithStopPrice("an item-id", 789)
    driver.check(itemProbe)
  }
}
