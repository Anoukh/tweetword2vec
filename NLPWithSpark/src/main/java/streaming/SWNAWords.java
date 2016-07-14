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

package streaming;

import java.util.Map;

import streaming.core.GenericTweetsProcessor;
import streaming.operators.StandoutWordsDetector;
import streaming.operators.WordEmbeddingsProcessor;
import tweets.TweetWordsProcessor;

public class SWNAWords {


	public static void main(String[] args) throws Exception{
        //String path = "/Users/srinath/playground/data-science/text-analytics/data/LKA/lka-2015Dec.csv";
		
		boolean test = false; 
		
		String path;
		int windowSize = 0; 
		
		if(test){
			path = "/Users/srinath/playground/data-science/text-analytics/data/LKA/test-lka.csv";
			windowSize = 2; 
		}else{
//			path = "data/lka-2015Dec.csv";
//			windowSize = 14;
			path = "data/us-election-aprial07.csv";
			windowSize = 3; 
		}
		
		
		
		WordEmbeddingsProcessor embeddingsProcessor = new WordEmbeddingsProcessor();
		GenericTweetsProcessor.processTweets(path, new SimpleTweetsPreprocessor(), new TweetWordsProcessor[]{embeddingsProcessor});
//      System.out.println("Graph");
		Map<String, Integer> topBiGrams = embeddingsProcessor.getGraph().calculateTopBigrams(10);
//      for(Entry<String, Integer> e: topBiGrams.entrySet()){
//      	System.out.println(e.getKey()+"="+e.getValue());	
//      }

		
		final StandoutWordsDetector standoutWordsDetector = new StandoutWordsDetector(windowSize);
		standoutWordsDetector.setEmbeddingsGraph(embeddingsProcessor.getGraph());
		GenericTweetsProcessor.processTweets(path, new SimpleTweetsPreprocessor(), new TweetWordsProcessor[]{standoutWordsDetector});

		embeddingsProcessor.getGraph().printCoOccurenceGraphEdges();
	}

		
		
}