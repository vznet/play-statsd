
import play.jobs.Every
import play.modules.statsd.Statsd
import play.jobs.Job
import play.Logger

@Every("1s")
class SomeJob extends Job {
  override def doJob() {
    Logger.info("doing job...")
    Statsd.increment("test1", 50)
    Statsd.timing("test2", 1000)
    Statsd.time("test3") {
      // do work
    }
  }
}
