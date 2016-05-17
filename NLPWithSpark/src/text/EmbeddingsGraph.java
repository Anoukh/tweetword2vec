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

package text;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import utils.TwitterUtils;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;


public class EmbeddingsGraph {
	public static class NodeData implements Comparable<NodeData>{
		String word; 
		int count; 
		double probability;
		boolean trailing;
		public NodeData(String word, int count, boolean trailing) {
			super();
			this.word = word;
			this.count = count;
			this.trailing = trailing;
		}
		@Override
		public int compareTo(NodeData o) {
			return this.count - o.count;
		}
	}
	
	
	Map<String, Integer> edgeStrength = new HashMap<>();
	
	TreeMultimap<String, NodeData> fromMapOnTopBiGrams = TreeMultimap.create();
	TreeMultimap<String, NodeData> toMapOnTopBiGrams = TreeMultimap.create();
	
	Map<String, Double> wordCoCouueranceProbabilityMap = new HashMap<>();

	
	public void addWords(String[] words){
		words = TwitterUtils.removeStopWords(words);
		for(int i=0;i<words.length;i++){
			for(int j=1;j<4;j++){
				if(i+j<words.length){
					String key = new StringBuffer().append(words[i])
							.append("#").append(words[i+j]).toString();
					Integer count = edgeStrength.get(key);
					if(count == null){
						count = new Integer(0);
					}
					count = count + 1;
					edgeStrength.put(key, count);
				}
			}
		}
	}
	
	/**
	 * This also update top bigram maps
	 * @param filter
	 * @return
	 */
	
	public Map<String,Integer> calculateTopBigrams(int filter){
		Map<String, Integer> subMap = new HashMap<>();
		for(Entry<String, Integer> e: edgeStrength.entrySet()){
			if(e.getValue()>filter){
				subMap.put(e.getKey(), e.getValue());
				String[] words = e.getKey().split("#");
				fromMapOnTopBiGrams.put(words[0], new NodeData(words[1], e.getValue(), true));
				toMapOnTopBiGrams.put(words[1], new NodeData(words[0], e.getValue(), false));
			}
		}
		
		
		TreeMultimap<Double, String> sortedWordCooccurences = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural().reverse());
		for(String key: fromMapOnTopBiGrams.keySet()){
			SortedSet<NodeData> list = fromMapOnTopBiGrams.get(key);
			int tot = 0; 
			for(NodeData d: list){
				tot = tot + d.count;
			}
			if(tot > 0){
				for(NodeData d: list){
					d.probability = d.count/(1.0*tot);
					wordCoCouueranceProbabilityMap.put(key+"#"+d.word, d.probability);
					sortedWordCooccurences.put(d.probability, key+"#"+d.word + "("+d.count+")");
				}
			}
		}
		
		for(String key: toMapOnTopBiGrams.keySet()){
			SortedSet<NodeData> list = toMapOnTopBiGrams.get(key);
			int tot = 0; 
			for(NodeData d: list){
				tot = tot + d.count;
			}
			if(tot > 0){
				for(NodeData d: list){
					d.probability = d.count/(1.0*tot);
				}
			}
		}
		
		System.out.println("### sortedWordCooccurences ####");
		
//		for(Entry<Double, String> e: sortedWordCooccurences.entries()){
//			if(e.getKey() > 0.4){
//				System.out.println(e.getValue()+ " "+ String.format("%.2f", e.getKey()));
//			}
//		}
		
		System.out.println("#######################");
		
		return subMap;
	}
	
