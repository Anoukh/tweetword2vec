import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

import utils.FileUtils;

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

public class SiddhiWordcountByDayTest {

	private static SimpleDateFormat dateForamtter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static Pattern pattern1 = Pattern.compile("[0-9+],[^,]+,\"([^\"]+)\",[^,]+,([^,]+).*");
	private static Pattern pattern2 = Pattern.compile("[0-9+],[^,]+,([^,]+),[^,]+,([^,]+).*");
	
	private static final long DAY_IN_MS = 1000*60*60*24;
	
	public static void main(String[] args) throws Exception{
		final AtomicInteger totalCount = new AtomicInteger();
		final AtomicInteger errorCount = new AtomicInteger();
		
		boolean test = false; 
		String path;
		if(test){
			path = "/Users/srinath/playground/data-science/text-analytics/data/LKA/test-lka.csv";
		}else{
//			path = "data/lka-2015Dec.csv";
//			windowSize = 14;
			path = "data/us-election-aprial07.csv";
		}
		
		// Creating Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();
        
        //https://docs.wso2.com/display/CEP400/Sample+0114+-+Using+External+Time+Windows

        
        String executionPlan = FileUtils.readFile("src/tweetWordCountByDay.eql");
        
        siddhiManager.setExtension("math:SWNV", SWNVAggrigator.class);
        
        //Generating runtime
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        //Adding callback to retrieve output events from query
        //executionPlanRuntime.addCallback("WordFreqByDay", new StreamCallback() {
        //executionPlanRuntime.addCallback("WordSWNVByDay", new StreamCallback() {
        
        executionPlanRuntime.addCallback("WordFreqByDay", new StreamCallback() {
			public void receive(Event[] events) {
				for(Event e: events){
					System.out.println(Arrays.toString(e.getData()));
				}
			}
		});

        //Retrieving InputHandler to push events into Siddhi
        final InputHandler inputHandler = executionPlanRuntime.getInputHandler("TweetWordsStream");

        //Starting event processing
        executionPlanRuntime.start();
        
        //send tweets data 
        //String path = "/Users/srinath/playground/data-science/text-analytics/data/LKA/lka-2015Dec.csv";
        FileUtils.doForEachLine(path, new FileUtils.LineProcessor() {
			public boolean process(String line)  {
				Matcher matcher = pattern1.matcher(line); 
				if(!matcher.find()){
					matcher = pattern2.matcher(line);
					if(!matcher.find()){
						matcher = null;
					}
				}
		    	  try {
					if(matcher != null){
						
						  String t = matcher.group(1).replaceAll("[\"')(]", "").toLowerCase();
						  //clean up
//						  t = t.replaceAll("[:!,.<]", "");
//			    		  t = t.replaceAll("#", "");
//			    		  t = t.replaceAll("@[A-z0-9]+", "");
//			    		  t= t.replaceAll("[0-9]*", "");
//			    		  t= t.replaceAll("http://[^\\s,]*", "");
//			    		  t= t.replaceAll("https://[^\\s,]*", "");
//			    		  t= t.replaceAll("lka", "lanka");
//			    		  t= t.replaceAll("sri lanka", "lanka");
//			    		  t= t.replaceAll("prime minister", "PM");
						  
						  //18/12/2015 23:51:11
						  long ts = dateForamtter.parse(matcher.group(2).trim()).getTime();
						  String[] words = t.split("\\s+");
						  long day = ts/DAY_IN_MS;
						  //(ts long, long day, word string, long freq)
						  //System.out.println(matcher.group(2) + " " +t);

						  for(String w: words){
							  inputHandler.send(new Object[]{new Long(ts), new Long(day) , w, new Long(1)});  
						  }
					  }else{
						  //System.out.println("!!!!"+ line);
						  errorCount.incrementAndGet();
					  }
				} catch (Exception e) {
					//e.printStackTrace();
					errorCount.incrementAndGet();
				}
		    	  totalCount.incrementAndGet();
					return true;

			}
		});
        Thread.sleep(500);

        //Shutting down the runtime
        executionPlanRuntime.shutdown();

        //Shutting down Siddhi
        siddhiManager.shutdown();
        System.out.println("Errors="+ errorCount + "/"+ totalCount);

	}

}
