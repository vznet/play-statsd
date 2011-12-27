package play.modules.statsd

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import play.Logger

/**
 * Configuration trait for the {@link StatsdClient}.
 *
 * Provides to the client the prefix for all stats sent to statsd and mechanism
 * for sending stats over the network.
 */
private[statsd] trait StatsdClientCake {
  protected val statPrefix: String
  protected val send: Function1[String, Unit]
}

/**
 * Real implementation of {@link StatsdClientCake}.
 *
 * <p><ul>This implementation:
 * <li>Reads in values from {@code conf/application.conf}
 * <li>Sends stats using a {@link DatagramSocket} to statsd server.
 * <ul>
 */
private[statsd] trait RealStatsdClientCake extends StatsdClientCake {

  // The property name for whether or not the statsd sending should be enabled.
  private val StatsdEnabledProperty = "statsd.enabled"

  // The property name for the statsd port.
  private val PortProperty = "statsd.port"

  // The property name for the statsd host.
  private val HostnameProperty = "statsd.host"

  // The property name for the application stat prefix.
  private val StatPrefixProperty = "statsd.prefix"

  // The stat prefix used by the client.
  protected val statPrefix = play.configuration(StatPrefixProperty, "statsd")

  /**
   * Expose a {@code send} function to the client. Is configured with the
   */
  protected lazy val send: Function1[String, Unit] = {
    import Sugar._

    try {
      // Check if Statsd sending is enabled.
      val enabled = booleanConfig(StatsdEnabledProperty)
      if (enabled) {
        // Initialize the socket, host, and port to be used to send the data.
        val socket = new DatagramSocket
        val hostname = config(HostnameProperty)
        val host = InetAddress.getByName(hostname)
        val port = intConfig(PortProperty)

        // Return the real send function, partially applied with the
        // socket, host, and port so the client only has to call "send(stat)".
        socketSend(socket, host, port) _
      } else {
        Logger.warn("Send will be NOOP because %s is not enabled".format(
          StatsdEnabledProperty))
        noopSend _
      }

    } catch {
      // If there is any error configuring the send function, log a warning
      // but don't throw an error. Use a noop function for all sends.
      case t: Throwable =>
        Logger.warn(t, "Send will be NOOP because of configuration problem:")
        noopSend _
    }
  }

  /**
   * Send the stat in a {@link DatagramPacket} to statsd.
   */
  private def socketSend(
    socket: DatagramSocket, host: InetAddress, port: Int)(stat: String) {
    try {
      val data = stat.getBytes
      socket.send(new DatagramPacket(data, data.length, host, port))
    } catch {
      case t: Throwable =>
        Logger.warn(t, "Exception sending stat [%s] to [%s:%d]".format(
          stat, host.getHostName(), port))
    }
  }

  /**
   * Don't do anything. Used if statsd isn't enabled or on config errors.
   */
  private def noopSend(stat: String) = Unit
}
