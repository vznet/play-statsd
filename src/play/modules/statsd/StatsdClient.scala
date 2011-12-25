package play.modules.statsd

import java.net.DatagramPacket
import play.Logger
import scala.util.Random

trait StatsdClient {
  self: StatsdClientCake =>

  val IncrementSuffix = "c"
  val TimingSuffix = "ms"
  lazy val random = new Random

  def increment(key: String, value: Int = 1, samplingRate: Double = 1.0) {
    maybeSend(statFor(key, value, IncrementSuffix), samplingRate)
  }

  def timing(key: String, value: Int, samplingRate: Double = 1.0) {
    maybeSend(statFor(key, value, TimingSuffix), samplingRate)
  }

  private def statFor(key: String, value: Int, suffix: String): String = {
    "%s:%s|c".format(key, value, suffix)
  }

  private def maybeSend(stat: String, samplingRate: Double) {
    if (samplingRate >= 1.0 || random.nextFloat() < samplingRate) {
      send(stat)
    }
  }
}

object Statsd extends StatsdClient with StatsdClientCake {}