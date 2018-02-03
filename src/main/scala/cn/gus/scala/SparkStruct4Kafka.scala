package cn.gus.scala

import java.util

import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.streaming.ProcessingTime
import org.apache.spark.sql.{ForeachWriter, Row, RowFactory, SaveMode, SparkSession}
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.apache.spark.storage.StorageLevel


object SparkStruct4Kafka {

  def main(args: Array[String]): Unit = {


    val appName = "Structured-Streaming-Kafka-01"
    val spark = SparkSession
      .builder()
      .appName(appName)
      //.master("spark://172.18.111.3:7078")
      .master("local[4]")
      //.enableHiveSupport()
      .config("spark.executor.memory", "512m")
      .config("spark.driver.cores", 1)
      .config("spark.cores.max", 4)
      .config("spark.driver.memory", "512m")
      .config("spark.shuffle.sort.bypassMergeThreshold", "20")
      .config("spark.sql.shuffle.partitions", "10")
      .config("spark.default.parallelism", "50")
      //.config("spark.driver.host", "172.16.39.52")
      //.config("spark.ui.port", 4051)
      //.config("spark.eventLog.dir", "hdfs://172.18.111.3:9000/spark-2.2.1/applicationHistory")
      //.config("spark.eventLog.enabled", value = true)
      //.config("spark.eventLog.compress", value = true)
      //.config("spark.logConf", value = true)
      .getOrCreate()

    val checkpointLocation = "hdfs://172.18.111.3:9000/tmp/checkpointLocation/Structured-Streaming-Kafka-01"

    val colSize = 2


    import spark.implicits._


    val lines = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "172.18.111.7:9092,172.18.111.8:9092,172.18.111.9:9092")
      .option("subscribe", "gxc200")
      //      .schema(schema)
      .load()
      .selectExpr("CAST(value AS STRING) as id")
      .as[(String)]

    //StorageLevel.MEMORY_ONLY

    val schemaString = "a,b,c"

    // Generate the schema based on the string of schema
    val fields = new util.ArrayList[StructField]
    for (fieldName <- schemaString.split(",")) {
      val field = DataTypes.createStructField(fieldName, DataTypes.StringType, true)
      fields.add(field)
    }
    val schema = DataTypes.createStructType(fields)


    //列长度 表名抽象
    lines.map(_.split(",")).filter(_.length == 3)
      .map { x =>
        var seq = Seq[String]();
        for (i <- 0 to colSize) {
          seq = seq :+ x(i)
        };
        //根据正则过滤的数据进入mysql---->>>
        RowFactory.create(seq: _*)
      }(RowEncoder.apply(schema))
      .createOrReplaceTempView("foo")


    val wordCounts = spark.sql("SELECT a,sum(b) FROM foo group by a ")

    val query = wordCounts.writeStream.trigger(ProcessingTime(5000L))
      .outputMode("update")
      .option("checkpointLocation", checkpointLocation)
      .foreach(new ForeachWriter[Row] {

        override def process(value: Row): Unit = {

          println(value.toSeq, "------------>>>>>")

        }

        override def close(errorOrNull: Throwable): Unit = {


        }

        override def open(partitionId: Long, version: Long): Boolean = {

          true
        }
      }).start()
    query.awaitTermination()

  }
}
