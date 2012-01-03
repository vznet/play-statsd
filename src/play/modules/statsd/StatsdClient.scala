package play.modules.statsd

import java.net.DatagramPacket
import play.Logger
import scala.util.Random

/**
 * Trait defining the statsd interface. It defines the two stats calls in
 * statsd: {@code increment} and {@code timing}. It must be instantiated with
 * {@link StatsdClientCake} which handles the sending of stats over the network.
 *
 * <p><ul>Two stats-related function are supported.
 * <li><b>increment</b>: Increment a given stat key.
 * <li><b>timing</b>: Sending timing info for a given operation key.
 * </ul>
 *
 * <p>For both, an optional {@code samplingRate} parameter can be provided.
 * For parameters between 0 and 1.0, the client will send the stat
 * {@code (samplingRate * 100)%} of the time. This is useful for some stats that
 * occur extremely frequently and therefore put too much load on the statsd
 * server.
 *
 * <p>The functionality is exposed to Play Apps using the {@link Statsd} object.
 */
trait StatsdClient {
  self: StatsdClientCake =>

  // Suffix for increment stats.
  private val IncrementSuffix = "c"

  // Suffix for timing stats.
  private val TimingSuffix = "ms"

  /**
   * Increment a given stat key. Optionally give it a value and sampling rate.
   *
   * @param key The stat key to be incremented.
   * @param value The amount by which to increment the stat. Defaults to 1.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   */
  def increment(key: String, value: Long = 1, samplingRate: Double = 1.0) {
    safely { maybeSend(statFor(key, value, IncrementSuffix), samplingRate) }
  }

  /**
   * Timing data for given stat key. Optionally give it a sampling rate.
   *
   * @param key The stat key to be timed.
   * @param value The number of milliseconds the operation took.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   */
  def timing(key: String, millis: Long, samplingRate: Double = 1.0) {
    safely { maybeSend(statFor(key, millis, TimingSuffix), samplingRate) }
  }

  /**
   * Time a given operation and send the resulting stat.
   *
   * @param key The stat key to be timed.
   * @param samplingRate The probability for which to increment. Defaults to 1.
   * @param operation An arbitrary block of code to be timed.
   * @return The result of the timed operation.
   */
  def time[T](key: String, samplingRate: Double = 1.0)(timed: => T): T = {
    val start = now()
    val result = timed
    val finish = now()
    timing(key, finish - start, samplingRate)
    result
  }

  /*
   * ****************************************************************
   *                PRIVATE IMPLEMENTATION DETAILS
   * ****************************************************************
   */

  /*
   * Creates the stat string to send to statsd.
   * For counters, it provides something like {@code key:value|c}.
   * For timing, it provides something like {@code key:millis|ms}.
   */
  private def statFor(key: String, value: Long, suffix: String): String = {
    "%s.%s:%s|%s".format(statPrefix, key, value, suffix)
  }

  /*
   * Probabilistically calls the {@code send} function. If the sampling rate
   * is 1.0 or greater, we always call send. Use a random number call send
   * function {@code (samplingRate * 100)%} of the time.
   */
  private def maybeSend(stat: String, samplingRate: Double) {
    if (samplingRate >= 1.0 || nextFloat() < samplingRate) {
      send(stat)
    }
  }

  /*
   * Safety net for operations that shouldn't throw exceptions.
   */
  private def safely(operation: => Unit) {
    try {
      operation
    } catch {
      case error => please warn error -> "Unhandled throwable sending stat."
    }
  }
}

/**
 * Wrap the {@link StatsdClient} trait configured with
 * {@link RealStatsdClientCake} in an object to make it available to the app.
 */
object Statsd extends StatsdClient with RealStatsdClientCake
