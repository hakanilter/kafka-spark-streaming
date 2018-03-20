package com.datapyro.spark

import com.datapyro.kafka.util.ConfigUtil
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, _}

object NetworkQualitySparkStreamingJob {

  def main(args: Array[String]): Unit = {
    val config = ConfigUtil.getConfig("spark")

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
    //.withColumn("signal", explode($"data.signals"))

    df.createOrReplaceTempView("network_signals")

    val sql =
      """
      SELECT
        WINDOW(FROM_UNIXTIME(x.signal.time/1000), "1 hour", "10 minutes") AS eventWindow,
        x.signal.networkType AS networkType,
        AVG(x.signal.rxSpeed) AS avgRxSpeed,
        AVG(x.signal.txSpeed) AS avgTxSpeed,
        MIN(x.signal.rxSpeed) AS minRxSpeed,
        MIN(x.signal.txSpeed) AS minTxSpeed,
        MAX(x.signal.rxSpeed) AS maxRxSpeed,
        MAX(x.signal.txSpeed) AS maxTxSpeed
      FROM
        (SELECT EXPLODE(data.signals) AS signal FROM network_signals) x
      GROUP BY
        eventWindow,
        networkType
      ORDER BY
        eventWindow,
        networkType
    """
    val query = spark.sql(sql)

    // show results
    val result = query.writeStream
      .format("console")
      .outputMode("complete")
      .option("truncate", "false")
      .start()

    result.awaitTermination()
  }
  
}
