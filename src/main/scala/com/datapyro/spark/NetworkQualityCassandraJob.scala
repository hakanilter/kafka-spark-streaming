package com.datapyro.spark

import java.util.UUID

import org.apache.spark.sql.{ForeachWriter, Row, SparkSession}
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import com.datapyro.kafka.util.ConfigUtil
import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{QueryBuilder => QB}

/**
  * Cassandra Spark Streaming Foreach Example
  * 
  * CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  *
  * CREATE TABLE network_signals (id TEXT PRIMARY KEY, deviceId TEXT, time TIMESTAMP, networkType TEXT, rxSpeed DOUBLE, txSpeed DOUBLE, rxData DOUBLE, txData DOUBLE, latitude DOUBLE, longitude DOUBLE);
  */
object NetworkQualityCassandraJob extends App {

  val config = ConfigUtil.getConfig("cassandra")

  // spark config
  val spark: SparkSession = SparkSession.builder
    .master(config.getProperty("spark.master"))
    .appName(getClass.getSimpleName)
    .getOrCreate()

  import spark.implicits._

  // define schema for json
  val schema = StructType(
    List(
      StructField("deviceId", StringType, true),
      StructField("time", LongType, true),
      StructField("signals", ArrayType(StructType(Array(
        StructField("time", LongType, true),
        StructField("networkType", StringType, true),
        StructField("rxSpeed", DoubleType, true),
        StructField("txSpeed", DoubleType, true),
        StructField("rxData", LongType, true),
        StructField("txData", LongType, true),
        StructField("latitude", DoubleType, true),
        StructField("longitude", DoubleType, true)
      ))))
    )
  )

  // create stream
  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", config.getProperty("bootstrap.servers"))
    .option("subscribe", config.getProperty("topic.names"))
    .load()
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .select(from_json($"value", schema).alias("data"))

  df.createOrReplaceTempView("network_signals")

  val sql = "SELECT x.deviceId, x.signal.* FROM (SELECT data.deviceId, EXPLODE(data.signals) AS signal FROM network_signals) x"
  val generateUUID = udf(() => UUID.randomUUID().toString)
  val query = spark.sql(sql).withColumn("id", generateUUID())

  // cassandra
  val keyspace = config.getProperty("cassandra.keyspace")
  val table = config.getProperty("cassandra.table")
  val cluster = {
    Cluster.builder()
      .addContactPoint(config.getProperty("cassandra.host"))
      .build()
  }

  val writer = new ForeachWriter[Row] {
    var session: Session = null
    var records: Int = 0
    var start: Long = 0

    override def open(partitionId: Long, version: Long) = {
      start = System.currentTimeMillis()
      session = cluster.connect(keyspace)
      session != null
    }

    override def process(row: Row) = {
      val query = {
        QB.insertInto(table)
          .value("deviceId", row.getString(0))
          .value("time", row.getLong(1))
          .value("networkType", row.getString(2))
          .value("rxSpeed", row.getDouble(3))
          .value("txSpeed", row.getDouble(4))
          .value("rxData", row.getLong(5))
          .value("txData", row.getLong(6))
          .value("latitude", row.getDouble(7))
          .value("longitude", row.getDouble(8))
          .value("id", row.getString(9))
      }
      session.executeAsync(query)
      records += 1
    }

    override def close(errorOrNull: Throwable) = {
      if (session != null) session.close()
      println(records + " records processed, takes " + (System.currentTimeMillis() - start) + " ms")
    }
  }

  val result = query.writeStream
    .trigger(Trigger.ProcessingTime(config.getProperty("processing.time")))
    .outputMode(OutputMode.Append())
    .foreach(writer)
    .start()

  result.awaitTermination()

}


