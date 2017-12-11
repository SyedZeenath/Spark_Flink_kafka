package cogroup;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousEventTimeTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import javax.annotation.Nullable;

import static org.apache.flink.streaming.api.windowing.time.Time.milliseconds;

public class ContinuousWindowCoGroupEx {
	
	@SuppressWarnings("serial")
	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		DataStream<Integer> integerDataStreamSource = env
				.fromElements(1, 2, 3, 4)
				.assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks<Integer>() {
					@Override
					public long extractTimestamp(Integer element, long previousElementTimestamp) {
						return (long) element;
					}

					@Nullable
					@Override
					public Watermark checkAndGetNextWatermark(Integer lastElement, long extractedTimestamp) {
						return new Watermark(lastElement);
					}

				});

		integerDataStreamSource
				.map(new MapFunction<Integer, Tuple2<Integer, Integer>>() {
					@Override
					public Tuple2<Integer, Integer> map(Integer value) throws Exception {
						return new Tuple2<>(1, value);
					}
				})
				.keyBy(0)
				.window(TumblingEventTimeWindows.of(milliseconds(10)))
				.trigger(ContinuousEventTimeTrigger.of(milliseconds(1)))
				.apply(new WindowFunction<Tuple2<Integer, Integer>, Integer, Tuple, TimeWindow>() {
					@Override
					public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple2<Integer, Integer>> input, Collector<Integer> out) throws Exception {
						System.out.println("tiggering");
						for (Tuple2<Integer, Integer> val : input) {
							System.out.println(val);
							out.collect(val.f1);
						}
					}
				})
				.coGroup(integerDataStreamSource).where(new KeySelector<Integer, Integer>() {
					@Override
					public Integer getKey(Integer value) throws Exception {
						return value;
					}
				})
				.equalTo(new KeySelector<Integer, Integer>() {
					@Override
					public Integer getKey(Integer value) throws Exception {
						return value;
					}
				})
				.window(TumblingEventTimeWindows.of(milliseconds(10)))
				.trigger(ContinuousEventTimeTrigger.of(milliseconds(1)))
				.apply(new CoGroupFunction<Integer, Integer, Object>() {
					@Override
					public void coGroup(Iterable<Integer> first, Iterable<Integer> second, Collector<Object> out) throws Exception {
						System.out.println("trigger cogroup");
						System.out.println("first");
						for (int val : first){
							System.out.println(val);
						}
						System.out.println("second");
						for (int val : second){
							System.out.println(val);
						}
					}
				});


			env.execute();
		}
}
