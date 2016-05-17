/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

public class RandomForestTest {
	public static void main(String[] args) {

		// Load and parse the data file, converting it to a DataFrame.

		SparkConf conf = new SparkConf().setAppName("LKA").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new org.apache.spark.sql.SQLContext(sc);
		DataFrame data = sqlContext
				.read()
				.format("libsvm")
				.load("/Users/srinath/playground/data-science/spark-1.6.0-bin-hadoop2.6/data/mllib/sample_libsvm_data.txt");

		// Index labels, adding metadata to the label column.
		// Fit on whole dataset to include all labels in index.
		StringIndexerModel labelIndexer = new StringIndexer()
				.setInputCol("label").setOutputCol("indexedLabel").fit(data);
		// Automatically identify categorical features, and index them.
		// Set maxCategories so features with > 4 distinct values are treated as
		// continuous.
		VectorIndexerModel featureIndexer = new VectorIndexer()
				.setInputCol("features").setOutputCol("indexedFeatures")
				.setMaxCategories(4).fit(data);

		// Split the data into training and test sets (30% held out for testing)
		DataFrame[] splits = data.randomSplit(new double[] { 0.7, 0.3 });
		DataFrame trainingData = splits[0];
		DataFrame testData = splits[1];

		// Train a RandomForest model.
		RandomForestClassifier rf = new RandomForestClassifier().setLabelCol(
				"indexedLabel").setFeaturesCol("indexedFeatures");

		// Convert indexed labels back to original labels.
		IndexToString labelConverter = new IndexToString()
				.setInputCol("prediction").setOutputCol("predictedLabel")
				.setLabels(labelIndexer.labels());

		// Chain indexers and forest in a Pipeline
		Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] {
				labelIndexer, featureIndexer, rf, labelConverter });

		// Train model. This also runs the indexers.
		PipelineModel model = pipeline.fit(trainingData);

		// Make predictions.
		DataFrame predictions = model.transform(testData);

		// Select example rows to display.
		predictions.select("predictedLabel", "label", "features").show(5);

		// Select (prediction, true label) and compute test error
		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
				.setLabelCol("indexedLabel").setPredictionCol("prediction")
				.setMetricName("precision");
		double accuracy = evaluator.evaluate(predictions);
		System.out.println("Test Error = " + (1.0 - accuracy));

		RandomForestClassificationModel rfModel = (RandomForestClassificationModel) (model
				.stages()[2]);
		System.out.println("Learned classification forest model:\n"
				+ rfModel.toDebugString());
	}
}