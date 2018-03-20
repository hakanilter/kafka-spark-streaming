package com.datapyro.kafka.producer;

import com.datapyro.kafka.model.NetworkData;
import com.datapyro.kafka.model.NetworkSignal;
import com.datapyro.kafka.util.ConfigUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class initializes a Kafka Producer and produces random network data
 */
public class RandomNetworkDataProducer implements Runnable {

    private static final long INCOMING_DATA_INTERVAL = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomNetworkDataProducer.class);

    @Override
    public void run() {
        LOGGER.info("Initializing kafka producer...");

        Properties properties = ConfigUtil.getConfig("network-data");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);

        String topic = properties.getProperty("topic.names");
        LOGGER.info("Start producing random network data to topic: " + topic);

        Producer<String, String> producer = new KafkaProducer<>(properties);

        Random random = new Random();

        final int deviceCount = 100;
        List<String> deviceIds = IntStream.range(0, deviceCount)
                                          .mapToObj(i-> UUID.randomUUID().toString())
                                          .collect(Collectors.toList());

        final int count = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            NetworkData networkData = new NetworkData();

            networkData.setDeviceId(deviceIds.get(random.nextInt(deviceCount-1)));
            networkData.setSignals(new ArrayList<>());
            for (int j = 0; j < random.nextInt(4)+1; j++) {
                NetworkSignal networkSignal = new NetworkSignal();
                networkSignal.setNetworkType(i % 2 == 0 ? "mobile" : "wifi");
                networkSignal.setRxData((long) random.nextInt(1000));
                networkSignal.setTxData((long) random.nextInt(1000));
                networkSignal.setRxSpeed((double) random.nextInt(100));
                networkSignal.setTxSpeed((double) random.nextInt(100));
                networkSignal.setTime(System.currentTimeMillis());
                networkData.getSignals().add(networkSignal);
            }

            String key = UUID.randomUUID().toString();
            String value = networkData.toJson();

            LOGGER.warn("Random data generated: {}: {}", key, value);

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record);

            try {
                Thread.sleep(INCOMING_DATA_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }

    public static void main(String[] args) {
        new RandomNetworkDataProducer().run();
    }

}
