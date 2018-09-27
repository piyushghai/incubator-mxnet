/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mxnetexamples.benchmark

import org.apache.mxnetexamples.InferBase
import org.apache.mxnetexamples.infer.imageclassifier.ImageClassifierExample
import org.apache.mxnetexamples.infer.objectdetector.SSDClassifierExample
import org.apache.mxnet._
import org.apache.mxnet.infer.Classifier
import org.kohsuke.args4j.{CmdLineParser, Option}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object ScalaInferenceBenchmark {

  private val logger = LoggerFactory.getLogger(classOf[ScalaInferenceBenchmark])

  def loadModel(objectToRun: InferBase, context: Array[Context]):
  Classifier = {
    objectToRun.loadModel(context)
  }

  def loadDataSet(objectToRun: InferBase):
  Any = {
    objectToRun.loadDataSet()
  }

  def runInference(objectToRun: InferBase, loadedModel: Classifier,  dataSet: Any, totalRuns: Int):
  Any = {

    for (i <- 1 to totalRuns) {
      val startTimeSingle = System.currentTimeMillis()
      objectToRun.runInference(loadedModel, dataSet)
      val estimatedTimeSingle = System.currentTimeMillis() - startTimeSingle
      printf("Inference time at iteration: %d is : %d \n",i, estimatedTimeSingle)
    }
  }


  def main(args: Array[String]): Unit = {
    val inst = new ScalaInferenceBenchmark

    val parser: CmdLineParser = new CmdLineParser(inst)

    var context = Context.cpu()
    if (System.getenv().containsKey("SCALA_TEST_ON_GPU") &&
      System.getenv("SCALA_TEST_ON_GPU").toInt == 1) {
      context = Context.gpu()
    }

    try {
      parser.parseArgument(args.toList.asJava)


      val modelPathPrefix = if (inst.modelPathPrefix == null) System.getenv("MXNET_HOME")
      else inst.modelPathPrefix

      val inputImagePath = if (inst.inputImagePath == null) System.getenv("MXNET_HOME")
      else inst.inputImagePath

      val inputImageDir = if (inst.inputImageDir == null) System.getenv("MXNET_HOME")
      else inst.inputImageDir

      val exampleName = if (inst.exampleName == null) "ImageClassifierExample"
      else inst.exampleName

      val count = inst.count.toString().toInt

      val exampleToBenchmark : InferBase = exampleName match {
//        case "ImageClassifierExample" => new ImageClassifierExample(modelPathPrefix, inputImagePath, inputImageDir)
        case "SSDClassifierExample" => new SSDClassifierExample(modelPathPrefix, inputImagePath, inputImageDir)
        case _ => throw new Exception("Invalid example name to run")
      }

      NDArrayCollector.auto().withScope {
        val loadedModel = loadModel(exampleToBenchmark, context)
        val dataSet = loadDataSet(exampleToBenchmark)
        runInference(exampleToBenchmark, loadedModel, dataSet, count)

      }
    } catch {
      case ex: Exception => {
        logger.error(ex.getMessage, ex)
        parser.printUsage(System.err)
        sys.exit(1)
      }
    }
  }


}

class ScalaInferenceBenchmark {
  @Option(name = "--example", usage = "The scala example to benchmark")
  private val exampleName: String = "ImageClassifierExample"
  @Option(name = "--model-path-prefix", usage = "the input model directory")
  private val modelPathPrefix: String = "/resnet-152/resnet-152"
  @Option(name = "--input-image", usage = "the input image")
  private val inputImagePath: String = "/images/kitten.jpg"
  @Option(name = "--input-dir", usage = "the input batch of images directory")
  private val inputImageDir: String = "/images/"
  @Option(name = "--count", usage = "number of times to run inference")
  private val count: Int = 1000
}