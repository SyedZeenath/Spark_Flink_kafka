package socketTextStream;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SocketTextStream {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {

		if (!parseParameters(args)) {
			return;
		}

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();

		// get input data
		DataStream<String> text = env.socketTextStream(hostName, port, '\n', 0);

		DataStream<Tuple2<String, Integer>> counts =
				// split up the lines in pairs (2-tuples) containing: (word,1)
				text.flatMap(new Tokenizer())
						// group by the tuple field "0" and sum up tuple field "1"
						.keyBy(0)
						.sum(1);

		if (fileOutput) {
			counts.writeAsText("/home/bizruntime/output.txt");
		} else {
			counts.print();
		}

		// execute program
		env.execute("WordCount from SocketTextStream Example");
	}

	// *************************************************************************
	// UTIL METHODS
	// *************************************************************************

	private static boolean fileOutput = false;
	private static String hostName;
	private static int port;
	private static String outputPath;

	private static boolean parseParameters(String[] args) {

		// parse input arguments
		if (args.length == 3) {
			fileOutput = true;
			hostName = args[0];
			port = Integer.valueOf(args[1]);
			outputPath = args[2];
		} else if (args.length == 2) {
			hostName = args[0];
			port = Integer.valueOf(args[1]);
		} else {
			System.err.println("Usage: SocketTextStreamWordCount <hostname> <port> [<output path>]");
			return false;
		}
		return true;
}

}
