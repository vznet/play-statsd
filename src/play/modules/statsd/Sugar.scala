package play.modules.statsd
import play.Logger
import org.apache.commons.lang.exception.ExceptionUtils

/**
 * Sugar to make using Java API for Play nicer.
 */
object please {
  private[statsd] def config(name: String): String = {
    checkNotNull(play.configuration(name), "[%s] prop is null".format(name))
  }

  private[statsd] def booleanConfig(name: String): Boolean = {
    "true" equals config(name).toLowerCase
  }

  private[statsd] def intConfig(name: String): Int = {
    Integer.parseInt(config(name))
  }

  private[statsd] def checkNotNull[T](ref: T, message: String = ""): T = {
    if (ref == null) {
      throw new IllegalStateException(message)
    }
    ref
  }

  def report(error: Throwable): Unit = this report error -> ""

  def report(pair: (Throwable, String)) {
    Logger.error(ExceptionUtils.getStackTrace(pair._1), pair._2)
  }

  def warn(pair: (Throwable, String)) {
    Logger.warn(ExceptionUtils.getStackTrace(pair._1), pair._2)
  }
}