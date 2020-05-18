package auctionsniper.ui

import javax.swing.SwingUtilities
import auctionsniper.{SniperListener, SniperSnapshot}

class SwingThreadSniperListener(delegate: SniperListener) extends SniperListener {
  def sniperStateChanged(snapshot: SniperSnapshot): Unit = {
    SwingUtilities.invokeLater(new Runnable() {
      def run(): Unit = {
        delegate.sniperStateChanged(snapshot)
      }
    })
  }
}