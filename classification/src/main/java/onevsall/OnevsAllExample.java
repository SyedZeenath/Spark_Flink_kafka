package onevsall;

import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.ml.classification.OneVsRestModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
// $example off$
import org.apache.spark.sql.SparkSession;

public class OnevsAllExample {

	 public static void main(String[] args) {
		    SparkSession spark = SparkSession
		      .builder().master("local")
		      .appName("JavaOneVsRestExample")
		      .getOrCreate();

		    // $example on$
		    // load data file.
		    Dataset<Row> inputData = spark.read().format("libsvm")
		      .load("/home/bizruntime/workspace/sparkstream/classification/files/sample_multilayer_classification.txt");

		    // generate the train/test split.
		    Dataset<Row>[] tmp = inputData.randomSplit(new double[]{0.8, 0.2});
		    Dataset<Row> train = tmp[0];
		    Dataset<Row> test = tmp[1];

		    // configure the base classifier.
		    LogisticRegression classifier = new LogisticRegression()
		      .setMaxIter(10)
		      .setTol(1E-6)
		      .setFitIntercept(true);

		    // instantiate the One Vs Rest Classifier.
		    OneVsRest ovr = new OneVsRest().setClassifier(classifier);

		    // train the multiclass model.
		    OneVsRestModel ovrModel = ovr.fit(train);

		    // score the model on test data.
		    Dataset<Row> predictions = ovrModel.transform(test)
		      .select("prediction", "label");

		    // obtain evaluator.
		    MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
		            .setMetricName("accuracy");

		    // compute the classification error on test data.
		    double accuracy = evaluator.evaluate(predictions);
		    System.out.println("Test Error = " + (1 - accuracy) + "accuracy: " +accuracy);
		    // $example off$

		    spark.stop();
		}
}
