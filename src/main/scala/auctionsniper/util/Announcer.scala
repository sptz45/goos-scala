package auctionsniper.util

import scala.collection.mutable.ArrayBuffer
import java.lang.reflect.{InvocationHandler, InvocationTargetException, Method, Proxy}
import java.util.EventListener

class Announcer[T <: EventListener](implicit m: Manifest[T]) {
  require(m.runtimeClass.isInterface)
  
  private val listeners = new ArrayBuffer[T]
  
  private val proxy: T = {
    val listenerType = m.runtimeClass
    Proxy.newProxyInstance(listenerType.getClassLoader, Array(listenerType), 
      new InvocationHandler() {
        def invoke(aProxy: AnyRef, method: Method, args: Array[AnyRef]): AnyRef = {
          doAnnounce(method, args)
          null
        }
      }).asInstanceOf[T]
  }
  
  private def doAnnounce(method: Method, args: Array[AnyRef]): Unit = {
    try {
      for (listener <- listeners)
        method.invoke(listener, args:_*)
    } catch {
      case e: IllegalAccessException => throw new IllegalArgumentException("could not invoke listener", e)
      case e: InvocationTargetException =>
        e.getCause match {
          case cause: RuntimeException => throw cause
          case cause: Error => throw cause
          case cause => throw new UnsupportedOperationException("listener threw exception", cause)
       }
    }
  }
  
  def +=(listener: T): Unit = {
    listeners += listener
  }
  
  def -=(listener: T): Unit = {
    listeners -= listener
  }
  
  def announce(): T = proxy
}

object Announcer {
  def to[T <: EventListener](implicit m: Manifest[T]) = new Announcer[T]
}
