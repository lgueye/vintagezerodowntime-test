import java.time.Instant

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class CreateMeasurementFacts extends Simulation {

  def timestampRef() = Instant.now.toEpochMilli
  val baseUrl = System.getProperty("vintagezerodowntime.engine.server.api.url")
  val usersPerSec = System.getProperty("gatling.users.per.second").toDouble
  val injectionDurationInMinutes = System.getProperty("gatling.scenario.duration.in.minutes").toDouble

  object HeartRateFact {

    def valueRef() = Random.nextInt(220 - 80) + 80
    val post = exec(http("new")
      .post("/api/providers/medrate/facts")
      .body(StringBody(session => s"""{ "measurement": "heart_rate", "provider": "medrate", "deviceBusinessId": "D-8563461", "value": "${valueRef()}", "timestamp": "${timestampRef()}" }""")))

  }

  object RespirationRateFact {

    def valueRef() = Random.nextInt(20 - 12) + 12
    val post = exec(http("new")
      .post("/api/providers/medrate/facts")
      .body(StringBody(session => s"""{ "measurement": "respiration_rate", "provider": "medrate", "deviceBusinessId": "D-8563461", "value": "${valueRef()}", "timestamp": "${timestampRef()}" }""")))

  }

  val httpConf = http
    .baseURL(baseUrl)
    .header("Content-Type", "application/json")
    .acceptHeader("application/json;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val postMeasurements = scenario("devices").exec(HeartRateFact.post, RespirationRateFact.post)

  setUp(
    postMeasurements.inject(constantUsersPerSec(usersPerSec) during(injectionDurationInMinutes minutes))
  ).protocols(httpConf)
}
