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

import streaming.core.StreamingOperator;
import utils.BasicUtils;

public class WordCountsByDay implements StreamingOperator{
	Map<String, Integer> map = new HashMap<String, Integer>();
	long lastDay = -1;

	@Override
	public Object[][] process(Object[] event) {
		long day  = (Long)event[0];
		String word = (String)event[1];
		//System.out.println(">"+lastDay + " "+ day + " " + map.size());
		if(lastDay == -1){
			lastDay = day;
		}
		if(lastDay == day){
			BasicUtils.addAndIncrement2Map(map, word, 1);
			return null;
		}else{
			Object[][] results = new Object[map.size()][];
			int index = 0;
			for(Entry<String, Integer> e: map.entrySet()){
				results[index] = new Object[]{lastDay, e.getKey(), new Long(e.getValue())};
				index++;
			}
			lastDay = day;
			map.clear();
			return results;
		}
	}

}
