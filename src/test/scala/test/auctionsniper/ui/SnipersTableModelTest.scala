
package test.auctionsniper.ui

import javax.swing.event.{TableModelEvent, TableModelListener}
import org.jmock.{Expectations, Mockery}
import org.jmock.integration.junit4.JMock
import org.junit.{Before, Test}
import org.junit.runner.RunWith

import org.hamcrest.Matchers._
import org.junit.Assert._

import auctionsniper._
import auctionsniper.UserRequestListener.Item
import auctionsniper.ui.{Column, SnipersTableModel}
import auctionsniper.util.Defect

@RunWith(classOf[JMock]) 
class SnipersTableModelTest { 
  private val ITEM_ID = "item 0"
  private val context = new Mockery
  private val listener = context.mock(classOf[TableModelListener])
  private val model = new SnipersTableModel
  private val sniper = new AuctionSniper(new Item(ITEM_ID, 234), null) 
  
  @Before
  def attachModelListener(): Unit = {
    model.addTableModelListener(listener) 
  } 
  
  @Test 
  def hasEnoughColumns(): Unit = { 
    assertThat(model.getColumnCount, equalTo(Column.values.length))
  }
  
  @Test
  def setsUpColumnHeadings(): Unit = { 
    for (column <- Column.values) { 
      assertEquals(column.name, model.getColumnName(column.ordinal))
    } 
  } 
  
  @Test
  def acceptsNewSniper(): Unit = {
    context.checking(new Expectations {
      oneOf(listener).tableChanged(`with`(anInsertionAtRow(0)))
    })

    model.sniperAdded(sniper);
    
    assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
  }
  
  @Test
  def setsSniperValuesInColumns(): Unit = {
    val bidding = sniper.snapshot.bidding(555, 666)
    context.checking(new Expectations {
      allowing(listener).tableChanged(`with`(anyInsertionEvent()))
      oneOf(listener).tableChanged(`with`(aChangeInRow(0)))
    })

    model.sniperAdded(sniper)
    model.sniperStateChanged(bidding)
    
    assertRowMatchesSnapshot(0, bidding)
  } 
  
  @Test
  def notifiesListenersWhenAddingASniper(): Unit = {
    context.checking(new Expectations {
      oneOf(listener).tableChanged(`with`(anInsertionAtRow(0)))
    })

    assertEquals(0, model.getRowCount())
    
    model.sniperAdded(sniper)
    
    assertEquals(1, model.getRowCount())
    assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
  }
  
  @Test
  def holdsSnipersInAdditionOrder(): Unit = {
    val sniper2 = new AuctionSniper(new Item("item 1", 345), null)
    context.checking(new Expectations {
      ignoring(listener)
    })
    
    model.sniperAdded(sniper)
    model.sniperAdded(sniper2)
    
    assertEquals(ITEM_ID, cellValue(0, Column.ITEM_IDENTIFIER))
    assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER))
  }
  
  @Test
  def updatesCorrectRowForSniper(): Unit = {
    val sniper2 = new AuctionSniper(new Item("item 1", 345), null)
    context.checking(new Expectations {
      allowing(listener).tableChanged(`with`(anyInsertionEvent()))

      oneOf(listener).tableChanged(`with`(aChangeInRow(1)))
    })
    
    model.sniperAdded(sniper)
    model.sniperAdded(sniper2)

    val winning1 = sniper2.snapshot.winning(123)
    model.sniperStateChanged(winning1)
    
    assertRowMatchesSnapshot(1, winning1)
  }

  @Test(expected=classOf[Defect])
  def throwsDefectIfNoExistingSniperForAnUpdate(): Unit = {
    model.sniperStateChanged(new SniperSnapshot("item 1", 123, 234, SniperState.WINNING))
  }
  
  private def assertRowMatchesSnapshot(row: Int, snapshot: SniperSnapshot): Unit = {
    assertEquals(snapshot.itemId, cellValue(row, Column.ITEM_IDENTIFIER))
    assertEquals(snapshot.lastPrice, cellValue(row, Column.LAST_PRICE))
    assertEquals(snapshot.lastBid, cellValue(row, Column.LAST_BID))
    assertEquals(SnipersTableModel.textFor(snapshot.state), cellValue(row, Column.SNIPER_STATE))
  }

  private def cellValue(rowIndex: Int, column: Column) =
    model.getValueAt(rowIndex, column.ordinal)

  private def anyInsertionEvent() = hasProperty("type", equalTo(TableModelEvent.INSERT))
  
  private def anInsertionAtRow(row: Int) =
    samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT))

  private def aChangeInRow(row: Int) = samePropertyValuesAs(new TableModelEvent(model, row)) 
} 
