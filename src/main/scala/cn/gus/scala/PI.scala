package cn.gus.scala

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

object PI {

  def main(args: Array[String]): Unit = {

    System.setProperty("log.dir", "/gjx/" + args(0) + ".log");

    val logger = LoggerFactory.getLogger("file")

    val spark = SparkSession
      .builder()
      //.appName(args(0))
      //.master("spark://172.18.111.3:7078")
      //.config("spark.executor.memory", "512m")
      //.config("spark.cores.max", 4)
      //.config("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
      //.config("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem")
      //.config("spark.driver.host", "172.18.111.3")
      .getOrCreate()

    //spark.sparkContext.addJar("hdfs://172.18.111.3:9000/gjx/spark2.2.1-1.0-SNAPSHOT.jar")

    //spark.sparkContext.hadoopConfiguration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")

    //spark.sparkContext.hadoopConfiguration.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem")


    logger.warn("init ok")
    val peopleRDD = spark.sparkContext.textFile("hdfs://172.18.111.3:9000/gjx/1.txt")

    // The schema is encoded in a string
    val schemaString = "name age"

    // Generate the schema based on the string of schema
    val fields = schemaString.split(" ")
      .map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema = StructType(fields)

    // Convert records of the RDD (people) to Rows
    val a = ArrayBuffer[String]()
    val rowRDD = peopleRDD
      .map(_.split(","))
      .map { x =>
        for (i <- 0 to 1) {
          a += x(i)
        };
        logger.warn(a(0) + "---------->" + a(1))
        Row(a: _*)
      }

    // Apply the schema to the RDD
    val peopleDF = spark.createDataFrame(rowRDD, schema)

    // Creates a temporary view using the DataFrame
    peopleDF.createOrReplaceTempView("people")

    // SQL can be run over a temporary view created using DataFrames
    val results = spark.sql("SELECT * FROM people")

    results.show()

  }
}
