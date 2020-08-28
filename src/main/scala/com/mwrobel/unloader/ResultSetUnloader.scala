package com.mwrobel.unloader

case class ResultSetUnloader(snowflake: SnowflakeConnector) {

  def perform(query: String): Unit = {
    val connection = snowflake.getConnection()

    try {
      snowflake
        .performQueryAndStreamResults(query, connection)
        .foreach { r =>
          println(r.getString(1))
        }
    } finally {
      connection.close()
    }

  }
}
