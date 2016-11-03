package com.github.tkqubo.akka_open_graph_fetcher

import java.util.concurrent.TimeoutException

import akka.actor.{ActorSystem, Scheduler}
import akka.pattern.after
import com.github.tkqubo.akka_open_graph_fetcher.WebDriverOpenGraphFetcher._
import com.machinepublishers.jbrowserdriver.JBrowserDriver
import org.openqa.selenium.{By, NoSuchElementException, WebDriver}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WebDriverOpenGraphFetcher(
  val createDriver: () => WebDriver = createDefaultDriver,
  val requestTimeout: FiniteDuration = defaultRequestTimeout
)(implicit scheduler: Scheduler) {
  /**
    * Fetches [[OpenGraph]] from the specified URL using WebDriver
    * @param url URL to fetch
    * @param ec
    * @return
    */
  def fetch(url: String)(implicit ec: ExecutionContext): Future[OpenGraph] =
    Future.firstCompletedOf(
      doFetch(url) :: requestTimeout(url) :: Nil
    )

  protected def doFetch(url: String)(implicit ec: ExecutionContext): Future[OpenGraph] =
    Future {
      val driver: WebDriver = createDriver()
      driver.get(url)
      try {
        driver.getPageSource // may throw a NoSuchElementException
        webDriverToOpenGraph(url, driver)
      } catch {
        case e: NoSuchElementException => OpenGraph(url, error = Some(Error.fromThrowable(e)))
      } finally {
        driver.quit()
      }
    }

  protected def webDriverToOpenGraph(url: String, driver: WebDriver): OpenGraph =
    OpenGraph(
      url = driver.metaValue("og:url") getOrElse url,
      title = driver.metaValue("og:title"),
      description = driver.metaValue("og:description"),
      image = driver.metaValue("og:image")
    )

  private def requestTimeout(url: String)(implicit ec: ExecutionContext): Future[OpenGraph] =
    after(requestTimeout, scheduler) {
      Future.successful(
        OpenGraph(url, error = Some(Error.fromThrowable(new TimeoutException(s"Request timeout for $url"))))
      )
    }

  implicit protected class WebDriverOps(val driver: WebDriver) {
    def metaValue(property: String): Option[String] =
      try {
        Some(driver.findElement(By.xpath(s"//meta[@property='$property']")).getAttribute("content"))
      } catch {
        case e: Throwable => None
      }
  }
}

object WebDriverOpenGraphFetcher {
  def apply(
    createDriver: () => WebDriver = createDefaultDriver,
    requestTimeout: FiniteDuration = defaultRequestTimeout
  )(implicit system: ActorSystem): WebDriverOpenGraphFetcher = new WebDriverOpenGraphFetcher(createDriver, requestTimeout)(system.scheduler)
  def createDefaultDriver(): JBrowserDriver = new JBrowserDriver()
  def defaultRequestTimeout: FiniteDuration = 8 seconds
}
