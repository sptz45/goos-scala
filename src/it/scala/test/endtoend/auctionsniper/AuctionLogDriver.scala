package test.endtoend.auctionsniper

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.logging.LogManager

import org.apache.commons.io.FileUtils
import org.hamcrest.Matcher
import auctionsniper.xmpp.XMPPAuctionHouse
import org.hamcrest.MatcherAssert.assertThat

class AuctionLogDriver:

  private val logFile = File(XMPPAuctionHouse.LOG_FILE_NAME)

  def hasEntry(matcher: Matcher[String]): Unit =
    assertThat(FileUtils.readFileToString(logFile, StandardCharsets.UTF_8), matcher)

  def clearLog(): Unit =
    logFile.delete()
    LogManager.getLogManager.reset()
