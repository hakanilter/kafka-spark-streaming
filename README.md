# Kafka Spark Streaming 
An example project for integrating Kafka and Spark Streaming in order to run streaming sql queries. 

- ** NetworkQualityStreamingJob **
An example Spark Streaming app which consumes network signal data and executes continuous SQL query.

- ** NetworkQualityCassandraJob **
An example Spark Streaming app which consumes network signal data and writes to Cassandra with a foreach writer

- ** NetworkQualityAnalysisJob **
An example Spark DataFrame app which creates a DF from Cassandra and executes an aggregation SQL query.
 