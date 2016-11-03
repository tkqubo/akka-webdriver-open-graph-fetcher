import sbt._

object Dependencies {
  object Versions {
    val akka    = "2.4.11"
    val akkaOpenGraphFetcher = "0.3.0"
    val specs2  = "3.8.5.1"
    val jbrowserdriver = "0.17.0"
    val mockScheduler: String = "0.4.0"
    val selenium = "2.45.0"
  }

  val dependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka"       %%  "akka-actor"                        % Versions.akka,
    "com.typesafe.akka"       %%  "akka-slf4j"                        % Versions.akka,
    "com.typesafe.akka"       %%  "akka-testkit"                      % Versions.akka % Test,
    "com.typesafe.akka"       %%  "akka-http-core"                    % Versions.akka,
    "com.typesafe.akka"       %%  "akka-http-experimental"            % Versions.akka,
    "com.typesafe.akka"       %%  "akka-http-testkit"                 % Versions.akka % Test,
    "com.typesafe.akka"       %%  "akka-http-spray-json-experimental" % Versions.akka,
    "com.github.tkqubo"       %%  "akka-open-graph-fetcher"           % Versions.akkaOpenGraphFetcher,
    "com.machinepublishers"   %   "jbrowserdriver"                    % Versions.jbrowserdriver,
    "org.seleniumhq.selenium" %   "selenium-java"                     % Versions.selenium,
    "com.miguno.akka"         %%  "akka-mock-scheduler"               % Versions.mockScheduler % Test,
    "org.specs2"              %%  "specs2-core"                       % Versions.specs2 % Test,
    "org.specs2"              %%  "specs2-matcher"                    % Versions.specs2 % Test,
    "org.specs2"              %%  "specs2-matcher-extra"              % Versions.specs2 % Test,
    "org.specs2"              %%  "specs2-mock"                       % Versions.specs2 % Test
  )
}