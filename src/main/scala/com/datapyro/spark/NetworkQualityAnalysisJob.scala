package com.datapyro.spark

import com.datapyro.kafka.util.ConfigUtil
import org.apache.spark.sql.SparkSession

/**
  * Cassandra Dataframe Example
  * (Works with Spark 2.2.0)
  */
object NetworkQualityAnalysisJob extends App {

  val config = ConfigUtil.getConfig("cassandra")

  // spark config
  val spark: SparkSession = SparkSession.builder
    .master(config.getProperty("spark.master"))
    .appName(getClass.getSimpleName)
    .getOrCreate()

  // prepare cassandra df
  val df = spark.read
    .format("org.apache.spark.sql.cassandra")
    .options(Map("table" -> "network_signals", "keyspace" -> "test", "cluster" -> "Test Cluster"))
    .load()

  df.printSchema()
  df.createOrReplaceTempView("network_signals")

  // execute sql
  val sql =
    """
      SELECT networkType, COUNT(*), AVG(rxSpeed), AVG(txSpeed), SUM(rxData), SUM(txData)
      FROM network_signals
      GROUP BY networkType
    """
  spark.sql(sql).show()

  spark.close()

}
