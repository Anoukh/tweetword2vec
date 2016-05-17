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

package streaming.operators;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import streaming.core.WindowOperation;
import text.EmbeddingsGraph;
import tweets.TweetWordsProcessor;
import utils.TopWordsByDayTracker;
import utils.TwitterUtils;

public class StandoutWordsDetector implements TweetWordsProcessor {
	WordCountsByDay wordCountsByDayProcessor; 
	WindowOperation windowProcesor; 
	int windowSize; 
	TopWordsByDayTracker topWordsByDayTracker;
	BufferedWriter writer;
	
	private EmbeddingsGraph embeddingsGraph;
	
	
	
	public void setEmbeddingsGraph(EmbeddingsGraph embeddingsGraph) {
		this.embeddingsGraph = embeddingsGraph;
	}

	public StandoutWordsDetector(int windowSize) {
		super();
		this.windowSize = windowSize;
	}

	@Override
	public void init() throws Exception{
		writer = new BufferedWriter(new FileWriter("wordRatingsByDay.csv"));
		writer.write("day, word, swna1, freqs, mean, median, stddev, swna2, swna3, swna4, cov, percentageAvialableValues\n");

		wordCountsByDayProcessor = new WordCountsByDay();
		windowProcesor = new WindowOperation(windowSize);
		topWordsByDayTracker = new TopWordsByDayTracker();

	}

	@Override
	public void processATweet(long timestamp, long day, String[] words) throws Exception{
		words = TwitterUtils.removeStopWords(words);
		List<String> wordsToProcess = new ArrayList<>();
		Collections.addAll(wordsToProcess, words);
				
		if(embeddingsGraph != null){
			for(int i=0;i<words.length;i++){
				for(int j=1;j<4;j++){
					if(i+j<words.length){
						String w1 = words[i];
						String w2 = words[i+j];
						double wordCooccueranceProbability = embeddingsGraph.getWordCooccueranceProbability(w1, w2);
						if(wordCooccueranceProbability > 0.4){
							wordsToProcess.add(new StringBuffer().append(w1).append("#").append(w2).toString());
						}
					}
				}
			}
			
			
//			for(int i=0;i<words.length;i++){
//				int next = i+1; 
//				if(next < words.length){
//					double wordCooccueranceProbability = embeddingsGraph.getWordCooccueranceProbability(words[i], words[next]);
//					if(wordCooccueranceProbability > 0.4){
//						wordsToProcess.add(new StringBuffer().append(words[i]).append("#").append(words[next]).toString());
//					}
//				}
//			}
		}
		
		 for(String w: wordsToProcess){
			  Object[][] results = wordCountsByDayProcessor.process(new Object[]{ new Long(day) , w, new Long(1)}); 
			  if(results != null){
				  for(Object[] result: results){
					  //System.out.println("1>" + Arrays.toString(result));
					  Object[][] windowProcesorResults = windowProcesor.process(result);
					  
					  
					  if((long)windowProcesorResults[0][3] > 15){
						  //System.out.println("2>" + Arrays.toString(windowProcesorResults[0]));
						  String str = Arrays.toString(windowProcesorResults[0]).replaceAll("[\\[\\]]", "");
						  
						  System.out.println(str);
						  writer.write(str);writer.write("\n");
						  topWordsByDayTracker.add((long)windowProcesorResults[0][0], (String)windowProcesorResults[0][1], (Double)windowProcesorResults[0][8]);
					  }
				  }
			  }
		  }		  	
	}

	
	public void finish() throws Exception{
        writer.flush();
        writer.close();
        topWordsByDayTracker.printTopWordsByDay();
        topWordsByDayTracker.printTopWordsByDayJSON();
	}
}
