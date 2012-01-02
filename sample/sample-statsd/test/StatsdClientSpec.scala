package test

import org.scalatest.matchers.ShouldMatchers
import play.modules.statsd.StatsdClient
import play.modules.statsd.StatsdClientCake
import play.test.UnitFlatSpec
import org.scalatest.matchers.MustMatchers
import scala.collection.mutable.ArrayBuffer
import org.mockito.Mockito._
import scala.util.Random

// TODO(dyross) This is NOT parallelizable!

object SillyClock {
  val stream: ArrayBuffer[Long] = ArrayBuffer()
  val iterator = stream.iterator

  def now(): Long = iterator.next()
}

trait Clock {
  def now(): Long
}

trait Sender extends Function1[String, Unit]

trait MockStatsdClientCake extends StatsdClientCake {
  val clock = mock(classOf[Clock])
  override def now(): Long = clock.now()

  val random = mock(classOf[Random])
  override def nextFloat(): Float = random.nextFloat()

  override val statPrefix: String = "test"

  val sender = mock(classOf[Sender])
  override val send: Function1[String, Unit] = sender
}

class StatsdClientSpec extends UnitFlatSpec with ShouldMatchers {

  it should "encode counter stats" in {
    val client = makeClient

    expect(client.sender -> "test.count:1|c") { client.increment("count", 1) }
    expect(client.sender -> "test.count:99|c") { client.increment("count", 99) }
  }

  it should "encode timing stats" in {
    val client = makeClient

    expect(client.sender -> "test.timed:1|ms") { client.timing("timed", 1) }
    expect(client.sender -> "test.timed:99|ms") { client.timing("timed", 99) }
  }

  it should "measure time" in {
    val client = makeClient

    expect(client.sender -> "test.timed:250|ms") {
      when(client.clock.now()).thenReturn(99000, 99250)
      client.time("timed") {
        // No-op
      }
    }
  }

  it should "work with sampling rates" in {
    val client = makeClient

    expect(client.sender -> "test.count:2|c") {
      when(client.random.nextFloat()).thenReturn(0.5f)
      client.increment("count", 1, 0.4)
      client.increment("count", 1, 0.5)
      client.increment("count", 2, 0.51)
    }
  }

  def makeClient = new StatsdClient with MockStatsdClientCake

  def expect[A, B](pair: (Function1[A, B], A))(call: => Unit) {
    call
    verify(pair._1).apply(pair._2)
  }
}
