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

package streaming;

import streaming.core.TweetPreprocessor;

public class SimpleTweetsPreprocessor implements TweetPreprocessor{

	@Override
	public String cleanup(String t) {
		t= t.replaceAll("[\"')(]", "").toLowerCase();
		  //clean up
		  t= t.replaceAll("http://[^\\s,]*", "");
		  t= t.replaceAll("https://[^\\s,]*", "");
		  t = t.replaceAll("[:!,.<]", "");
		  t = t.replaceAll("#", "");
		  t = t.replaceAll("rt", "");
		  t = t.replaceAll("@[A-z0-9]+", "");
		  t= t.replaceAll("[0-9]*", "");
		  t= t.replaceAll("lka", "lanka");
		  t= t.replaceAll("sri lanka", "lanka");
		  t= t.replaceAll("prime minister", "PM");
		  //t = t.replaceAll("[^A-z0-9@$\\s]", "");
		return t;
	}

}
