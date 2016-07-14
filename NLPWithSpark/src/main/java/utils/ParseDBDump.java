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

package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ParseDBDump {

	private static Pattern pattern1 = Pattern.compile("\\(([^)]+)\\)");
	private static Pattern pattern2 = Pattern.compile("timeStamp(.*?)[0-9:]6[\\s0-9]+.*?text(.*?)hit_count");

	public static void main(String[] args) throws Exception {
		final AtomicInteger count = new AtomicInteger();
		
		final BufferedWriter w = new BufferedWriter(new FileWriter(
				"election2016Cleanup.csv"));
		w.write("date,tweet\n");
		
		FileUtils.doForEachLine("/Users/srinath/code/workspace/NLPWithSpark/data/dumpDASdb.sql", new FileUtils.LineProcessor() {
			public boolean process(String line)  {
//				if(line.contains("(")){
//					System.out.println(line);	
//				}					

				Matcher matcher = pattern1.matcher(line); 
				while(matcher.find()){
					int index = count.incrementAndGet();
//					if(index >= 100){
//						return false;
//					}
					
					String baseStr = matcher.group(1); 
					String[] tokens = baseStr.split(",");
					for(String t: tokens){
						t = t.replace("'","");
						t = t.replaceAll("[^A-z0-9#@/:\\s]", "");
						t = t.replaceAll("!", "");
						t = t.replaceAll(",", "");
						t = t.replaceAll("[0\\\\]+", " ");
						t = t.replaceAll("\\s+", " ");
						//t = t.replaceAll("![!\\s]+", "!");
						t = t.replaceAll("null", "");
						t = t.trim();
						if(t.contains("parent_text")){
							Matcher matcher2 = pattern2.matcher(t);
							if(matcher2.find()){
								try {
									w.write(matcher2.group(1) + ","+ matcher2.group(2).trim());
									w.write("\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}else{
								System.out.println("No match " + t);	
							}
														
						}
//						String decodedStr = new String(BaseEncoding.base64().decode(t)); 
//						System.out.println(decodedStr);
					}
					//System.out.println();
				}
				return true;
			}
		});
		w.flush();
		w.close();

	}

}
