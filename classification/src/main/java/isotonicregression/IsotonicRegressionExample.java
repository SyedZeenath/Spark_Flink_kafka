package isotonicregression;

import org.apache.spark.ml.regression.IsotonicRegression;
import org.apache.spark.ml.regression.IsotonicRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
// $example off$
import org.apache.spark.sql.SparkSession;

public class IsotonicRegressionExample {
	public static void main(String[] args) {
	    // Create a SparkSession.
	    SparkSession spark = SparkSession
	      .builder().master("local")
	      .appName("JavaIsotonicRegressionExample")
	      .getOrCreate();

	    // $example on$
	    // Loads data.
	    Dataset<Row> dataset = spark.read().format("libsvm")
	      .load("/home/bizruntime/workspace/sparkstream/classification/files/sample_isotonic_regression_data.txt");

	    // Trains an isotonic regression model.
	    IsotonicRegression ir = new IsotonicRegression();
	    IsotonicRegressionModel model = ir.fit(dataset);

	    System.out.println("Boundaries in increasing order: " + model.boundaries() + "\n");
	    System.out.println("Predictions associated with the boundaries: " + model.predictions() + "\n");

	    // Makes predictions.
	    model.transform(dataset).show();
	    // $example off$

	    spark.stop();
	}

}
