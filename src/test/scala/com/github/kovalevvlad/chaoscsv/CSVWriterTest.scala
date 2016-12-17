package com.github.kovalevvlad.chaoscsv

import java.io.{File, FileReader}
import java.util

import org.apache.commons.csv.{CSVFormat, CSVParser}
import org.scalatest.FunSuite

import collection.JavaConverters._

case class CSVFile(header: Seq[String], data: Seq[Map[String, String]])

class CSVWriterTest extends FunSuite {
  test("can write file with 0 columns") {
    val csv: CSVFile = writeIntoTempFileThenReadBack(Map())

    assert(csv.data.isEmpty)
    // expecting chaos column header to be added
    assert(csv.header.length === 1)
  }

  test("can write file with 0 rows") {
    val data = Map("a" -> Seq.empty[String], "b" -> Seq.empty[String])
    val csv = writeIntoTempFileThenReadBack(data)

    assert(csv.data.isEmpty)

    // expecting chaos column header to be added
    assert(csv.header.length === data.keys.size + 1)
    assert((csv.header.toSet -- data.keySet).size == 1)
  }

  test("columns are randomly ordered") {
    val numberOfOrderings =
      Seq
      .fill(100)(writeIntoTempFileThenReadBack(Map("a" -> Seq.empty[String], "b" -> Seq.empty[String])))
      .map(csv => csv.header)
      .toSet
      .size

    assert(numberOfOrderings > 1)
  }

  test("special characters are always added") {
    val columnName = "a"
    val csv = writeIntoTempFileThenReadBack(Map(columnName -> Seq("value")))
    val unknownHeaderColumns = csv.header.toSet - columnName
    assert(unknownHeaderColumns.size == 1)
    val chaosColumnName = unknownHeaderColumns.head
    csv.data.headOption match {
      case None => throw new AssertionError("CSV we wrote had 0 rows")
      case Some(row) =>
        val chaosValue = row(chaosColumnName)
        assert(chaosValue contains ",")
        assert(chaosValue contains "\"")
        assert(chaosValue contains "\n")
        assert(chaosValue contains "\r")
    }
  }

  test("can write a simple file with multiple rows and columns") {
    val column1 = "column1"
    val column2 = "column 2"
    val data = Map(column1 -> Seq("value 1 1", "value 1 2"), column2 -> Seq("value 2 1", "value 2 2"))
    val readCSV = writeIntoTempFileThenReadBack(data)

    assert(readCSV.header contains column1)
    assert(readCSV.header contains column2)
    assert(readCSV.data.map(_(column1)) === data(column1))
    assert(readCSV.data.map(_(column2)) === data(column2))
  }

  def generateTempFile(): File = {
    val tempFile = File.createTempFile("chaos-csv-test", ".csv")
    tempFile.deleteOnExit()
    tempFile
  }

  def readCSV(csvFile: File): CSVFile = {
    val reader = new FileReader(csvFile)
    try {
      val parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())
      val header = parser.getHeaderMap.asScala.toSeq.sortBy(_._2).map(_._1)
      val data = parser.getRecords.asScala.map(_.toMap.asScala.toMap)
      CSVFile(header, data)
    }
    finally {
      reader.close()
    }
  }

  def writeIntoTempFileThenReadBack(data: Map[String, Seq[String]]): CSVFile = {
    val csvFile = generateTempFile()
    new CSVWriter(csvFile).write(data)
    val csv = readCSV(csvFile)
    csv
  }
}
