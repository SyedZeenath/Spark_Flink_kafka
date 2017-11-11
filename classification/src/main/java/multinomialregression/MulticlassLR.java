package multinomialregression;

import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class MulticlassLR {
	 public static void main(String[] args) {
	        SparkSession spark = SparkSession
	                .builder().master("local")
	                .appName("JavaMulticlassLogisticRegressionWithElasticNetExample")
	                .getOrCreate();

	        // $example on$
	        // Load training data
	        Dataset<Row> training = spark.read().format("libsvm")
	                .load("/home/bizruntime/workspace/sparkstream/classification/files/sample_libsvm_data.txt");

	        LogisticRegression lr = new LogisticRegression()
	                .setMaxIter(10)
	                .setRegParam(0.3)
	                .setElasticNetParam(0.8);

	        // Fit the model
	        LogisticRegressionModel lrModel = lr.fit(training);

	        // Print the coefficients and intercept for multinomial logistic regression
	        System.out.println("Coefficients: \n"
	                + lrModel.coefficientMatrix() + " \nIntercept: " + lrModel.interceptVector());
	        // $example off$

	        spark.stop();
	}

}
