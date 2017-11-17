package fpgrowth;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.ml.fpm.FPGrowth;
import org.apache.spark.ml.fpm.FPGrowthModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.*;

public class FPGrowthExample {
	public static void main(String[] args) {
	    SparkSession spark = SparkSession
	      .builder().master("local")
	      .appName("JavaFPGrowthExample")
	      .getOrCreate();

	    // $example on$
	    List<Row> data = Arrays.asList(
	      RowFactory.create(Arrays.asList("1 2 5 6".split(" "))),
	      RowFactory.create(Arrays.asList("1 2 3 5 8".split(" "))),
	      RowFactory.create(Arrays.asList("1 2 3".split(" ")))
	    );
	    StructType schema = new StructType(new StructField[]{ new StructField(
	      "items", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
	    });
	    Dataset<Row> itemsDF = spark.createDataFrame(data, schema);

	    FPGrowthModel model = new FPGrowth()
	      .setItemsCol("items")
	      .setMinSupport(0.5)
	      .setMinConfidence(0.6)
	      .fit(itemsDF);

	    // Display frequent itemsets.
	    model.freqItemsets().show();

	    // Display generated association rules.
	    model.associationRules().show();

	    // transform examines the input items against all the association rules and summarize the
	    // consequents as prediction
	    model.transform(itemsDF).show();
	    // $example off$

	    spark.stop();
	}

}
