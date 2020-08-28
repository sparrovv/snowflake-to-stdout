package com.mwrobel.unloader

import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {
  val snowflakePass = sys.env
    .get("SNOWFLAKE_PASSWORD")
    .getOrElse(throw new Exception("No snowflake password, please provide SNOWFLAKE_PASSWORD"))
  val snowflakeUser = sys.env
    .getOrElse("SNOWFLAKE_USER", throw new Exception("No snowflake user, please provide SNOWFLAKE_USER"))
  val snowflakeUrl = sys.env
    .getOrElse("SNOWFLAKE_URL", throw new Exception("No snowflake url, please provide SNOWFLAKE_URL"))
  val snowflakeConfig = SnowflakeConfig(snowflakeUser, snowflakePass, snowflakeUrl)
  val snowflake       = new SnowflakeConnector(snowflakeConfig)

  def main(args: Array[String]): Unit = {
    val config = AppOptions.parse(args) match {
      case Some(c) => c
      case _ =>
        println("Wrong options")
        System.exit(1)

        throw new Exception("Wrong options, it shouldn't even get here")
    }

    if (config.useStdSqlMethod) {
      val resultSetUnloader = ResultSetUnloader(snowflake)
      resultSetUnloader.perform(config.sql.get)
    } else {
      val streamUnloader = StreamUnloader(config, snowflake)
      streamUnloader.perform
    }
  }
}
