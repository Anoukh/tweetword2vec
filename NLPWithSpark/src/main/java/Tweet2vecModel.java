import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.apache.spark.mllib.linalg.Vector;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.Map;
import utils.TwitterUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Tweet2vecModel {
    public static void main(String[] args) {
        String logFile = "/home/anoukh/SentimentAnalysis/ml-projects-java/NLPWithSpark/Tweets.csv"; // Should be some file on your system

        SparkConf conf = new SparkConf().setAppName("LKA").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> tweetText = TwitterUtils.loadTwitterData(sc, logFile);
        List<String> collectedList = tweetText.collect();

        Word2VecModel model = Word2VecModel.load(sc.sc(), "kaggle.model");

        java.util.Map<String, List<Double>> finalVector = new HashMap<>();
        long count = 0;
        for (String tweet : collectedList) { //For each tweet
            double[] totalVectorValue;
            List<Double> theList = new ArrayList<>();
            String[] tokens = tweet.split(" ");

            for (String word: tokens) { //For each word in the tweet
                count++;
                Vector vectorValues = null;
                try{
                    vectorValues = model.transform(word);
                }catch (IllegalStateException e){
//                    e.printStackTrace();
                    count++;
                }
                if (vectorValues != null){
                    totalVectorValue = vectorValues.toArray();
                    for (double temp: totalVectorValue) {
                        theList.add(temp);
                    }
                }
                if (count == 30){
                    break;
                }
            }

            finalVector.put(tweet, theList);
        }


//        Tuple2<String, Object>[] synonyms = model.findSynonyms("vote", 40);
//
//        for(Tuple2<String, Object> d: synonyms) {
//            System.out.println(d._1 + " "+ d._2);
//        }

//        System.out.println(count);
        int max = 300;

        java.util.Iterator<java.util.Map.Entry<String, List<Double>>> iterate = finalVector.entrySet().iterator();

        while (iterate.hasNext()){
            java.util.Map.Entry entry = iterate.next();
            List<Double> val = (List<Double>) entry.getValue();

            for (int i = val.size(); i < max; i ++){
                val.add(0.0d);
            }
        }

        for (Object temp: finalVector.values()) {
            if (max < ((List<Double>)temp).size()){
                max = ((List<Double>)temp).size();
            }

        }

        File file = new File("/home/anoukh/SentimentAnalysis/ml-projects-java/NLPWithSpark/kaggleTweetModel.csv");
        FileWriter writer = null;
        // if file doesnt exists, then create it
        try{

            if (!file.exists()) {
                file.createNewFile();
            }

            writer = new FileWriter(file);


            java.util.Iterator<java.util.Map.Entry<String, List<Double>>> iterate2 = finalVector.entrySet().iterator();

            while (iterate2.hasNext()){
                java.util.Map.Entry entry = iterate2.next();
                List<Double> val = (List<Double>) entry.getValue();

                String key = (String)entry.getKey();
                writer.append(key);

                for (int i = 0; i < val.size(); i ++){
                    writer.append(",");
                    writer.append(Double.toString(val.get(i)));
                }
                writer.append("\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        System.out.println(max);
//        Map<String, float[]> vectors = model.getVectors();
//        Iterator<Tuple2<String, float[]>> iterator = vectors.iterator();
//        List<String> words = new ArrayList<>();
//
////		int maxSize = 1000;
//        double[][] vectorsAsDouble = null;
//        int index = 0;
//        while(iterator.hasNext()){
//            Tuple2<String, float[]>  v = iterator.next();
//            if(index < vectors.size()){
//                words.add(v._1);
//                System.out.println(v._1);
//                if(vectorsAsDouble == null){
//                    vectorsAsDouble = new double[Math.min(vectors.size(), Integer.MAX_VALUE)][v._2.length];
//                }
//                int j=0;
//                for(float f: v._2){
//                    vectorsAsDouble[index][j] = f;
//                    j++;
//                }
//                index++;
//            }
//        }
    }
}
