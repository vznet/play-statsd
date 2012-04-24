package play.modules.statsd.api

import play.api.test.FakeApplication
import java.net.{SocketTimeoutException, DatagramPacket, DatagramSocket}
import play.api.test.Helpers.running
import org.specs2.mutable.{BeforeAfter, Specification, After}

case class StatsdSpec() extends Specification {
  "Statsd" should {
    "send increment by one message" in new Setup {
      running(fakeApp) {
        println("Executing 1")
        mockStatsd
        Statsd.increment("test")
        receive() mustEqual "statsd.test:1|c"
      }
    }
    "send increment by more message" in new Setup {
      running(fakeApp) {
        println("Executing 2")
        mockStatsd
        Statsd.increment("test", 10)
        receive() mustEqual "statsd.test:10|c"
      }
    }
    "hopefully send a message when sampling rate is only just below 1" in new Setup {
      running(fakeApp) {
        println("Executing 3")
        mockStatsd
        Statsd.increment("test", 10, 0.9999999999)
        receive() mustEqual "statsd.test:10|c|@0.999999"
      }
    }
  }

  trait Setup extends BeforeAfter {
    val PORT = 57475;
    val fakeApp = FakeApplication(additionalConfiguration = Map(
      "ehcacheplugin" -> "disabled",
      "statsd.enabled" -> "true",
      "statsd.host" -> "localhost",
      "statsd.port" -> PORT.toString))
    lazy val mockStatsd = {
      println("Starting mock")
      val socket = new DatagramSocket(PORT)
      socket.setSoTimeout(1000)
      socket
    }

    def receive() = {
      val buf: Array[Byte] = new Array[Byte](1024)
      val packet = new DatagramPacket(buf, buf.length)
      try {
        mockStatsd.receive(packet)
      }
      catch {
        case s: SocketTimeoutException => failure("Didn't receive message within one second")
      }
      new String(packet.getData, 0, packet.getLength)
    }

    def before {
      println("Executing before")
      // mockStatsd
    }

    def after {
      println("Stopping mock")
      mockStatsd.close()
    }
  }


}
