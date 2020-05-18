package auctionsniper.ui

import javax.swing.table.AbstractTableModel
import scala.collection.mutable.ArrayBuffer

import auctionsniper.{AuctionSniper, SniperListener, SniperSnapshot, SniperState}
import auctionsniper.SniperPortfolio.PortfolioListener
import auctionsniper.util.Defect

class SnipersTableModel extends AbstractTableModel with SniperListener with PortfolioListener {
  
  private val snapshots = new ArrayBuffer[SniperSnapshot]
  
  def getColumnCount = Column.values.length
  
  def getRowCount = snapshots.size

  override def getColumnName(column: Int) = Column.at(column).name

  def getValueAt(rowIndex: Int, columnIndex: Int) =
    Column.at(columnIndex).valueIn(snapshots(rowIndex)).asInstanceOf[Object]
  
  def sniperStateChanged(newSnapshot: SniperSnapshot) {
    val updated = snapshots.indexWhere(s => newSnapshot.isForSameItemAs(s))
    if (updated == -1) throw new Defect("No existing Sniper state for " + newSnapshot.itemId)
    snapshots(updated) = newSnapshot
    fireTableRowsUpdated(updated, updated)
  }

  def sniperAdded(sniper: AuctionSniper) {
    addSniperSnapshot(sniper.snapshot)
    sniper.addSniperListener(new SwingThreadSniperListener(this))
  } 

  def addSniperSnapshot(newSniper: SniperSnapshot) {
    snapshots += newSniper
    val row = snapshots.size - 1
    fireTableRowsInserted(row, row)
  }
}

object SnipersTableModel {
  private val STATUS_TEXT = Array(
    "Joining", "Bidding", "Winning", "Losing", "Lost", "Won", "Failed")
  
  def textFor(state: SniperState) = STATUS_TEXT(state.ordinal)
}