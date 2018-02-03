package cn.gus.scala

import java.util

import org.apache.spark.sql.{ForeachWriter, Row, RowFactory, SparkSession}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.streaming.ProcessingTime
import org.apache.spark.sql.types.{DataTypes, StructField}


object Spark221Streaming4Kafka011 {

  //val serialVersionUID = 1L

  def main(args: Array[String]): Unit = {


    //1005,gus05,2018-01-07 19:27:11,1005,10

    //1012,gus07,2018-01-09 10:30:11,1012,10

    System.setProperty("log.dir", args(0))
    val appName = "Structured-Streaming-Kafka-"+System.currentTimeMillis()
    val spark = SparkSession
      .builder()
      .appName(appName)
      .master("spark://172.18.111.3:7078")
      //.master("local[4]")
      .enableHiveSupport()
      .config("spark.executor.memory", "512m")
      .config("spark.driver.cores", 1)
      .config("spark.cores.max", 20)
      .config("spark.driver.memory", "1024m")
      //.config("spark.shuffle.sort.bypassMergeThreshold", "20")
      //.config("spark.sql.shuffle.partitions", "10")
      //.config("spark.default.parallelism", "50")
      .config("spark.driver.host", "172.16.39.103")
      //.config("spark.ui.port", 4051)
      .config("spark.eventLog.dir", "hdfs://172.18.111.3:9000/spark-2.2.1/applicationHistory")
      .config("spark.eventLog.enabled", value = true)
      //.config("spark.eventLog.compress", value = true)
      //.config("spark.logConf", value = true)
      .getOrCreate()


    spark.sparkContext.addJar("hdfs://172.18.111.3:9000/gjx/ss.jar")

    val checkpointLocation = "hdfs://172.18.111.3:9000/tmp/checkpointLocation/Structured-Streaming-Kafka-05"

    val colSize = 5

    //spark.sql("select * from gus.dept").createOrReplaceTempView("deptTmp")

    //spark.sql("select * from global_temp.deptTmp").show()



    import spark.implicits._

    //spark.sql("use gus")

    //spark.sql("select * from gus.dept").createOrReplaceTempView("deptTmp")

    val lines = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "172.18.111.7:9092,172.18.111.8:9092,172.18.111.9:9092")
      .option("subscribe", "gxc200")
      //      .schema(schema)
      .load()
      .selectExpr("CAST(value AS STRING) as v")
      .as[(String)]

    val schemaString = "eno,ename,hdate,sal,dno"

    // Generate the schema based on the string of schema
    val fields = new util.ArrayList[StructField]
    for (fieldName <- schemaString.split(",")) {
      val field = DataTypes.createStructField(fieldName, DataTypes.StringType, true)
      fields.add(field)
    }
    val schema = DataTypes.createStructType(fields)




    //列长度 表名抽象
    lines.map(_.split(",")).filter(_.length == 5)
      .map { x =>
        var seq = Seq[String]();
        for (i <- 0 until colSize) {
          seq = seq :+ x(i)
        };
        //根据正则过滤的数据进入mysql---->>>
        RowFactory.create(seq: _*)
      }(RowEncoder.apply(schema))
        .createOrReplaceTempView("emp")

    val wordCounts = spark.sql("select e.dno,FROM_UNIXTIME((UNIX_TIMESTAMP(e.hdate) DIV 300)*300) gb," +
    "sum(e.sal) count from gus.dept d, emp e where d.id = e.dno group by e.dno,gb")

    //val i = spark.sparkContext.longAccumulator

    //val j = spark.sparkContext.longAccumulator

    //var k = 0

    val query = wordCounts.writeStream.trigger(ProcessingTime(20000L))
      //update
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

        //spark.sql("select * from emp").show

        //spark.sql("select * from gus.emp").show

      }).start()
    query.awaitTermination()

  }
}
