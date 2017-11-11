package stream3;

import java.util.Arrays;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;

public class StructuredWordCount {
	
	public static void main(String[] args) throws Exception {
	    if (args.length < 2) {
	      System.err.println("Usage: JavaStructuredNetworkWordCount <hostname> <port>");
	      System.exit(1);
	    }

	    String host = args[0];
	    int port = Integer.parseInt(args[1]);

	    SparkSession spark = SparkSession
	      .builder()
	      .appName("JavaStructuredNetworkWordCount")
	      .getOrCreate();

	    // Create DataFrame representing the stream of input lines from connection to host:port
	    Dataset<Row> lines = spark
	      .readStream()
	      .format("socket")
	      .option("host", host)
	      .option("port", port)
	      .load();

	    // Split the lines into words
	    Dataset<String> words = lines
	    		  .as(Encoders.STRING())
	    		  .flatMap((FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(), Encoders.STRING());
	        

	    // Generate running word count
	    Dataset<Row> wordCounts = words.groupBy("value").count();

	    // Start running the query that prints the running counts to the console
	    StreamingQuery query = wordCounts.writeStream()
	      .outputMode("complete")
	      .format("console")
	      .start();

	    query.awaitTermination();
	    System.out.println(query.status());
	}

}
