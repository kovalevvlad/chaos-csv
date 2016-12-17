package com.github.kovalevvlad.chaoscsv

import java.io.{File, FileWriter}

import org.apache.commons.csv.{CSVFormat, CSVPrinter}

import collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.util.Random


object CSVWriter {
  private final val CHAOS_COLUMN_NAME = "__DUMMY_CHAOS_COLUMN"
  private final val SPECIAL_SYMBOLS = List("\"", ",", "\n", "\r\n", "\r")
  private def randomChaosValue: String = {
    val randomValueWithPredictableOrder = SPECIAL_SYMBOLS.flatMap(symbol => Seq.fill(1 + Random.nextInt(1))(symbol))
    Random.shuffle(randomValueWithPredictableOrder).mkString
  }
}

class CSVWriter(csvFile: File) {
  private def write(data: ListMap[String, Seq[AnyRef]]): Unit = {
    require(csvFile.canWrite, s"${csvFile.getAbsoluteFile.toString} is not writable!")
    require(data.values.map(_.length).toSet.size == 1, s"All columns must be of the same length but got ${data.mapValues(_.length)}")

    val (header, columns) = data.unzip

    val printer = new CSVPrinter(new FileWriter(csvFile), CSVFormat.DEFAULT.withHeader(header.toSeq: _*))
    try {
      val rows = columns.transpose
      rows.foreach(row => printer.printRecord(row.asJava))
    }
    finally {
      printer.close()
    }
  }

  def write(data: Map[String, Seq[AnyRef]]): Unit = {
    val chaosDummyColumnLength = data.headOption match {
      case Some((columnName, columnData)) => columnData.length
      case None => 0
    }

    val chaosColumn = Seq.fill(chaosDummyColumnLength)(CSVWriter.randomChaosValue)
    val columns = data + (CSVWriter.CHAOS_COLUMN_NAME -> chaosColumn)
    val randomlyOrderedColumns = ListMap(Random.shuffle(columns.toSeq): _*)
    write(randomlyOrderedColumns)
  }
}
