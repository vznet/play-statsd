
import play.jobs.Every
import play.modules.statsd.Statsd
import play.jobs.Job

@Every("1s")
class SomeJob extends Job {
  override def doJob() {
    Statsd.increment("test1", 50)
    Statsd.timing("test2", 1000)
  }
}