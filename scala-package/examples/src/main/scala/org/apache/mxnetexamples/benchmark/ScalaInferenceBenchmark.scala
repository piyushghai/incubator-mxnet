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
import org.apache.mxnet._
import org.apache.mxnet.infer.Classifier
import org.kohsuke.args4j.{CmdLineParser, Option}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object ScalaInferenceBenchmark {

  private val logger = LoggerFactory.getLogger(classOf[ScalaInferenceBenchmark])

  def loadModel(objectToRun: InferBase, context: Array[Context]):
  Any = {
    objectToRun.loadModel(context)
  }

  def loadDataSet(objectToRun: InferBase):
  Any = {
    objectToRun.loadSingleData()
  }

  def loadBatchDataSet(objectToRun: InferBase, batchSize: Int):
  List[Any] = {
    objectToRun.loadBatchFileList(batchSize)
  }

  def runInference(objectToRun: InferBase, loadedModel: Classifier,  dataSet: Any, totalRuns: Int):
  List[Long] = {
    var inferenceTimes: List[Long] = List()

    for (i <- 1 to totalRuns) {
      val startTimeSingle = System.currentTimeMillis()
      objectToRun.runSingleInference(loadedModel, dataSet)
      val estimatedTimeSingle = System.currentTimeMillis() - startTimeSingle
      inferenceTimes = estimatedTimeSingle :: inferenceTimes
      printf("Inference time at iteration: %d is : %d \n", i, estimatedTimeSingle)
    }

    inferenceTimes
  }

  def runBatchInference(objecToRun: InferBase, loadedModel: Classifier, dataSetBatches: List[Any]):
  List[Long] = {

    var inferenceTimes: List[Long] = List()
    for (batch <- dataSetBatches) {
      val loadedBatch = objecToRun.loadInputBatch(batch)
      val startTimeSingle = System.currentTimeMillis()
      objecToRun.runBatchInference(loadedModel, loadedBatch)
      val estimatedTimeSingle = System.currentTimeMillis() - startTimeSingle
      inferenceTimes = estimatedTimeSingle :: inferenceTimes
      printf("Batch Inference time is : %d \n", estimatedTimeSingle)
    }

    inferenceTimes
  }

  def percentile(p: Int, seq: Seq[Long]): Long = {
    val sorted = seq.sorted
    val k = math.ceil((seq.length - 1) * (p / 100.0)).toInt
    return sorted(k)
  }

  def printStatistics(inferenceTimes: List[Long])  {

    val times: Seq[Long] = inferenceTimes
    val p50 = percentile(50, times)
    val p99 = percentile(99, times)
    val average = times.sum / (times.length * 1.0)

    printf("\nLatency p99 %d(ms), p50 %d(ms), average %f(ms)", p99, p50, average)

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

      val inputImagePath = if (inst.inputPath == null) System.getenv("MXNET_HOME")
      else inst.inputPath

      val inputImageDir = if (inst.inputDir == null) System.getenv("MXNET_HOME")
      else inst.inputDir

      val exampleName = if (inst.exampleName == null) "ImageClassifierExample"
      else inst.exampleName

      val count = inst.count.toString().toInt

      val batchSize = inst.batchSize.toString.toInt

      val exampleToBenchmark : InferBase = exampleName match {
        case "ImageClassifierExample" =>
          new ImageClassifierExample(modelPathPrefix, inputImagePath, inputImageDir)
        case "ObjectDetectionExample" =>
          new SSDClassifierExample(modelPathPrefix, inputImagePath, inputImageDir)
        case _ => throw new Exception("Invalid example name to run")
      }

      logger.info("Running single inference call")
      // Benchmarking single inference call
      NDArrayCollector.auto().withScope {
        val loadedModel = loadModel(exampleToBenchmark, context)
        val dataSet = loadDataSet(exampleToBenchmark)
        val inferenceTimes = runInference(exampleToBenchmark, loadedModel, dataSet, count)
        printStatistics(inferenceTimes)
      }

      logger.info("Running for batch inference call")
      // Benchmarking batch inference call
      NDArrayCollector.auto().withScope {
        val loadedModel = loadModel(exampleToBenchmark, context)
        val batchDataSet = loadBatchDataSet(exampleToBenchmark, batchSize)
        val inferenceTimes = runBatchInference(exampleToBenchmark, loadedModel, batchDataSet)
        printStatistics(inferenceTimes)
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
  @Option(name = "--input-data", usage = "the input data path")
  private val inputPath: String = "/images/kitten.jpg"
  @Option(name = "--input-dir", usage = "the input directory path for batch inference")
  private val inputDir: String = "/images/"
  @Option(name = "--count", usage = "number of times to run inference")
  private val count: Int = 1000
  @Option(name = "--batchSize", usage = "BatchSize to run batchinference calls")
  private val batchSize: Int = 10

}