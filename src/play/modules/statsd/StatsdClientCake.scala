package play.modules.statsd
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import play.Logger

trait StatsdClientCake {

  private val PortProperty = "statsd.port"
  private val HostnameProperty = "statsd.host"

  protected lazy val send: Function1[String, Unit] = {
    try {
      val socket = new DatagramSocket
      val hostname = play.configuration(HostnameProperty, "localhost")
      val host = InetAddress.getByName(hostname)
      val port = play.configuration(PortProperty).toInt
      socketSend(socket, host, port) _
    } catch {
      case t: Throwable => noopSend _
    }
  }

  private[this] def socketSend(
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

  private[this] def noopSend(stat: String) = Unit
}