package com.github.tkqubo.akka_open_graph_fetcher

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import com.miguno.akka.testing.VirtualTime
import org.openqa.selenium.{By, NoSuchElementException, WebDriver, WebElement}
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}
import scala.util.Random
import scalaz.syntax.std.all._

/**
  * Test class for [[WebDriverOpenGraphFetcher]]
  * {{{
  * sbt "test-only com.github.tkqubo.akka_open_graph_fetcher.WebDriverOpenGraphFetcherTest"
  * }}}
  */
// scalastyle:off magic.number
class WebDriverOpenGraphFetcherTest
  extends Specification
  with Mockito {

  "WebDriverOpenGraphFetcher" should {
    "fetch(url: String)" should {
      "pass" in new Context {
        // Given
        val url = "http://example.com"
        doNothing.when(driver).get(url)
        driver.getPageSource returns "page source"
        val url2: String = assumeMetaTag(driver, "url")
        val title: String = assumeMetaTag(driver, "title")
        val description: String = assumeMetaTag(driver, "description")
        val image: String = assumeMetaTag(driver, "image")
        val expected = OpenGraph(url2, title.some, description.some, image.some)
        doNothing.when(driver).quit()

        // When
        val actual = Await.result(target.fetch(url), Duration.Inf)

        // Then
        actual === expected
        there was one(driver).get(any)
        there was one(driver).getPageSource
        there was one(driver).quit()
        Seq("url", "title", "description", "image").forall { name =>
          there was one(driver).findElement(By.xpath(s"//meta[@property='og:$name']"))
        }
        there was noMoreCallsTo(driver)
      }

      "pass with falling back to the default values" in new Context {
        // Given
        val url = "http://example.com"
        doNothing.when(driver).get(url)
        driver.getPageSource returns "page source"
        assumeNoMetaTag(driver, "url")
        assumeNoMetaTag(driver, "title")
        assumeNoMetaTag(driver, "description")
        assumeNoMetaTag(driver, "image")
        val expected = OpenGraph(url)
        doNothing.when(driver).quit()

        // When
        val actual = Await.result(target.fetch(url), Duration.Inf)

        // Then
        actual === expected
        there was one(driver).get(any)
        there was one(driver).getPageSource
        there was one(driver).quit()
        Seq("url", "title", "description", "image").forall { name =>
          there was one(driver).findElement(By.xpath(s"//meta[@property='og:$name']"))
        }
        there was noMoreCallsTo(driver)
      }

      "fail with invalid URL" in new Context {
        // Given
        val url: String = "invalid"
        val expected: OpenGraph = OpenGraph(
          url, error = Error.maybeFromStatusCode(StatusCodes.ServiceUnavailable)
        )
        doNothing.when(driver).get(url)
        doThrow(new NoSuchElementException("invalid URL")).when(driver).getPageSource

        // When
        val actual = Await.result(target.fetch(url), Duration.Inf)

        // Then
        actual === expected
        there was one(driver).get(any)
        there was one(driver).getPageSource
        there was one(driver).quit()
        there was noMoreCallsTo(driver)
      }

      "fail with timeout" in new SlowContext {
        // Given
        val url: String = "http://example.com"
        val expected: OpenGraph = OpenGraph(
          url, error = Error.fromThrowable(new TimeoutException(s"Request timeout for $url")).some
        )

        // When
        val eventualOpenGraph: Future[OpenGraph] = target.fetch(url)(global)
        time.advance(timeout.toMillis + 1)
        val actual: OpenGraph = Await.result(eventualOpenGraph, Duration.Inf)

        // Then
        actual === expected
        there was noMoreCallsTo(driver)
      }
    }
  }

  trait Context extends Scope {
    val timeout = 3 seconds
    val time = new VirtualTime
    implicit val system = ActorSystem("test")
    implicit val mat = ActorMaterializer()
    implicit val scheduler = time.scheduler
    val driver: WebDriver = mock[WebDriver]
    val target = new WebDriverOpenGraphFetcher(createDriver = () => driver, requestTimeout = timeout)(scheduler)
  }

  trait SlowContext extends Context {
    override val target: WebDriverOpenGraphFetcher = new WebDriverOpenGraphFetcher(() => driver, timeout) {
      override protected def doFetch(url: String)(implicit ec: ExecutionContext): Future[OpenGraph] =
        for {
          _ <- Future(Thread.sleep(timeout.toMillis + 10))
          result <- Future.failed(new IllegalStateException())
        } yield result
    }
  }

  private def assumeMetaTag(driver: WebDriver, name: String): String = {
    val value = Random.alphanumeric.take(200).mkString
    val ogTag = mock[WebElement]
    ogTag.getAttribute("content") returns value
    driver.findElement(By.xpath(s"//meta[@property='og:$name']")) returns ogTag
    value
  }

  private def assumeNoMetaTag(driver: WebDriver, name: String) =
    doThrow(new NoSuchElementException(s"$name non-existent")).when(driver).findElement(By.xpath(s"//meta[@property='og:$name']"))
}
