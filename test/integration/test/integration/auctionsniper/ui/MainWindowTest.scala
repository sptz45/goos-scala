package test.integration.auctionsniper.ui

import com.objogate.wl.swing.probe.ValueMatcherProbe
import org.junit.Test
import org.hamcrest.Matchers.equalTo

import test.endtoend.auctionsniper.AuctionSniperDriver
import auctionsniper.{UserRequestListener, SniperPortfolio}
import auctionsniper.UserRequestListener.Item
import auctionsniper.ui.MainWindow

class MainWindowTest {
  private val mainWindow = new MainWindow(new SniperPortfolio) 
  private val driver = new AuctionSniperDriver(100) 
  
  @Test 
  def makesUserRequestWhenJoinButtonClicked() { 
    val itemProbe = new ValueMatcherProbe[Item](equalTo(new Item("an item-id", 789)), "item request")
    mainWindow.addUserRequestListener(new UserRequestListener() {
      def joinAuction(item: Item) {
        itemProbe.setReceivedValue(item)
      }
    })
    
    driver.startBiddingWithStopPrice("an item-id", 789)
    driver.check(itemProbe)
  }
}
