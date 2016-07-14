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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import scala.Tuple2;
import utils.TwitterUtils;

public class LDATest {
	
	public static final int VOCABULARY_SIZE = 1000;
	
	/**
	 * 
	 * @author srinath
	 * https://databricks.com/blog/2015/03/25/topic-modeling-with-lda-mllib-meets-graphx.html
	 * https://databricks.com/blog/2015/09/22/large-scale-topic-modeling-improvements-to-lda-on-spark.html
	 *
	 */
	
	static class CountComparator implements Comparator<Tuple2<String,Integer>>, Serializable {
		@Override
		public int compare(Tuple2<String, Integer> o1, Tuple2<String, Integer> o2) {
			return o1._2 - o2._2;
		}
	}
	
	
	public static class String2IdConverter implements  Function<String, Vector>{
		private Map<String, Integer> words2IntergerMapLocal; 
		
          public String2IdConverter(Broadcast<Map<String, Integer>> words2IntergerMap) {
			super();
			this.words2IntergerMapLocal = words2IntergerMap.getValue();
		}

		public Vector call(String s) {
			if(words2IntergerMapLocal != null){
				double[] vocabFreq = new double[words2IntergerMapLocal.size()];
	            String[] sarray = s.trim().split(" ");
	            //calcuate frequency of each word in the vocabulary occuring 
	            // if vocabulary is w1,w2,w3 a sentance with two w1s and 3 w2s will have 2 3 0
	            for(String t: sarray){
	            	Integer value = words2IntergerMapLocal.get(t);
	            	if(value != null){
	            		vocabFreq[value]++;	
	            	}
	            }              
	            return Vectors.dense(vocabFreq);
			}else{
	            return Vectors.dense(new double[0]);
			}
          }
	} 
	
	
	
  public static void main(String[] args) {
    SparkConf conf = new SparkConf().setAppName("LDA Example").setMaster("local");
    JavaSparkContext sc = new JavaSparkContext(conf);

    // Load and parse the data
    String path = "/Users/srinath/playground/data-science/text-analytics/data/LKA/lka-2015Dec.csv";
    JavaRDD<String> data = TwitterUtils.loadTwitterData(sc, path);
    
    JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
    	  public Iterable<String> call(String s) { 
    		  String[] tokens = s.split("\\s+");
    		  List<String> selectedTokens = new ArrayList<>();
    		  for(String t: tokens){
    			  if(t.length() > 2 && !TwitterUtils.isStopWord(t)){
    				  selectedTokens.add(t); 
    			  }
    		  }
    		  return selectedTokens; 
    	  }
	});
	JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
	  public Tuple2<String, Integer> call(String s) { return new Tuple2<String, Integer>(s, 1); }
	});
	JavaPairRDD<String, Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
	  public Integer call(Integer a, Integer b) { return a + b; }
	});
	
	JavaPairRDD<Integer, String> reveresedWordCounts = wordCounts.mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
		      public Tuple2<Integer, String>  call(Tuple2<String, Integer> t) {
		    	  return new Tuple2<Integer,String>(t._2, t._1);
		      }
	});
	
	JavaPairRDD<Integer, String> sortedReveresedWordCounts = reveresedWordCounts.sortByKey(false);
	List<Tuple2<Integer, String>> orderedWords = sortedReveresedWordCounts.take(VOCABULARY_SIZE);
	
	System.out.println();
	System.out.println("LDA>List"+ orderedWords);
	
	//List<Tuple2<String, Integer>> orderedWords = wordCounts.takeOrdered(300, new CountComparator());
	//System.out.println("LDA>List"+ orderedWords);
	
	final Map<String, Integer> words2IntergerMapLocal = new HashMap<String, Integer>();
	List<String> index2Words = new ArrayList<>();
	int index = 0;
	for(int i = 10;i<orderedWords.size();i++){
		Tuple2<Integer, String> t = orderedWords.get(i);
		words2IntergerMapLocal.put(t._2, index);
		index++;
		index2Words.add(t._2);
	}
	
	System.out.println("LDA>Vocabulary =" + words2IntergerMapLocal);
	
	final Broadcast<Map<String, Integer>> words2IntergerMap = sc.broadcast(words2IntergerMapLocal);

	
    
    JavaRDD<Vector> parsedData = data.map(new String2IdConverter(words2IntergerMap));
    // Index documents with unique IDs ( http://daily-scala.blogspot.com/2010/05/zipwithindex.html)
    JavaPairRDD<Long, Vector> corpus = JavaPairRDD.fromJavaRDD(parsedData.zipWithIndex().map(
        new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
          public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
            return doc_id.swap();
          }
        }
    ));
    corpus.cache();

    // Cluster the documents into three topics using LDA
    LDA lda = new LDA().setK(10).setMaxIterations(300);
    System.out.println("LDA>Iterations" + lda.getMaxIterations());
    		
    LDAModel ldaModel = lda.run(corpus);

    // Output topics. Each is a distribution over words (matching word count vectors)
    System.out.println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize()
        + " words):");
//    Matrix topics = ldaModel.topicsMatrix();
//    for (int topic = 0; topic < 3; topic++) {
//      System.out.print("Topic " + topic + ":");
//      for (int word = 0; word < ldaModel.vocabSize(); word++) {
//        System.out.print(" " + topics.apply(word, topic));
//      }
//      System.out.println();
//    }
    
    Tuple2<int[], double[]>[] topics = ldaModel.describeTopics();
    for(Tuple2<int[], double[]> t: topics){
    	System.out.println(Arrays.toString(t._1) + "="+ Arrays.toString(t._2));
    	StringBuffer buf = new StringBuffer();
    	for(int i=0;i<10;i++){
    		buf.append(index2Words.get(t._1[i]))
    			.append("(").append(TwitterUtils.format(t._2[i])).append(") ");
    	}
    	System.out.println("LDA>"+buf);
    	
    }

    String loc = "myLDAModel"+System.currentTimeMillis();
    ldaModel.save(sc.sc(), loc);
    System.out.println("Model saved to "+ loc);
    DistributedLDAModel sameModel = DistributedLDAModel.load(sc.sc(), loc);
    
  }
}