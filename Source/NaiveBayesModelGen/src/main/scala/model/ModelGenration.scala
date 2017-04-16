package model

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import java.io.File

import scala.collection.immutable.HashMap

object ModelGenration{

  //Get all files in directory
  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def main(args: Array[String]) {
    //Training Data directory
    val trainFolder = "Data/*"

    //Spark configuration
    val conf = new SparkConf().setAppName(s"NBExample").setMaster("local[*]").set("spark.driver.memory", "4g").set("spark.executor.memory", "4g")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)


    // Load documents, and prepare them for NB.
    val (input, corpus) = preprocess(sc, trainFolder)

    //Get class name from file name
    input.collect.foreach(f => {
      val location_array = f._1.split("/")
      val class_name = location_array(location_array.length - 1)
    })

    var hm = new HashMap[String, Int]()
    val path ="Data/"
    val files = getListOfFiles(path)
    var lis = List.empty[String]
    files.foreach(f =>{lis :+= f.getName })
    val CATEGORIES = lis
    var index = 0
    CATEGORIES.foreach(f => {
      hm += CATEGORIES(index) -> index
      index += 1
    })
    //val mapping = sc.broadcast(hm)
    val data = input.zip(corpus)
    val featureVector = data.map(f => {
      val location_array = f._1._1.split("/")
      val class_name = location_array(location_array.length - 1)

      new LabeledPoint(hm.get(class_name).get.toDouble, f._2)
    })

    //Training NaiveBayes Classifier
    val model = NaiveBayes.train(featureVector, lambda = 3.0, modelType = "multinomial")

    //Model saved to use it later for classification
    model.save(sc,"Model/NBmodel")
    sc.stop()

  }
  private def preprocess(sc: SparkContext,
                         paths: String): (RDD[(String, String)], RDD[Vector]) = {

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._
    val df = sc.wholeTextFiles(paths,500).map(f => {
      var ff = f._2.replaceAll("[^a-zA-Z\\s:]", " ")
      ff = ff.replaceAll(":", "")
      // println(ff)
      (f._1, nlp.returnLemma(ff))
    }).toDF("location", "docs")

    //NLP and pipeline
    //Tokenizing
    val tokenizer = new RegexTokenizer()
      .setInputCol("docs")
      .setOutputCol("rawTokens")
    //StopWord remover
    val stopWordsRemover = new StopWordsRemover()
      .setInputCol("rawTokens")
      .setOutputCol("tokens")
    //TF-IDF
    val tf = new org.apache.spark.ml.feature.HashingTF()
      .setInputCol("tokens")
      .setOutputCol("features")
    val idf = new org.apache.spark.ml.feature.IDF()
      .setInputCol("features")
      .setOutputCol("idfFeatures")

    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, stopWordsRemover, tf, idf))

    val model = pipeline.fit(df)
    val documents = model.transform(df)
      .select("idfFeatures")
      .rdd
      .map { case Row(features: Vector) => features }

    val input = model.transform(df).select("location", "docs").rdd.map { case Row(location: String, docs: String) => (location, docs) }
    (input, documents)
  }
}