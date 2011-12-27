package play.modules.statsd

/**
 * Sugar to make using Java API for Play nicer.
 */
object Sugar {
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
}