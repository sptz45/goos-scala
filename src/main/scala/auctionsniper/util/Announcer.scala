package auctionsniper.util

import scala.collection.mutable.ArrayBuffer
import java.lang.reflect.{InvocationHandler, InvocationTargetException, Method, Proxy}
import java.util.EventListener
import scala.annotation.targetName
import scala.reflect.ClassTag

class Announcer[T <: EventListener](using m: ClassTag[T]):
  require(m.runtimeClass.isInterface)
  
  private val listeners = ArrayBuffer[T]()
  
  private val proxy: T =
    val listenerType = m.runtimeClass
    Proxy.newProxyInstance(listenerType.getClassLoader, Array(listenerType), 
      new InvocationHandler():
        def invoke(aProxy: AnyRef, method: Method, args: Array[AnyRef]): AnyRef =
          doAnnounce(method, args)
          null
      ).asInstanceOf[T]

  
  private def doAnnounce(method: Method, args: Array[AnyRef]): Unit =
    try
      for listener <- listeners do
        method.invoke(listener, args:_*)
    catch
      case e: IllegalAccessException => throw IllegalArgumentException("could not invoke listener", e)
      case e: InvocationTargetException =>
        e.getCause match
          case cause: RuntimeException => throw cause
          case cause: Error => throw cause
          case cause => throw new UnsupportedOperationException("listener threw exception", cause)

  @targetName("appendOne")
  def +=(listener: T): Unit = listeners += listener

  @targetName("subtractOne")
  def -=(listener: T): Unit = listeners -= listener
  
  def announce(): T = proxy


object Announcer:
  def to[T <: EventListener](using ClassTag[T]): Announcer[T] = Announcer[T]
