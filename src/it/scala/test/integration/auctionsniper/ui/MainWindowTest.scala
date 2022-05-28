package test.integration.auctionsniper.ui

import auctionsniper.SniperPortfolio
import auctionsniper.UserRequestListener.Item
import auctionsniper.ui.MainWindow
import com.objogate.wl.swing.probe.ValueMatcherProbe
import munit.FunSuite
import org.hamcrest.Matchers.equalTo
import test.endtoend.auctionsniper.AuctionSniperDriver

class MainWindowTest extends FunSuite:

  private val mainWindow = new MainWindow(new SniperPortfolio) 
  private val driver = new AuctionSniperDriver(100) 

  test("makes user request when join button clicked") {
    val itemProbe = new ValueMatcherProbe[Item](equalTo(Item("an item-id", 789)), "item request")
    mainWindow.addUserRequestListener(itemProbe.setReceivedValue(_))
    
    driver.startBiddingWithStopPrice("an item-id", 789)
    driver.check(itemProbe)
  }
