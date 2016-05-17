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

import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaScriptPrinter {
	private static SimpleDateFormat dateForamtter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static SimpleDateFormat dateForamtterSimple = new SimpleDateFormat("dd/MM/yyyy");


	private static final long DAY_IN_MS = 1000*60*60*24;


	public static void main(String[] args) throws Exception{
		StringBuffer buf = new StringBuffer();
		for(int i =0;i<26;i++){
			buf.append("\tspec1.data[0].values = wordsData["+i+"];\n");
			buf.append("\tvg.parse.spec(spec1, function(chart) {\n");
			buf.append("\tthis.view = chart({\n");
			buf.append("\t      el: \"#dChart"+i+"\"\n");
			buf.append("\t  }).update();\n");
			buf.append("\t});\n");
			
		}
		System.out.println(buf.toString());
		
		long startTime = dateForamtter.parse("01/01/2014 00:00:00").getTime();

		int cellsPerRow = 5;
		buf = new StringBuffer();
		
		for(int i =0;i<5;i++){
			buf.append("\t<tr>\n");
			for(int j=0;j<cellsPerRow;j++){
				int index = (cellsPerRow*i+j);
				long time  = startTime + DAY_IN_MS*( 800+index);
				buf.append("\t\t<td><h2>"+dateForamtterSimple.format(new Date(time))+"<h2><div id=\"dChart"+index+"\">\n");
			}
			buf.append("\t</tr>\n");
		}
		
		System.out.println(buf.toString());
		
	}

}