//	public List<String> getCommonSequences(String word, double probabailityLimit){
//		
//		List<String> words = new ArrayList<>();
//		
//		
//	}
//	
//	
//	public void exploreRight(String word, List<String> words, double probabailityLimit, double currentProbabaility){
//		SortedSet<NodeData> fromList = fromMapOnTopBiGrams.get(word);
//		if(fromList != null && fromList.size() > 0){
//			double newProbabaility = currentProbabaility*fromList.first().probability;
//			if(newProbabaility > probabailityLimit){
//				words.add(word);
//				exploreRight(word, words, probabailityLimit, newProbabaility);
//			}
//		}
//	}
//
//	public void exploreLeft(String word, List<String> words, double probabailityLimit, double currentProbabaility){
//		SortedSet<NodeData> fromList = toMapOnTopBiGrams.get(word);
//		if(fromList != null && fromList.size() > 0){
//			double newProbabaility = currentProbabaility*fromList.first().probability;
//			if(newProbabaility > probabailityLimit){
//				words.add(word);
//				exploreLeft(word, words, probabailityLimit, newProbabaility);
//			}
//		}
//	}
	
	public double getWordCooccueranceProbability(String w1, String w2){
		Double  p = wordCoCouueranceProbabilityMap.get(new StringBuffer()
			.append(w1).append("#").append(w2).toString());
		return ((p == null) ? 0: p);
	}
	
	
	
	public void printCoOccurenceGraphEdges() throws Exception{
		if (fromMapOnTopBiGrams.size() == 0) {
			throw new Exception(
					"Please call calculateTopBigrams() before calling this");
		}

		BufferedWriter w = new BufferedWriter(new FileWriter(
				"wordCoOccuerances.csv"));
		w.write("from,to,freq, probability\n");

		for (String key : fromMapOnTopBiGrams.keySet()) {
			SortedSet<NodeData> list = fromMapOnTopBiGrams.get(key);
			for (NodeData d : list) {
				StringBuffer buf = new StringBuffer();
				buf.append(key).append(",").append(d.word).append(",")
						.append(d.count).append(",").append(d.probability)
						.append("\n");
				w.write(buf.toString());
			}
		}
		w.flush();
		w.close();
		
		w = new BufferedWriter(new FileWriter(
				"wordCoOccuerances.dot"));
		w.write("digraph {\nlayout=\"sfdp\"\n");
		
		//width=.5 at node level

		for (String key : fromMapOnTopBiGrams.keySet()) {
			SortedSet<NodeData> list = fromMapOnTopBiGrams.get(key);
			for (NodeData d : list) {
				StringBuffer buf = new StringBuffer();
				buf.append("\t").append(key.replaceAll("[^A-z0-9]", "")).append("->").append(d.word).append("\n");
				w.write(buf.toString());
			}
		}
		w.write("}\n");
		w.flush();
		w.close();
		
		
		/*
		 * <?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns"  
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns 
        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
  <key id="d0" for="node" attr.name="color" attr.type="string">
    <default>yellow</default>
  </key>
  <key id="d1" for="edge" attr.name="weight" attr.type="double"/>
  <graph id="G" edgedefault="undirected">
    <node id="n0">
      <data key="d0">green</data>
    </node>
    <node id="n1"/>
    <node id="n2">
      <data key="d0">blue</data>
    </node>
    <node id="n3">
      <data key="d0">red</data>
    </node>
    <node id="n4"/>
    <node id="n5">
      <data key="d0">turquoise</data>
    </node>
    <edge id="e0" source="n0" target="n2">
      <data key="d1">1.0</data>
    </edge>
    <edge id="e1" source="n0" target="n1">
      <data key="d1">1.0</data>
    </edge>
    <edge id="e2" source="n1" target="n3">
      <data key="d1">2.0</data>
    </edge>
    <edge id="e3" source="n3" target="n2"/>
    <edge id="e4" source="n2" target="n4"/>
    <edge id="e5" source="n3" target="n5"/>
    <edge id="e6" source="n5" target="n4">
      <data key="d1">1.1</data>
    </edge>
  </graph>
</graphml>
		 * 
		 */
		
		//Print Graph ML - http://graphml.graphdrawing.org/primer/graphml-primer.html
		//https://gephi.org/users/supported-graph-formats/
		
		
		
	}
	
	
}
