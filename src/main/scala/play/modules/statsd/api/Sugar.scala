package play.modules.statsd.api
import play.api.Play

/**
 * Sugar to make using Java API for Play nicer.
 */
object please {
  private[api] def config(name: String): String = {
    Play.current.configuration.getString(name) getOrElse {
      throw new IllegalStateException("[%s] prop is null".format(name))
    }
  }

  private[api] def booleanConfig(name: String): Boolean = {
    Play.current.configuration.getBoolean(name).getOrElse(false)
  }

  private[api] def intConfig(name: String): Int = {
    Integer.parseInt(config(name))
  }
}