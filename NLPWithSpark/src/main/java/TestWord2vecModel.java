import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.feature.Word2VecModel;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.tsne.TSne;
import com.jujutsu.tsne.*;

import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.Map;

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

public class TestWord2vecModel {

	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("LKA").setMaster("local");
	    JavaSparkContext sc = new JavaSparkContext(conf);
	    
		Word2VecModel model = Word2VecModel.load(sc.sc(), "lkaword2vec.model");
		
		Tuple2<String, Object>[] synonyms = model.findSynonyms("vote", 40);

		for(Tuple2<String, Object> d: synonyms) {
		 System.out.println(d._1 + " "+ d._2);
		}

		
		Map<String, float[]> vectors = model.getVectors();
		Iterator<Tuple2<String, float[]>> iterator = vectors.iterator();
		List<String> words = new ArrayList<>();
		
//		int maxSize = 1000;
		double[][] vectorsAsDouble = null;
		int index = 0; 
		while(iterator.hasNext()){
			Tuple2<String, float[]>  v = iterator.next();
			if(index < vectors.size()){
				words.add(v._1);
				System.out.println(v._1);
				if(vectorsAsDouble == null){
					vectorsAsDouble = new double[Math.min(vectors.size(), Integer.MAX_VALUE)][v._2.length];
				}
				int j=0; 
				for(float f: v._2){
					vectorsAsDouble[index][j] = f;
					j++;
				}
				index++;				
			}
		}

//		System.out.println(vectorsAsDouble.length);
//		System.out.println(vectorsAsDouble[1].length);
//		System.out.println(vectorsAsDouble[2].length);

//	    int initial_dims = 55;
//	    double perplexity = 20.0;
//
//		System.out.println("Size=" + vectorsAsDouble.length);
//	    TSne tsne = new FastTSne();
//		double [][] Y = tsne.tsne(vectorsAsDouble, 2, initial_dims, perplexity);
//
//		index = 0;
//		for(double[] t: Y){
//			System.out.println(words.get(index)+","+String.format("%.2f", t[0]) +","+String.format("%.2f", t[1]));
//			index++;
//		}
//	    System.out.println(MatrixOps.doubleArrayToPrintString(Y, ", ", Y.length,Y[0].length));

		
	}

}
