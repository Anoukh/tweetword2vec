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

import java.util.Arrays;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import streaming.SimpleTweetsPreprocessor;
import streaming.core.GenericTweetsProcessor;
import tweets.TweetWordsProcessor;
import utils.FileUtils;

public class SiddhiWordCountByDayProcessor implements TweetWordsProcessor {
	private InputHandler inputHandler;
	private SiddhiManager siddhiManager; 
	private ExecutionPlanRuntime executionPlanRuntime;
	
	@Override
	public void init() throws Exception {
		siddhiManager = new SiddhiManager();
        String executionPlan = FileUtils.readFile("src/tweetWordCountByDay.eql");
        executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        executionPlanRuntime.addCallback("WordFreqByDay", new StreamCallback() {
			public void receive(Event[] events) {
				for(Event e: events){
					System.out.println(Arrays.toString(e.getData()));
				}
			}
		});

        //Retrieving InputHandler to push events into Siddhi
        inputHandler = executionPlanRuntime.getInputHandler("TweetWordsStream");

        //Starting event processing
        executionPlanRuntime.start();
	}

	@Override
	public void processATweet(long timestamp, long day, String[] words)
			throws Exception {
		for(String w: words){
			  inputHandler.send(new Object[]{new Long(timestamp), new Long(day) , w, new Long(1)});  
		  }
		
	}

	@Override
	public void finish() throws Exception {
		Thread.sleep(500);

        //Shutting down the runtime
        executionPlanRuntime.shutdown();

        //Shutting down Siddhi
        siddhiManager.shutdown();
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
			path = "data/us-election-aprial07.csv";
			windowSize = 3; 
		}
		
		SiddhiWordCountByDayProcessor processor = new SiddhiWordCountByDayProcessor();
		GenericTweetsProcessor.processTweets(path, new SimpleTweetsPreprocessor(), new TweetWordsProcessor[]{processor});
	}
}
