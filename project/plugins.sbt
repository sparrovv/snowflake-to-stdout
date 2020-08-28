resolvers += Resolver.jcenterRepo
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("org.scalameta"    % "sbt-scalafmt"         % "2.0.0")
addSbtPlugin("com.eed3si9n"     % "sbt-assembly"         % "0.14.10")
addSbtPlugin("au.com.onegeek"   %% "sbt-dotenv"          % "2.1.146")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
