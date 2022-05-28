
package test.auctionsniper.ui

import auctionsniper.UserRequestListener.Item
import auctionsniper.*
import auctionsniper.ui.{Column, SnipersTableModel}
import auctionsniper.util.Defect
import javax.swing.event.{TableModelEvent, TableModelListener}
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.jmock.Expectations
import scala.util.chaining.scalaUtilChainingOps
import test.fixtures.JMockSuite


class SnipersTableModelTest extends JMockSuite:

  private val ITEM_ID = "item 0"
  private val sniper = AuctionSniper(Item(ITEM_ID, 234), null)

  private var model = SnipersTableModel()

  private val withListener = FunFixture[TableModelListener](
    setup = _ => {
      context().mock(classOf[TableModelListener]).tap {listener =>
        model.addTableModelListener(listener)
      }
    },
    teardown = _ => ()
  )

  override def afterEach(context: AfterEach): Unit =
    model = SnipersTableModel()

  test("has enough columns") {
    assertThat(model.getColumnCount, equalTo(Column.values.length))
  }

  test("setsUpColumnHeadings") {
    for column <- Column.values do
      assertEquals(column.name, model.getColumnName(column.ordinal))
  }

  withListener.test("accepts new sniper") { listener =>
    context().checking(new Expectations:
      oneOf(listener).tableChanged(`with`(anInsertionAtRow(0)))
    )

    model.sniperAdded(sniper)

    assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
  }

  withListener.test("sets sniper values in columns") { listener =>
    val bidding = sniper.snapshot.bidding(555, 666)
    context().checking(new Expectations:
      allowing(listener).tableChanged(`with`(anyInsertionEvent()))
      oneOf(listener).tableChanged(`with`(aChangeInRow(0)))
    )

    model.sniperAdded(sniper)
    model.sniperStateChanged(bidding)

    assertRowMatchesSnapshot(0, bidding)
  }

  withListener.test("notifies listeners when adding a sniper") { listener =>
    context().checking(new Expectations:
      oneOf(listener).tableChanged(`with`(anInsertionAtRow(0)))
    )

    assertEquals(0, model.getRowCount())

    model.sniperAdded(sniper)

    assertEquals(model.getRowCount(), 1)
    assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
  }

  withListener.test("holds snipers in addition order") { listener =>
    val sniper2 = AuctionSniper(Item("item 1", 345), null)
    context().checking(new Expectations:
      ignoring(listener)
    )

    model.sniperAdded(sniper)
    model.sniperAdded(sniper2)

    assert(clue(cellValue(0, Column.ITEM_IDENTIFIER)) == ITEM_ID)
    assert(clue(cellValue(1, Column.ITEM_IDENTIFIER)) == "item 1")
  }

  withListener.test("updates correct row for sniper") { listener =>
    val sniper2 = AuctionSniper(Item("item 1", 345), null)
    context().checking(new Expectations:
      allowing(listener).tableChanged(`with`(anyInsertionEvent()))

      oneOf(listener).tableChanged(`with`(aChangeInRow(1)))
    )

    model.sniperAdded(sniper)
    model.sniperAdded(sniper2)

    val winning1 = sniper2.snapshot.winning(123)
    model.sniperStateChanged(winning1)

    assertRowMatchesSnapshot(1, winning1)
  }

  test("throws defect if no existing sniper for an update") {
    intercept[Defect] {
      model.sniperStateChanged(SniperSnapshot("item 1", 123, 234, SniperState.WINNING))
    }
  }

  private def assertRowMatchesSnapshot(row: Int, snapshot: SniperSnapshot): Unit =
    assertEquals(cellValue(row, Column.ITEM_IDENTIFIER), snapshot.itemId)
    assert(clue(cellValue(row, Column.LAST_PRICE).toString) == clue(snapshot.lastPrice.toString))
    assert(clue(cellValue(row, Column.LAST_BID).toString) == clue(snapshot.lastBid.toString))
    assertEquals(cellValue(row, Column.SNIPER_STATE), SnipersTableModel.textFor(snapshot.state))

  private def cellValue(rowIndex: Int, column: Column) = model.getValueAt(rowIndex, column.ordinal)

  private def anyInsertionEvent() = hasProperty("type", equalTo(TableModelEvent.INSERT))

  private def anInsertionAtRow(row: Int) =
    samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT))

  private def aChangeInRow(row: Int) = samePropertyValuesAs(TableModelEvent(model, row))
