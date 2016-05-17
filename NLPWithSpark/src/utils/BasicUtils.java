package utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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

public class BasicUtils {
	private static SimpleDateFormat dateForamtter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static SimpleDateFormat dateForamtter2 = new SimpleDateFormat("dd/MM/yyyy");
	private static final long DAY_IN_MS = 1000*60*60*24;
	
	private static long START_TIME; 
	static{
		try {
			START_TIME = dateForamtter.parse("01/01/2014 00:00:00").getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static  void addAndIncrement2Map(Map<String, Integer> map, String key, int val){
		Integer t = map.get(key);
		if(t == null){
			t = new Integer(0);
		}
		t = t+val;
		map.put(key, t);
	}
	
	
	
	public static long fromDateToNumber(long ts) throws Exception{
		  //18/12/2015 23:51:11
		  long day = (ts - START_TIME )/DAY_IN_MS;
		  return day; 
	}
	
	public static String fromNumberToDate(long day){
		long time = START_TIME+ (day*DAY_IN_MS) + DAY_IN_MS/2;
		return dateForamtter2.format(new Date(time));
	}

	public static void main(String[] args) throws Exception{
		Date today = new Date();
		long dayAsNumber = fromDateToNumber(dateForamtter.format(today));
		System.out.println(dayAsNumber);
		String dayAsStr = fromNumberToDate(dayAsNumber);
		System.out.println(dayAsStr);
		
	}
	
}
