package stream2;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class StreamExample {
	public static void main(String[] args) throws InterruptedException {
	SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount");
	JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
	JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
	JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
	
	JavaPairDStream<String, Integer> pairs = words.mapToPair(s -> new Tuple2<>(s, 1));
	//JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey((i1, i2) -> i1 + i2);
	JavaPairDStream<String, Integer> windowedWordCounts = pairs.reduceByKeyAndWindow((i1, i2) -> i1 + i2, Durations.seconds(30), Durations.seconds(10));
	
	windowedWordCounts.print();
	jssc.start();              // Start the computation
	jssc.awaitTermination(); 
}
}


