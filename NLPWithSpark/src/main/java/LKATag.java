import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import org.apache.cassandra.thrift.Cassandra.system_add_column_family_args;
import utils.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.feature.Word2VecModel;

import scala.Tuple12;
import scala.Tuple2;


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

public class LKATag {


    public static void main(String[] args) {

        String logFile = "/home/anoukh/SentimentAnalysis/ml-projects-java/NLPWithSpark/Tweets.csv"; // Should be some file on your system
        SparkConf conf = new SparkConf().setAppName("TwiiterSentiment").setMaster("local").set("spark.executor.memory", "8G")
                .set("spark.driver.maxResultSize", "16G");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> tweetText = TwitterUtils.loadTwitterData(sc, logFile);
//        JavaRDD<String> tweetText = sc.textFile(logFile).cache();
        List<String> collectedList = tweetText.collect();

        for (String value : collectedList) {
            System.out.println(value);
        }

        JavaRDD<List> splittedTokens = tweetText.map(new Function<String, List>() {
            @Override
            public List call(String s) {
                ArrayList<String> list = new ArrayList<>();
                Collections.addAll(list, s.split(" "));
                return list;
            }
        });


//		val input = sc.textFile("text8").map(line => line.split(" ").toSeq)

        Word2Vec word2vec = new Word2Vec().setVectorSize(10);


        Word2VecModel model = word2vec.fit(splittedTokens);

        System.out.println(model.getVectors().size());
//        Tuple2<String, Object>[] synonyms = model.findSynonyms("trump", 40);
//
//        for (Tuple2<String, Object> d : synonyms) {
//            System.out.println(d._1 + " " + d._2);
//        }

        model.save(sc.sc(), "kaggle.model" + System.currentTimeMillis());

    }

}
