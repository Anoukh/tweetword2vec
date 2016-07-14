package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

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

public class TwitterUtils {
    private static Set stopWords = new HashSet<>();

    static {
        try {
            String wordsAsStr = FileUtils.readFile("resources/stopwords.txt");
            String[] stopwordsArray = wordsAsStr.split("\\s+");
            Collections.addAll(stopWords, stopwordsArray);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static boolean isStopWord(String word) {
        return stopWords.contains(word) || word.trim().length() < 2;
    }


    public static String[] removeStopWords(String[] words) {
        List<String> filteredWords = new ArrayList<>();
        for (String w : words) {
            if (!isStopWord(w)) {
                String cleanupWord = w.replaceAll("[^A-z0-9#]", "");
                if (cleanupWord.length() > 1) {
                    filteredWords.add(cleanupWord);
                }

            }
        }
        return filteredWords.toArray(new String[0]);
    }


    public static JavaRDD<String> loadTwitterData(JavaSparkContext sc, String file) {
        JavaRDD<String> logData = sc.textFile(file).cache();

        //long numAs = logData.count();

//	    long numBs = logData.filter(new Function<String, Boolean>() {
//	      public Boolean call(String s) { return s.contains("Cricket"); }
//	    }).count();

//	    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);


        JavaRDD<String> tweetText = logData.map(new Function<String, String>() {
            @Override
            public String call(String s) {
                String[] tokens = s.split(" ");
//                if (tokens.length > 2) {
                String t = new String();
                for (String token:
                     tokens) {
                    t += token.replaceAll("[\"')(]", "").toLowerCase();
                    t = t.replaceAll("[:!,.<]", "");
                    t = t.replaceAll("#", "");
                    t = t.replaceAll("@[A-z0-9]+", "");
                    t = t.replaceAll("[0-9]*", "");
                    t = t.replaceAll("http://[^\\s,]*", "");
                    t = t.replaceAll("https://[^\\s,]*", "");
                    t = t.replaceAll("lka", "lanka");
                    t = t.replaceAll("sri lanka", "lanka");
                    t = t.replaceAll("prime minister", "PM");
                    t = t.replaceAll("&amp;", "");
                    t = t.replaceAll("…", "");
                    t = t.replaceAll(" RT ", "");
                    t += " ";
                }
                    return t;
//                } else {
//                    return null;
//                }
            }
        });

        tweetText = tweetText.filter(new Function<String, Boolean>() {
            public Boolean call(String s) {
                return s != null;
            }
        });
        return tweetText;
    }

    public static float format(double value) {
        return Math.round(1000 * value) / 1000f;
    }
}
