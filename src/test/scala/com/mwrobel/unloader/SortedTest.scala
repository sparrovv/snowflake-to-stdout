package com.mwrobel.unloader

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.matching.Regex

object ListOrdering extends Ordering[List[Long]] {
  implicit override def compare(x: List[Long], y: List[Long]): Int = {
    val left  = x.reverse
    val right = y.reverse

    left.zip(right).foldLeft(0) { case (acc, lr) => acc + lr._1.compareTo(lr._2) }
  }
}

object NatSort {
  private val numberExtraction = new Regex("\\d+")

  def sort(input: Traversable[String]): Traversable[String] = {

    object WordNumber
    case class WordNumber(word: String, number: List[Long])

    def extractNumeric(word: String): WordNumber = {
      val matches   = numberExtraction.findAllMatchIn(word)
      val matchList = matches.toList

      val theseNumbers = matchList.map(ma => ma.group(0).toLong)

      WordNumber(word, theseNumbers)
    }

    val mapped = for {
      current <- input
    } yield extractNumeric(current)

    val sorted = mapped.toList.sortBy(wn => wn.number)(ListOrdering)

    val reduced = for {
      current <- sorted
    } yield current.word

    reduced
  }
}

class SortedTest extends AnyFlatSpec with Matchers {
  val input = List(
    "foo/data_0_0_1.json.gz",
    "foo/data_0_0_10.json.gz",
    "foo/data_0_2_11.json.gz",
    "foo/data_0_0_3.json.gz",
    "foo/data_0_0_2.json.gz",
    "foo/data_1_0_2.json.gz"
  )

  val expected = List(
    "foo/data_0_0_1.json.gz",
    "foo/data_0_0_2.json.gz",
    "foo/data_0_0_3.json.gz",
    "foo/data_0_0_10.json.gz",
    "foo/data_0_2_11.json.gz",
    "foo/data_1_0_0.json.gz"
  )

  "tests sorting" should "not parse" in {
    NatSort.sort(input).shouldEqual(expected)
  }

}
