package auctionsniper

import java.util.EventListener

trait UserRequestListener extends EventListener {
  
  def joinAuction(item: UserRequestListener.Item)
}

object UserRequestListener {
  
  case class Item(identifier: String, stopPrice: Int) {
    
    def allowsBid(bid: Int) = bid <= stopPrice
  }
}
