package topspeed;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.TimestampExtractor;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.delta.DeltaFunction;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.evictors.TimeEvictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.DeltaTrigger;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("deprecation")
public class TopSpeedWindowing {
	private static final int NUM_CAR_EVENTS = 100;

	// *************************************************************************
	// PROGRAM
	// *************************************************************************

	public static void main(String[] args) throws Exception {

		if (!parseParameters(args)) {
			return;
		}

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		
		DataStream<Tuple4<Integer, Integer, Double, Long>> carData;

		if (fileInput) {
			carData = env.readTextFile(inputPath).map(new ParseCarData());
		} else {
			int numOfCars = 2;
			carData = env.addSource(CarSource.create(numOfCars));
		}

		int evictionSec = 10;
		double triggerMeters = 50;
		DataStream<Tuple4<Integer, Integer, Double, Long>> topSpeeds = carData
				.assignTimestamps(new CarTimestamp())
				.keyBy(0)
				.window(GlobalWindows.create())
				.evictor(TimeEvictor.of(Time.of(evictionSec, TimeUnit.SECONDS)))
				.trigger(DeltaTrigger.of(triggerMeters,
						new DeltaFunction<Tuple4<Integer, Integer, Double, Long>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public double getDelta(
									Tuple4<Integer, Integer, Double, Long> oldDataPoint,
									Tuple4<Integer, Integer, Double, Long> newDataPoint) {
								return newDataPoint.f2 - oldDataPoint.f2;
							}
						}, carData.getType().createSerializer(env.getConfig())))
				.maxBy(1);

		if (fileOutput) {
			topSpeeds.print();
			topSpeeds.writeAsText(outputPath);
		} else {
			topSpeeds.print();
		}

		env.execute("CarTopSpeedWindowingExample");
	}

	// *************************************************************************
	// USER FUNCTIONS
	// *************************************************************************

	private static class CarSource implements SourceFunction<Tuple4<Integer, Integer, Double, Long>> {

		private static final long serialVersionUID = 1L;
		private Integer[] speeds;
		private Double[] distances;

		private Random rand = new Random();

		private volatile boolean isRunning = true;
		private int counter;

		private CarSource(int numOfCars) {
			speeds = new Integer[numOfCars];
			distances = new Double[numOfCars];
			Arrays.fill(speeds, 50);
			Arrays.fill(distances, 0d);
		}

		public static CarSource create(int cars) {
			return new CarSource(cars);
		}

		@Override
		public void run(SourceContext<Tuple4<Integer, Integer, Double, Long>> ctx) throws Exception {

			while (isRunning && counter < NUM_CAR_EVENTS) {
				Thread.sleep(100);
				for (int carId = 0; carId < speeds.length; carId++) {
					if (rand.nextBoolean()) {
						speeds[carId] = Math.min(100, speeds[carId] + 5);
					} else {
						speeds[carId] = Math.max(0, speeds[carId] - 5);
					}
					distances[carId] += speeds[carId] / 3.6d;
					Tuple4<Integer, Integer, Double, Long> record = new Tuple4<>(carId,
							speeds[carId], distances[carId], System.currentTimeMillis());
					ctx.collect(record);
					counter++;
				}
			}
		}

		@Override
		public void cancel() {
			isRunning = false;
		}
	}

	private static class ParseCarData extends
			RichMapFunction<String, Tuple4<Integer, Integer, Double, Long>> {
		private static final long serialVersionUID = 1L;

		@Override
		public Tuple4<Integer, Integer, Double, Long> map(String record) {
			String rawData = record.substring(1, record.length() - 1);
			String[] data = rawData.split(",");
			return new Tuple4<>(Integer.valueOf(data[0]), Integer.valueOf(data[1]), Double.valueOf(data[2]), Long.valueOf(data[3]));
		}
	}

	private static class CarTimestamp implements TimestampExtractor<Tuple4<Integer, Integer, Double, Long>> {
		private static final long serialVersionUID = 1L;

		@Override
		public long extractTimestamp(Tuple4<Integer, Integer, Double, Long> element,
				long currentTimestamp) {
			return element.f3;
		}

		@Override
		public long extractWatermark(Tuple4<Integer, Integer, Double, Long> element,
				long currentTimestamp) {
			return element.f3 - 1;
		}

		@Override
		public long getCurrentWatermark() {
			return Long.MIN_VALUE;
		}
	}

	// *************************************************************************
	// UTIL METHODS
	// *************************************************************************

	private static boolean fileInput = false;
	private static boolean fileOutput = false;
	private static String inputPath;
	private static String outputPath;

	private static boolean parseParameters(String[] args) {

		if (args.length > 0) {
			if (args.length == 2) {
				fileInput = true;
				fileOutput = true;
				inputPath = args[0];
				outputPath = args[1];
			} else {
				System.err.println("Usage: TopSpeedWindowingExample <input path> <output path>");
				return false;
			}
		}
		return true;
}

}
