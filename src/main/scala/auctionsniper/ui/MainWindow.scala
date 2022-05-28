package auctionsniper.ui

import java.text.NumberFormat
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.*
import javax.swing.*
import auctionsniper.*
import auctionsniper.UserRequestListener.Item
import auctionsniper.util.Announcer

import scala.util.chaining.scalaUtilChainingOps

class MainWindow(portfolio: SniperPortfolio) extends JFrame("Auction Sniper"):
  import MainWindow.*
  
  private val userRequests = Announcer.to[UserRequestListener]
  
  setName(MainWindow.MAIN_WINDOW_NAME)
  fillContentPane(makeSnipersTable(portfolio), makeControls())
  pack() 
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE) 
  setVisible(true)
  
  def addUserRequestListener(listener: UserRequestListener): Unit =
    userRequests += listener
  
  private def fillContentPane(snipersTable: JTable, controls: JPanel): Unit =
    val contentPane = getContentPane
    contentPane.setLayout(BorderLayout())
    contentPane.add(controls, BorderLayout.NORTH)
    contentPane.add(JScrollPane(snipersTable), BorderLayout.CENTER)

  private def makeControls() =
    val itemIdField = makeItemIdField()
    val stopPriceField = makeStopPriceField()

    val controls = JPanel(FlowLayout()).tap { c =>
      c.add(itemIdField)
      c.add(stopPriceField)
    }

    val joinAuctionButton = JButton("Join Auction").tap { button =>
      button.setName(JOIN_BUTTON_NAME)

      button.addActionListener(new ActionListener():
        def actionPerformed(e: ActionEvent): Unit =
          userRequests.announce().joinAuction(Item(itemId, stopPrice))

        private def itemId = itemIdField.getText
        private def stopPrice = stopPriceField.getValue.asInstanceOf[Number].intValue
      )
    }

    controls.add(joinAuctionButton)
    controls
  
  private def makeItemIdField() =
    JTextField().tap { itemIdField =>
      itemIdField.setColumns(10)
      itemIdField.setName(NEW_ITEM_ID_NAME)
    }

  private def makeStopPriceField() =
    JFormattedTextField(NumberFormat.getIntegerInstance).tap { stopPriceField =>
      stopPriceField.setColumns(7)
      stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME)
    }

  private def makeSnipersTable(portfolio: SniperPortfolio) =
    val model = SnipersTableModel()
    portfolio.addPortfolioListener(model)
    new JTable(model).tap { table => table.setName(SNIPERS_TABLE_NAME) }


object MainWindow:
  private val SNIPERS_TABLE_NAME = "Snipers Table"
  val APPLICATION_TITLE = "Auction Sniper"
  val MAIN_WINDOW_NAME = "Auction Sniper Main"
  val NEW_ITEM_ID_NAME = "item id"
  val JOIN_BUTTON_NAME = "join button"
  val NEW_ITEM_STOP_PRICE_NAME = "stop price"
