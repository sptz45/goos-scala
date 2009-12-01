package auctionsniper.ui

import javax.swing.SwingUtilities
import auctionsniper.{SniperListener, SniperSnapshot}

class SwingThreadSniperListener(delegate: SniperListener) extends SniperListener {
  def sniperStateChanged(snapshot: SniperSnapshot) {
    SwingUtilities.invokeLater(new Runnable() {
      def run() {
        delegate.sniperStateChanged(snapshot)
      }
    })
  }
}
