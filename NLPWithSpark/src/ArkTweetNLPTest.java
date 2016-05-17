import java.io.IOException;
import java.util.List;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;

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

public class ArkTweetNLPTest {

	/*
	 * get the jar from https://code.google.com/archive/p/ark-tweet-nlp/downloads, new version ( in git hub does not work)
	 */
	
	public static void main(String[] args) throws IOException {
		Tagger tagger=new Tagger();
	    tagger.loadModel("/Users/srinath/code/workspace/NLPWithSpark/projects/ark-tweet-nlp-master/models/model.20120919");
	    String tweet="@Dolb this is very bad #way \"http://bitly.com/44\" :)";
	    List<TaggedToken> tt=tagger.tokenizeAndTag(tweet);
	    for (  TaggedToken a : tt) {
	      System.out.println(a.token + ":\t" + a.tag);
	    }

	}

}
