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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class WindowOperation implements StreamingOperator{
	Map<String, TreeMap<Long, Long>> map = new HashMap<>();
	
	long lastDay;
	private int numberOfDays = 2;
	
	
	public WindowOperation(int numberOfDays) {
		super();
		this.numberOfDays = numberOfDays;
	}
	
	
	@Override
	public Object[][] process(Object[] event) {
		long day  = (Long)event[0];
		String word = (String)event[1];
		long freqs = (Long)event[2];
		
		TreeMap<Long, Long> sortedFreq = map.get(word);
		if(sortedFreq == null){
			sortedFreq = new TreeMap<Long, Long>();
			map.put(word, sortedFreq);
		}
		
		Long t = sortedFreq.get(day);
		if(t != null){
			freqs = freqs + t;
		}
		sortedFreq.put(day, freqs);
		
		Iterator<Entry<Long, Long>> iterator = sortedFreq.headMap(1+ day - numberOfDays).entrySet().iterator();
		while(iterator.hasNext()){
			iterator.next();
			iterator.remove();
		}
		
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		long dayIndex = 1+ day - numberOfDays; 
		for(Entry<Long, Long> e: sortedFreq.entrySet()){
			while(e.getKey() > dayIndex){
				dayIndex++;
				stats.addValue(0);
			}
			stats.addValue(e.getValue());
		}
		
		if(sortedFreq.size() > numberOfDays){
			System.out.println(day + " size=" +sortedFreq.size() + " "+ sortedFreq);	
		}
		
		double mean = stats.getMean();
		double meadian = stats.getPercentile(50);
		mean = (mean == 0)? 1: mean;
		meadian = (meadian == 0)?1: meadian;
		double stddev = stats.getStandardDeviation();
		stddev = (stddev == 0)? 1: stddev;
		double cov = stddev/ mean;
		
		
		//double swna = Math.log(freqs)*freqs/stats.getMean();
		double swna1 = Math.log(meadian)*Math.abs(freqs-meadian)/stddev;
		if(Double.isNaN(swna1)){
			System.out.println();
		}
		double swna2 = Math.abs(freqs-meadian)/stddev;
		double swna3 = freqs/ (meadian*cov);
		
		
		Gaussian gaussian = new Gaussian(100, 50);

		double swna4 = (0.1+100*gaussian.value(meadian)) * freqs/ (meadian*cov);
		
		
		
		
		int percentageAvialableValues = Math.round(100*sortedFreq.size()/numberOfDays);
		//System.out.println("#"+ word + " " + freqs + " "+ stats.getMean() + Arrays.toString(stats.getValues()));
		return new Object[][]{{day, word, swna1, freqs, stats.getMean(), meadian, stddev, 
			swna2, swna3, swna4, cov, percentageAvialableValues}};
		
//		if(freqs > 3 && swna> 5){
//			return new Object[][]{{day, word, swna}};	
//		}else{
//			return null;
//		}
		
		
	}
}
