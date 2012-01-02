package test

import org.scalatest.matchers.ShouldMatchers
import play.modules.statsd.StatsdClient
import play.modules.statsd.StatsdClientCake
import play.test.UnitFlatSpec
import org.scalatest.matchers.MustMatchers

object RememberingFunction extends Function1[String, Unit] {
  var lastInput: String = _

  override def apply(input: String) {
    lastInput = input
  }
}

trait MockStatsdClientCake extends StatsdClientCake {
  override val statPrefix: String = "test"
  override val send: Function1[String, Unit] = RememberingFunction
}

object Client extends StatsdClient with MockStatsdClientCake

class StatsdClientSpec extends UnitFlatSpec with ShouldMatchers with MustMatchers {

  it should "encode counter stats properly" in {
    statFor { Client.increment("count", 1) } must be === "test.count:1|c"
    statFor { Client.increment("count", 10) } must be === "test.count:10|c"
  }

  it should "encode timing stats properly" in {
    statFor { Client.timing("timed", 1) } must be === "test.timed:1|ms"
    statFor { Client.timing("timed", 5) } must be === "test.timed:5|ms"
  }

  def statFor(call: => Unit): String = {
    call
    RememberingFunction.lastInput
  }
}
