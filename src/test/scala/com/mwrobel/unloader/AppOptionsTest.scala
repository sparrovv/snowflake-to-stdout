package com.mwrobel.unloader

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AppOptionsTest extends AnyFlatSpec with Matchers {

  "tests parsing" should "not parse" in {
    val result = AppOptions.parse(Array())
    result.shouldEqual(None)
  }

  "tests parsing" should "work with default options and standard result-set method" in {
    val config = AppOptions.parse(Array("--sql", "select"))

    config.shouldEqual(Some(Config(stageName = None, sql = Some("select"), useStdSqlMethod = true)))
  }

  "tests parsing" should "work" in {
    val config = AppOptions.parse(Array("--sql", "select", "--stage", "yo", "--prefix", "foo/bar"))

    config.shouldEqual(
      Some(
        Config(
          stageName = Some("yo"),
          sql = Some("select"),
          prefix = Some("foo/bar"),
          useStdSqlMethod = false,
          copyToStage = true,
          rmStageFiles = true
        )
      )
    )
  }

  "tests parsing" should "only unload" in {
    val config = AppOptions.parse(Array("--prefix", "foo/bar", "--stage", "yo", "--only-stream"))

    config.shouldEqual(
      Some(
        Config(
          copyToStage = false,
          stageName = Some("yo"),
          prefix = Some("foo/bar"),
          useStdSqlMethod = false
        )
      )
    )
  }
}
