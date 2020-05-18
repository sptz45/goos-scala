package auctionsniper.ui

import java.text.NumberFormat
import java.awt.event.{ActionListener, ActionEvent}
import java.awt._
import javax.swing._

import auctionsniper._
import auctionsniper.UserRequestListener.Item

class MainWindow(portfolio: SniperPortfolio) extends JFrame("Auction Sniper") {
  import MainWindow._
  
  val userRequests = util.Announcer.to[UserRequestListener]
  
  setName(MainWindow.MAIN_WINDOW_NAME)
  fillContentPane(makeSnipersTable(portfolio), makeControls())
  pack() 
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE) 
  setVisible(true)
  
  def addUserRequestListener(listener: UserRequestListener) {
    userRequests += listener
  }
  
  private def fillContentPane(snipersTable: JTable, controls: JPanel) { 
    val contentPane = getContentPane 
    contentPane.setLayout(new BorderLayout) 
    contentPane.add(controls, BorderLayout.NORTH) 
    contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER) 
  }
  
  private def makeControls() = { 
    val itemIdField = makeItemIdField()
    val stopPriceField = makeStopPriceField()

    val controls = new JPanel(new FlowLayout) 
    controls.add(itemIdField)
    controls.add(stopPriceField)

    val joinAuctionButton = new JButton("Join Auction") 
    joinAuctionButton.setName(JOIN_BUTTON_NAME) 
    
    joinAuctionButton.addActionListener(new ActionListener() { 
      def actionPerformed(e: ActionEvent) { 
        userRequests.announce().joinAuction(new Item(itemId, stopPrice)) 
      } 
      private def itemId = itemIdField.getText
      private def stopPrice = stopPriceField.getValue.asInstanceOf[Number].intValue 
    })
    controls.add(joinAuctionButton)
    controls
  }
  
  private def makeItemIdField() = {
    val itemIdField = new JTextField
    itemIdField.setColumns(10)
    itemIdField.setName(NEW_ITEM_ID_NAME)
    itemIdField
  }

  private def makeStopPriceField() = {
    val stopPriceField = new JFormattedTextField(NumberFormat.getIntegerInstance)
    stopPriceField.setColumns(7)
    stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME)
    stopPriceField
  }

  private def makeSnipersTable(portfolio: SniperPortfolio) = { 
    val model = new SnipersTableModel
    portfolio.addPortfolioListener(model)
    val snipersTable = new JTable(model) 
    snipersTable.setName(SNIPERS_TABLE_NAME) 
    snipersTable
  }
}


object MainWindow {
  private val SNIPERS_TABLE_NAME = "Snipers Table"
  val APPLICATION_TITLE = "Auction Sniper"
  val MAIN_WINDOW_NAME = "Auction Sniper Main"
  val NEW_ITEM_ID_NAME = "item id"
  val JOIN_BUTTON_NAME = "join button"
  val NEW_ITEM_STOP_PRICE_NAME = "stop price"
}
