package test.auctionsniper.xmpp

import java.util.logging.{LogManager, Logger}

import org.jmock.{Expectations, Mockery}
import org.jmock.integration.junit4.JMock
import org.jmock.lib.legacy.ClassImposteriser
import org.junit.{After, Test}
import org.junit.runner.RunWith

import auctionsniper.xmpp.LoggingXMPPFailureReporter

@RunWith(classOf[JMock]) 
class LoggingXMPPFailureReporterTest {
  
  val context = new Mockery { 
    setImposteriser(ClassImposteriser.INSTANCE) 
  } 
  val logger = context.mock(classOf[Logger]) 
  val reporter = new LoggingXMPPFailureReporter(logger)
  
  @After
  def resetLogging(): Unit = { 
    LogManager.getLogManager.reset()
  } 
  
  @Test 
  def writesMessageTranslationFailureToLog(): Unit = { 
    context.checking(new Expectations { 
      oneOf(logger).severe("<auction id> " 
                         + "Could not translate message \"bad message\" " 
                         + "because \"java.lang.Exception: an exception\"") 
    }) 
    
    reporter.cannotTranslateMessage("auction id", "bad message", new Exception("an exception")) 
  } 
} 
