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

package streaming.core;

import java.io.FileReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import tweets.TweetWordsProcessor;
import utils.BasicUtils;
import utils.FileUtils;

public class GenericTweetsProcessor {
	private static SimpleDateFormat dateForamtter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static Pattern pattern1 = Pattern.compile("[0-9+],[^,]+,\"([^\"]+)\",[^,]+,([^,]+).*");
	private static Pattern pattern2 = Pattern.compile("[0-9+],[^,]+,([^,]+),[^,]+,([^,]+).*");


	public static void processTweets(String path, final TweetPreprocessor preprocessor, final TweetWordsProcessor[] precessors) throws Exception{	
		final AtomicInteger totalCount = new AtomicInteger();
		final AtomicInteger errorCount = new AtomicInteger();

		for(TweetWordsProcessor processor: precessors){
			  processor.init();
		 }
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
						
						  String t = matcher.group(1);
						  t = preprocessor.cleanup(t);
						  
						  //18/12/2015 23:51:11
						  String[] words = t.split("\\s+");
						  
						  long ts = dateForamtter.parse(matcher.group(2).trim()).getTime();
						  long day = BasicUtils.fromDateToNumber(ts);

						  for(TweetWordsProcessor processor: precessors){
							  processor.processATweet(ts, day, words);
						  }
						 
//						  FileUtils.writeToFile(buf.toString(), new File("wordranks.csv"));
					  }else{
						  //System.out.println("!!!!"+ line);
						  errorCount.incrementAndGet();
					  }
				} catch (Exception e) {
					System.out.println(" Error: "+ e.getMessage() + " in "+ line);
					errorCount.incrementAndGet();
				}
		    	  totalCount.incrementAndGet();
		    	  return true;

			}
		});
		for(TweetWordsProcessor processor: precessors){
			  processor.finish();
		 }

	}
	
	
	public static void main(String[] args) throws Exception{
		
		
		
		Reader in = new FileReader("data/us-election-aprial07.csv");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		for (CSVRecord record : records) {
		    System.out.println(record.get(0) + " "+ record.get(1));
		}
	}
}




