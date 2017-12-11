package ml;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.TimestampExtractor;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class IncrementLearningSkeleton {
	private static DataStream<Integer> trainingData = null;
	private static DataStream<Integer> newData = null;

	// *************************************************************************
	// PROGRAM
	// *************************************************************************

	public static void main(String[] args) throws Exception {

		if (!parseParameters(args)) {
			return;
		}

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		trainingData = env.addSource(new FiniteTrainingDataSource());
		newData = env.addSource(new FiniteNewDataSource());

		// build new model on every second of new data
		DataStream<Double[]> model = trainingData
				.assignTimestamps(new LinearTimestamp())
				.timeWindowAll(Time.of(5000, TimeUnit.MILLISECONDS))
				.apply(new PartialModelBuilder());

		// use partial model for newData
		DataStream<Integer> prediction = newData.connect(model).map(new Predictor());

		// emit result
		if (fileOutput) {
			prediction.writeAsText(outputPath);
		} else {
			prediction.print();
		}

		// execute program
		env.execute("Streaming Incremental Learning");
	}

	// *************************************************************************
	// USER FUNCTIONS
	// *************************************************************************

	/**
	 * Feeds new data for newData. By default it is implemented as constantly
	 * emitting the Integer 1 in a loop.
	 */
	public static class FiniteNewDataSource implements SourceFunction<Integer> {
		private static final long serialVersionUID = 1L;
		private int counter;

		@Override
		public void run(SourceContext<Integer> ctx) throws Exception {
			Thread.sleep(20);
			while (counter < 50) {
				ctx.collect(getNewData());
			}
		}

		@Override
		public void cancel() {
			// No cleanup needed
		}

		private Integer getNewData() throws InterruptedException {
			Thread.sleep(5);
			counter++;
			return 1;
		}
	}

	/**
	 * Feeds new training data for the partial model builder. By default it is
	 * implemented as constantly emitting the Integer 1 in a loop.
	 */
	public static class FiniteTrainingDataSource implements SourceFunction<Integer> {
		private static final long serialVersionUID = 1L;
		private int counter = 0;

		@Override
		public void run(SourceContext<Integer> collector) throws Exception {
			while (counter < 8200) {
				collector.collect(getTrainingData());
			}
		}

		@Override
		public void cancel() {
			// No cleanup needed
		}

		private Integer getTrainingData() throws InterruptedException {
			counter++;
			return 1;
		}
	}

	public static class LinearTimestamp implements TimestampExtractor<Integer> {
		private static final long serialVersionUID = 1L;

		private long counter = 0L;

		@Override
		public long extractTimestamp(Integer element, long currentTimestamp) {
			return counter += 10L;
		}

		@Override
		public long extractWatermark(Integer element, long currentTimestamp) {
			return counter - 1;
		}

		@Override
		public long getCurrentWatermark() {
			return Long.MIN_VALUE;
		}

	}

	/**
	 * Builds up-to-date partial models on new training data.
	 */
	public static class PartialModelBuilder implements AllWindowFunction<Integer, Double[], TimeWindow> {
		private static final long serialVersionUID = 1L;

		protected Double[] buildPartialModel(Iterable<Integer> values) {
			return new Double[]{1.};
		}

		@Override
		public void apply(TimeWindow window, Iterable<Integer> values, Collector<Double[]> out) throws Exception {
			out.collect(buildPartialModel(values));
		}
	}

	/**
	 * Creates newData using the model produced in batch-processing and the
	 * up-to-date partial model.
	 * <p>
	 * By defaults emits the Integer 0 for every newData and the Integer 1
	 * for every model update.
	 * </p>
	 */
	public static class Predictor implements CoMapFunction<Integer, Double[], Integer> {
		private static final long serialVersionUID = 1L;

		Double[] batchModel = null;
		Double[] partialModel = null;

		@Override
		public Integer map1(Integer value) {
			// Return newData
			return predict(value);
		}

		@Override
		public Integer map2(Double[] value) {
			// Update model
			partialModel = value;
			batchModel = getBatchModel();
			return 1;
		}

		// pulls model built with batch-job on the old training data
		protected Double[] getBatchModel() {
			return new Double[]{0.};
		}

		// performs newData using the two models
		protected Integer predict(Integer inTuple) {
			return 0;
		}

	}

	// *************************************************************************
	// UTIL METHODS
	// *************************************************************************

	private static boolean fileOutput = false;
	private static String outputPath;

	private static boolean parseParameters(String[] args) {

		if (args.length > 0) {
			// parse input arguments
			fileOutput = true;
			if (args.length == 1) {
				outputPath = args[0];
			} else {
				System.err.println("Usage: IncrementalLearningSkeleton <result path>");
				return false;
			}
		} else {
			System.out.println("Executing IncrementalLearningSkeleton with generated data.");
			System.out.println("  Provide parameter to write to file.");
			System.out.println("  Usage: IncrementalLearningSkeleton <result path>");
			
		}
		return true;
	}


}