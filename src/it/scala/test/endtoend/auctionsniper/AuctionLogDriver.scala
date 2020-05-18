package test.endtoend.auctionsniper

import java.io.File
import java.util.logging.LogManager
import org.apache.commons.io.FileUtils
import org.hamcrest.Matcher
import org.junit.Assert.assertThat

import auctionsniper.xmpp.XMPPAuctionHouse

class AuctionLogDriver {
  private val logFile = new File(XMPPAuctionHouse.LOG_FILE_NAME)

  def hasEntry(matcher: Matcher[String]): Unit = {
    assertThat(FileUtils.readFileToString(logFile), matcher) 
  }

  def clearLog(): Unit = {
    logFile.delete()
    LogManager.getLogManager().reset()
  }
}