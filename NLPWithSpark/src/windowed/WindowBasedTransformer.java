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

package windowed;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import utils.CSVLineParser;
import utils.FileUtils;

public class WindowBasedTransformer {
	private static SimpleDateFormat dateForamtter = new SimpleDateFormat("yyyy-MM-dd");
	

	private int windowSize;
	public WindowBasedTransformer(int windowSize){
		this.windowSize = windowSize;
	}
	
	Map<String, ValueBuffer> map = new HashMap<String, WindowBasedTransformer.ValueBuffer>();
	
	
	public class ValueBuffer{
		int index = 0;
		double[] cbuffer;
		boolean full = false;
		
		public ValueBuffer(int windowSize){
			cbuffer = new double[windowSize];
		}
		
		public void append(double val){
			cbuffer[index] = val;
			index = (index+1) % cbuffer.length;
			if(!full && index == 0){
				full = true;
			}
			
			//System.out.println(val + "+ "+ Arrays.toString(cbuffer) + " "+ full);
		}
		
		public double[] getValues(){
			double[] newBuf = new double[windowSize];
			System.arraycopy(cbuffer, 0, newBuf, 0, windowSize);			
			return newBuf;
		}
	}
	
	
	
	
	public double[] processDataPoint(String group, Double value){
		ValueBuffer buf = map.get(group);
		if(buf == null){
			buf = new ValueBuffer(windowSize);
			map.put(group, buf);
		}
		double[] wdata = null; 
		
		if(buf.full){
			wdata = buf.getValues();
		}
		buf.append(value);
		return wdata;
	}
	
	
	
	
	public static void main(String[] args) throws Exception{
		final CSVLineParser parser = new CSVLineParser();
		final int windowSize = 4;
		final AtomicInteger count = new AtomicInteger();
		final WindowBasedTransformer processor = new WindowBasedTransformer(windowSize);
		final long startTime = dateForamtter.parse("2013-01-01").getTime();
		
		final BufferedWriter w = new BufferedWriter(new FileWriter("rossmannW.csv"));
		w.write("Store,DayOfWeek,Date,Customers,Open,Promo,StateHoliday,SchoolHoliday,W1,W2,W3,W4,Sales\n");
    	
    	//FileUtils.doForEachLine("/Users/srinath/playground/data-science/keras-theano/TimeSeriesRegression/rossmannStore1.csv", new FileUtils.LineProcessor() {
		FileUtils.doForEachLine("/Users/srinath/playground/data-science/keras-theano/TimeSeriesRegression/rossmann.csv", new FileUtils.LineProcessor() {
			public boolean process(String line)  {
				try {
					String[] tokens = line.replaceAll("\"", "").split(",");
					
					String store = tokens[0];
					String dayOfWeek = tokens[1];
					
					Date date = dateForamtter.parse(tokens[2]);					
					long daysSinceStart = (date.getTime() - startTime)/(1000*60*60*24);
					double sales = Double.parseDouble(tokens[3]);
					String customers = tokens[4];
					String isOpen = tokens[5];
					String isPromo = tokens[6];
					float StateHoliday = Float.parseFloat(tokens[7]);
					String SchoolHoliday = tokens[8];
					
					double[] wdata = processor.processDataPoint(store, sales);
					StringBuffer buf = new StringBuffer();
					if(wdata != null){						
						buf.append(store);
						buf.append(",").append(dayOfWeek);
						buf.append(",").append(daysSinceStart);
						buf.append(",").append(customers);
						buf.append(",").append(isOpen);
						buf.append(",").append(isPromo);
						buf.append(",").append(StateHoliday);
						buf.append(",").append(SchoolHoliday);
						for(int i=0;i<wdata.length;i++){
							buf.append(",").append(wdata[i]);
						}
						buf.append(",").append(sales);
						buf.append("\n");
						//System.out.println(buf);
						w.write(buf.toString());
						
					}
					
					boolean isContinue = true;
					//isContinue = count.incrementAndGet() < 10;
//					if(store.equals("1")){
//						isContinue = count.incrementAndGet() < 10;
//						System.out.println(buf);
//					}
					
					//return ;
					return isContinue;
				} catch (NumberFormatException e) {
					return true;
				} catch (ParseException e) {
					e.printStackTrace();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					return true;
				}
			}
		});
		w.close();
	}
	

}
