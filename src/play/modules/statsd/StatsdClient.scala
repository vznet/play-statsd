package play.modules.statsd

import java.net.DatagramPacket
import play.Logger
import scala.util.Random

/**
 * Trait defining the statsd interface. It defines the two stats calls in
 * statsd: {@code increment} and {@code timing}. It must be instantiated with
 * {@link StatsdClientCake} which handles the sending of stats.
 */
private[statsd] trait StatsdClient {
  self: StatsdClientCake =>

  // Suffix for increment stats.
  val IncrementSuffix = "c"

  // Suffix for timing stats.
  private val TimingSuffix = "ms"

  // Random instance used for sampled rates.
  private lazy val random = new Random

  /**
   * Increment a given stat. Optionally give it a value and sampling rate.
   *
   * @param key The stat to be incremented.
   * @param value The amount by which to increment the stat. Defaults to 1.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   */
  def increment(key: String, value: Int = 1, samplingRate: Double = 1.0) {
    maybeSend(statFor(key, value, IncrementSuffix), samplingRate)
  }

  def timing(key: String, value: Int, samplingRate: Double = 1.0) {
    maybeSend(statFor(key, value, TimingSuffix), samplingRate)
  }

  private def statFor(key: String, value: Int, suffix: String): String = {
    "%s.%s:%s|c".format(statPrefix, key, value, suffix)
  }

  private def maybeSend(stat: String, samplingRate: Double) {
    if (samplingRate >= 1.0 || random.nextFloat() < samplingRate) {
      send(stat)
    }
  }
}

/**
 * Wrap the {@link StatsdClient} trait configured with {@link StatsdClientCake}
 * in a public object to make it available as a singleton to the app.
 */
object Statsd extends StatsdClient with StatsdClientCake {}