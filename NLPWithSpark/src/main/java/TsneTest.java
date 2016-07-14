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

import java.io.File;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.utils.MatrixOps;
import com.jujutsu.tsne.SimpleTSne;
import com.jujutsu.tsne.TSne;
import com.jujutsu.utils.MatrixUtils;

public class TsneTest {
  public static void main(String [] args) {
    int initial_dims = 55;
    double perplexity = 20.0;
    
    double [][] X = MatrixUtils.simpleRead2DMatrix(new File("projects/T-SNE-Java-master/tsne-demos/src/main/resources/datasets/iris_X.txt"), ",");
    System.out.println(MatrixOps.doubleArrayToPrintString(X, ", ", 50,10));
    TSne tsne = new FastTSne();
	double [][] Y = tsne.tsne(X, 2, initial_dims, perplexity);        
    System.out.println(MatrixOps.doubleArrayToPrintString(Y, ", ", 50,10));
    //plotIris(Y);
    
//    double [][] X = MatrixUtils.simpleRead2DMatrix(new File("projects/T-SNE-Java-master/tsne-demos/src/main/resources/datasets/mnist2500_X.txt"), ",");
//    System.out.println(MatrixOps.doubleArrayToPrintString(X, ", ", 50,10));
//    TSne tsne = new FastTSne();
//    double [][] Y = tsne.tsne(X, 2, initial_dims, perplexity);   

    // Plot Y or save Y to file and plot with some other tool such as for instance R

  }
}
