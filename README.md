# Akka WebDriver Open Graph Fetcher

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tkqubo/akka-webdriver-open-graph-fetcher_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tkqubo/akka-webdriver-open-graph-fetcher_2.11/)
[![Circle CI](https://img.shields.io/circleci/project/tkqubo/akka-webdriver-open-graph-fetcher/master.svg)](https://circleci.com/gh/tkqubo/akka-webdriver-open-graph-fetcher)
[![Coverage Status](https://coveralls.io/repos/tkqubo/akka-webdriver-open-graph-fetcher/badge.svg?branch=master&service=github)](https://coveralls.io/github/tkqubo/akka-webdriver-open-graph-fetcher?branch=master)
[![License: MIT](http://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## About

Tiny library which fetches open graph data from the specified URL, powered by akka-http client and [WebDriver](http://www.seleniumhq.org/projects/webdriver/)

### What this library can do, compared to [Akka Open Graph Fetcher](https://github.com/tkqubo/akka-open-graph-fetcher)

This library can parse asynchronously-rendered HTML body using WebDriver, so it can fetch correct open graph from web pages like Google Map.

:warning: *No support for encodings other than `UTF-8`*

1. This library internally uses [jBrowserDriver](https://github.com/MachinePublishers/jBrowserDriver).
1. jBrowserDriver currently cannot detect correct encoding (see [this issue](https://github.com/MachinePublishers/jBrowserDriver/issues/200)).
1. Hence `akka-webdriver-open-graph-fetcher` cannot either.


## Usage

```scala
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.github.tkqubo.akka_open_graph_fetcher._

implicit val system = ActorSystem("console")
implicit val mat = ActorMaterializer()

val fetcher = WebDriverOpenGraphFetcher()
val future = fetcher.fetch("https://www.google.co.jp/maps/place/Higashi-ginza+Station")
val openGraph = Await.result(future, Duration.Inf)

println(openGraph.url)
println(openGraph.title)
println(openGraph.description)
println(openGraph.image)
println(openGraph.error)

// https://www.google.co.jp/maps/place/Higashi-ginza+Station
// Some(東銀座駅)
// Some(〒104-0061 東京都中央区銀座４丁目１０ 東銀座駅)
// Some(https://maps.google.com/maps/api/staticmap?sensor=false&center=35.6697002801969%2C139.76495121525855&zoom=16&size=256x256&language=ja&markers=35.669700299999995%2C139.7671399&client=google-maps-frontend&signature=sAUn5_JMlqwmTV4XMqdAnAcdDyE)
// None
```
