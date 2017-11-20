package consumer;

import kafka.consumer.Consumer;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class KafkaConsumer {

    private ExecutorService executor;
    private final ConsumerConnector consumer;
    private final String topic;

    public KafkaConsumer(String zookeeper, String groupId, String topic) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "5000");
        props.put("zookeeper.sync.time.ms", "2500");
        props.put("auto.commit.interval.ms", "1000");

        consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        this.topic = topic;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void runConsumer(int threadCount) {
        Map<String, Integer> topicCount = new HashMap<>();
        topicCount.put(topic, threadCount);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(topic);

        executor = Executors.newFixedThreadPool(threadCount);

        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new KafkaConsumerThread(stream, threadNumber));
            ++ threadNumber;
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {

        }
        if (consumer != null) {
            consumer.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        String topic = args[0];
        int threadCount = Integer.parseInt(args[1]);
        KafkaConsumer kafkaConsumer = new KafkaConsumer("localhost:2181", "testgroup", topic);
        kafkaConsumer.runConsumer(threadCount);
    }

}
