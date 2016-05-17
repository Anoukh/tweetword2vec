import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.TSne;

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

public class RunTsneOnCSV {

	public static void main(String[] args) {
		int initial_dims = 55;
	    double perplexity = 20.0;

		System.out.println("Size=" + vectorsAsDouble.length);
	    TSne tsne = new FastTSne();
		double [][] Y = tsne.tsne(vectorsAsDouble, 2, initial_dims, perplexity);   
		
		index = 0; 
		for(double[] t: Y){
			System.out.println(words.get(index)+","+String.format("%.2f", t[0]) +","+String.format("%.2f", t[1]));
			index++;			
		}

	}

}
