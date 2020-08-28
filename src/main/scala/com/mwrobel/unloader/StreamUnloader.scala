package com.mwrobel.unloader

import scala.io.Source
import scala.util.{Failure, Success, Try}
import SnowflakeImplicits._
import com.typesafe.scalalogging.LazyLogging

case class StreamUnloader(config: Config, snowflake: SnowflakeConnector) extends LazyLogging {
  def perform = {
    val stageUnloadPrefix: String = config.prefix.get
    val stageName                 = config.stageName.get
    val stagePath                 = s"$stageName/${stageUnloadPrefix}/"

    if (config.copyToStage) {
      val sql       = config.sql
      val copyQuery = query(stagePath, sql.get)
      buildFiles(copyQuery)
    }

    val files = dataUnloadFilePaths(stagePath)
    performUnload(files, stageName)

    if (config.rmStageFiles)
      cleanUnloadFiles(stagePath)
  }

  def query(stagePath: String, sql: String): String = {
    s"""copy into @${stagePath} from (
       | ${sql}
       | )
       | file_format = (type = 'JSON');
       |  """.stripMargin

  }

  private def buildFiles(query: String) =
    snowflake.performQuery(query)(_ => "")

  private def dataUnloadFilePaths(stagePath: String) = {
    snowflake
      .performQuery(s"""list @$stagePath""".stripMargin)(rs => rs.extract[String](_.getString("name")))
      .toList
  }

  private def performUnload(filePaths: Seq[String], stageName: String) = {
    filePaths.zipWithIndex.foreach {
      case (fullFilePath, _) =>
        snowflake.runOnConnection(connection => {
          val filePath = fullFilePath.replaceFirst(stageName + "/", "")
          val stream   = connection.downloadStream(stageName, filePath, true)
          Source
            .fromInputStream(stream)
            .getLines()
            .foreach(println)
        })
    }
  }

  private def cleanUnloadFiles(stagePath: String) = {
    logger.info(s"Cleaning files for $stagePath}")

    Try(snowflake.performQuery(s"""remove @$stagePath""")(_ => "")) match {
      case Success(_) =>
      case Failure(ex) =>
        logger.error(s"failed to clean files for $stagePath", ex)
    }
  }
}
