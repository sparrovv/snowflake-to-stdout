package com.mwrobel.unloader

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.Properties
import java.util.concurrent.TimeUnit

import com.snowflake.client.jdbc.SnowflakeDriver
import com.typesafe.scalalogging.LazyLogging
import net.snowflake.client.jdbc.SnowflakeConnection

import scala.concurrent.duration.Duration

import scala.language.implicitConversions
object SnowflakeImplicits {

  implicit class ResultSetWrapper(resultSet: ResultSet) {
    def extract[T](resultSet2Type: ResultSet => T): Seq[T] = {
      new Iterator[T] {
        def hasNext: Boolean = resultSet.next()
        def next(): T =
          resultSet2Type(resultSet)
      }.toList
    }
  }

  implicit def conn2SnowflakeConn(conn: Connection): SnowflakeConnection = {
    conn.unwrap(classOf[SnowflakeConnection])
  }
}
class SnowflakeConnector(snowflakeConfig: SnowflakeConfig) extends LazyLogging {

  private val snowflakeDriver: SnowflakeDriver = new SnowflakeDriver()

  private val properties: Properties = {
    val props = new Properties()
    props.put("user", snowflakeConfig.user)
    props.put("password", snowflakeConfig.password)
    props
  }

  def getConnection(): Connection = {
    Option(snowflakeDriver.connect(snowflakeConfig.url, properties))
      .getOrElse(throw new Exception(s"Invalid snowflake connection string: ${snowflakeConfig.url}"))
  }

  def performQuery[T](query: String)(toResult: ResultSet => T): T = {
    performPreparedQuery(query)(_ => {})(toResult)
  }

  def performQueryAndStreamResults(query: String, connection: Connection): Iterator[ResultSet] = {
    try {
      val statement = connection.prepareStatement(query)
      logger.info(s"Executing query - $query")
      val resultSet: ResultSet = statement.executeQuery()

      new Iterator[ResultSet] {
        val rs       = resultSet
        val metadata = rs.getMetaData
        val columns  = metadata.getColumnCount();

        def hasNext: Boolean = rs.next()
        def next(): ResultSet = {
          // @todo add an option to just quickly print something to STDOUT in table format
//          val x: Map[String, AnyRef] = 1
//            .to(columns)
//            .map { i =>
//              (metadata.getColumnName(i), rs.getObject(i))
//            }
//            .toMap

          rs
        }
      }
    } catch {
      case ex: Exception =>
        logger.error(s"Query failed - $query", ex)
        throw ex
    }
  }

  def performPreparedQuery[T](
      query: String
  )(prepareStatement: PreparedStatement => Unit)(toResult: ResultSet => T): T = {
    runOnConnection(connection => {
      try {
        logger.info(s"Preparing query - $query")
        val statement = connection.prepareStatement(query)
        prepareStatement(statement)
        logger.info(s"Executing query - $query")
        val tStart    = System.nanoTime()
        val resultSet = statement.executeQuery()
        val result    = toResult(resultSet)
        val took      = Duration(System.nanoTime() - tStart, TimeUnit.NANOSECONDS)
        logger.info(s"Executed query in ${took.toMillis}ms - $query")
        result
      } catch {
        case ex: Exception =>
          logger.error(s"Query failed - $query", ex)
          throw ex
      }
    })
  }

  def runOnConnection[T](task: Connection => T): T = {
    val connection = getConnection()
    try {
      task(connection)
    } finally {
      connection.close()
    }
  }
}
