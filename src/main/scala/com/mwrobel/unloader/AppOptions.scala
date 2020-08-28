package com.mwrobel.unloader

case class Config(
    stageName: Option[String] = None,
    prefix: Option[String] = None,
    copyToStage: Boolean = true,
    sql: Option[String] = None,
    rmStageFiles: Boolean = true,
    useStdSqlMethod: Boolean = true
)

object AppOptions {
  private val parser = new scopt.OptionParser[Config]("snowflake-to-stdout") {
    opt[String]('n', "stage")
      .action((x, c) => c.copy(stageName = Some(x), useStdSqlMethod = false))
      .text("stage name")

    opt[String]('p', "prefix")
      .action((x, c) => c.copy(prefix = Some(x)))
      .text("prefix name")

    opt[Unit]('c', "only-stream")
      .action((_, c) => c.copy(copyToStage = false))
      .text("copy to stage")

    opt[String]('s', "sql")
      .action((x, c) => c.copy(sql = Some(x)))
      .text("sql to unload")

    opt[Unit]('k', "keep")
      .action((x, c) => c.copy(rmStageFiles = false))
      .text("keep unloaded files in the stage")

    checkConfig(
      c =>
        if (c.useStdSqlMethod && c.sql.isEmpty)
          failure("--sql option is not provided")
        else if (c.useStdSqlMethod && c.sql.isDefined)
          success
        else if (c.copyToStage && c.sql.isEmpty)
          failure("--sql option is not provided")
        else if (c.copyToStage && c.stageName.isEmpty)
          failure("--stage option is not provided")
        else if (c.useStdSqlMethod == false && c.copyToStage == false && c.prefix.isEmpty)
          failure("--prefix option is not provided")
        else success
    )
  }

  def parse(args: Array[String]) = {
    parser.parse(args, Config())
  }
}
