package test.auctionsniper.xmpp

import java.util.logging.{LogManager, Logger}

import auctionsniper.xmpp.LoggingXMPPFailureReporter
import org.jmock.imposters.ByteBuddyClassImposteriser
import org.jmock.{Expectations, Mockery}
import test.fixtures.JMockSuite

class LoggingXMPPFailureReporterTest extends JMockSuite:

  override def configureJMock(mockery: Mockery): Unit =
    mockery.setImposteriser(ByteBuddyClassImposteriser.INSTANCE)

  override def afterEach(context: AfterEach): Unit =
    LogManager.getLogManager.reset()

  test("writes message translation failure to log") {
    val logger = context().mock(classOf[Logger])
    val reporter = new LoggingXMPPFailureReporter(logger)

    context().checking(new Expectations:
      oneOf(logger).severe("<auction id> " 
                         + "Could not translate message \"bad message\" " 
                         + "because \"java.lang.Exception: an exception\"") 
    )
    
    reporter.cannotTranslateMessage("auction id", "bad message", Exception("an exception"))
  }
