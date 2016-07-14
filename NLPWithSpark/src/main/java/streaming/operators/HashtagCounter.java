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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import streaming.SimpleTweetsPreprocessor;
import streaming.core.GenericTweetsProcessor;
import tweets.TweetWordsProcessor;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class HashtagCounter implements TweetWordsProcessor {
	Map<String, Integer> hashtagCounterMap = new HashMap<>();
	
	
	@Override
	public void init() throws Exception{
	}

	@Override
	public void processATweet(long timestamp, long day, String[] words) throws Exception{
		for(String w: words){
			if(w.startsWith("#")){
				Integer count = hashtagCounterMap.get(w);
				if(count == null){
					count = new Integer(0);
				}
				count++;
				hashtagCounterMap.put(w, count);
			}
		}
		
	}

	
	public void finish() throws Exception{
		TreeMultimap<Integer, String> map = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural().reverse());
		
		
		for(Entry<String, Integer> e: hashtagCounterMap.entrySet()){
			map.put(e.getValue(), e.getKey());
		}
		
		Set<Entry<Integer, String>> entries = map.entries();
		for(Entry<Integer, String> e: entries){
			System.out.println(e.getValue() + " = "+ e.getKey());
		}
		
		
	}
	
	public static void main(String[] args) throws Exception {
		boolean test = false; 
		
		String path;
		int windowSize = 0; 
		
		if(test){
			path = "/Users/srinath/playground/data-science/text-analytics/data/LKA/test-lka.csv";
			windowSize = 2; 
		}else{
//			path = "data/lka-2015Dec.csv";
//			windowSize = 14;
			path = "data/us-election-march28.csv";
			windowSize = 3; 
		}
		
		
		
		HashtagCounter hashtagCounter  = new HashtagCounter();
		GenericTweetsProcessor.processTweets(path, new SimpleTweetsPreprocessor(), new TweetWordsProcessor[]{hashtagCounter});
	}
}
