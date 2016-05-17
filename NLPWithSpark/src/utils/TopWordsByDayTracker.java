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

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TopWordsByDayTracker {
	private int maxWordsPerDay = 10; 
	private double measureLimit = 15; 
	
	private TreeMap<Long, TreeMap<Double, String>> map = new TreeMap<>();
	
	public void add(long date, String word, double measure){
		TreeMap<Double, String> m2wMap = map.get(date);
		if(m2wMap == null){
			m2wMap = new TreeMap<>();
			map.put(date, m2wMap);
		}
		m2wMap.put(measure, word);
	}
	
	
	public void printTopWordsByDayJSON(){
		StringBuffer buf1 = new StringBuffer();
		buf1.append("[");
		for(Entry<Long, TreeMap<Double, String>> e: map.entrySet()){
			NavigableMap<Double, String> descendingMap = e.getValue().descendingMap();
			int i = 0;
			//StringBuffer buf = new StringBuffer();
			
			buf1.append("[");
			
			for(Entry<Double, String>  ew :descendingMap.entrySet()){
				if(measureLimit == -1  || ew.getKey() > measureLimit){
//					buf.append(ew.getValue()).append("(")
//						.append(String.format("%.2f",ew.getKey())).append(")").append(" ");
						buf1.append("{\"text\":\""+ew.getValue()+"\", \"value\":\""+Math.round(ew.getKey())+"\"},");
				}
				i++;
				if(measureLimit == -1 && i >= maxWordsPerDay){
					break;
				}
			}
			buf1.deleteCharAt(buf1.length()-1);
			buf1.append("],\n");
			//System.out.println(e.getKey()+"="+buf.toString());			
		}
		buf1.deleteCharAt(buf1.length()-1);
		buf1.append("]\n\n");
		System.out.print(buf1.toString());
		
	}
	
	
	public void printTopWordsByDay(){
		for(Entry<Long, TreeMap<Double, String>> e: map.entrySet()){
			NavigableMap<Double, String> descendingMap = e.getValue().descendingMap();
			int i = 0;
			StringBuffer buf = new StringBuffer();
			for(Entry<Double, String>  ew :descendingMap.entrySet()){
				if(measureLimit == -1  || ew.getKey() > measureLimit){
					buf.append(ew.getValue()).append("(")
						.append(String.format("%.2f",ew.getKey())).append(")").append(" ");
				}
				i++;
				if(measureLimit == -1 && i >= maxWordsPerDay){
					break;
				}
			}
			System.out.println(BasicUtils.fromNumberToDate(e.getKey())+"="+buf.toString());			
		}
		
	}

}
