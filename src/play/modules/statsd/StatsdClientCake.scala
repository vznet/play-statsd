package play.modules.statsd
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import play.Logger

private[statsd] trait StatsdClientCake {

  // The property name for the statsd port.
  private val PortProperty = "statsd.port"

  // The property name for the statsd host.
  private val HostnameProperty = "statsd.host"

  // The property name for the application stat prefix.
  private val StatPrefixProperty = "statsd.stat.prefix"

  // The stat prefix used by the client.
  protected val statPrefix = play.configuration(StatPrefixProperty, "statsd")

  /**
   * Expose a {@code send} function to the client. Is configured with the
   */
  protected lazy val send: Function1[String, Unit] = {
    try {

      // Initialize the socket, host, and port to be used to send the data.
      val socket = new DatagramSocket
      val hostname = play.configuration(HostnameProperty, "localhost")
      val host = InetAddress.getByName(hostname)
      val port = play.configuration(PortProperty).toInt

      // Return the real send function, partially applied with the
      // socket, host, and port so the client only has to call "send(stat)".
      socketSend(socket, host, port) _

    } catch {
      // If there is any error configuring the send function, log a warning
      // but don't throw an error. Use a noop function for all sends.
      case t: Throwable =>
        Logger.warn(t, "Could not configure statsd client. Send will be noop.")
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
   *
   */
  private def noopSend(stat: String) = Unit
}