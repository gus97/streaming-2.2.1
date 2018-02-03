package cn.gus.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

import org.apache.spark.sql.*;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;


public class Mapp {

    public static void main(String[] args) {

        SparkSession spark = SparkSession
                .builder()
                .master("spark://172.18.111.3:7078")
                .appName("Java Spark SQL basic example")
                .config("spark.some.config.option", "some-value")
                .getOrCreate();

        JavaRDD<String> peopleRDD = spark.sparkContext()
                .textFile("file:///Users/guxichang/Desktop/gus/1.txt", 1)
                .toJavaRDD();

// The schema is encoded in a string
        String schemaString = "name age";

// Generate the schema based on the string of schema
        List<StructField> fields = new ArrayList<>();
        for (String fieldName : schemaString.split(" ")) {
            StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
            fields.add(field);
        }
        StructType schema = DataTypes.createStructType(fields);

// Convert records of the RDD (people) to Rows
        JavaRDD<Row> rowRDD = peopleRDD.map((Function<String, Row>) record -> {
            String[] attributes = record.split(",");

//            String[] a = new String[attributes.length];
//
//            for (int i = 0; i < attributes.length; i++) {
//                a[i] = attributes[i];
//            }

            return RowFactory.create(attributes[0],attributes[1]);
        });


        // Apply the schema to the RDD
        Dataset<Row> peopleDataFrame = spark.createDataFrame(rowRDD, schema);

// Creates a temporary view using the DataFrame
        peopleDataFrame.createOrReplaceTempView("people");

// SQL can be run over a temporary view created using DataFrames
        spark.sql("SELECT * FROM people").show();

// The results of SQL queries are DataFrames and support all the normal RDD operations
// The columns of a row in the result can be accessed by field index or by field name
//        Dataset<String> namesDS = results.map(
//                (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
//                Encoders.STRING());
//        namesDS.show();

    }
}
