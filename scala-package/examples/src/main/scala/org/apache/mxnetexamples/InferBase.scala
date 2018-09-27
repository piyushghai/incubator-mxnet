package org.apache.mxnetexamples

import org.apache.mxnet.infer.Classifier
import org.apache.mxnet._

abstract class InferBase {

  def loadModel(context: Array[Context]): Classifier
  def loadDataSet(): Any
  def runInference(loadedModel: Classifier, input: Any): Any

}
