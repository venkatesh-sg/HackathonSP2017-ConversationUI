package Servlet

import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest => HSReq, HttpServletResponse => HSResp}
import javax.servlet.ServletException

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{RegexTokenizer, StopWordsRemover}
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.http.client.HttpResponseException
/**
  * Created by Venkatesh on 4/16/2017.
  */
@WebServlet(name="specializationgen", urlPatterns = Array("/specializationgen"))
class specialization extends HttpServlet {

  @throws(classOf[ServletException])
  @throws(classOf[Exception])
  @throws(classOf[HttpResponseException])
  override
  def doPost(req : HSReq, resp : HSResp)  ={
    val e = req.getParameter("TestData")
    println(e);
    val conf = new SparkConf().setAppName(s"NBMODEL").setMaster("local[*]")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)

    val testd=e.split(" ")

    val testFV = getTFIDFVector(sc, testd)

    val model=NaiveBayesModel.load(sc,"Model/NBmodel")

    val result= model.predict(testFV)

    print(result)
    resp.getWriter.write(result.toString())
  }

  def getTFIDFVector(sc: SparkContext, input: Array[String]): RDD[Vector] = {

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._
    val df = sc.parallelize(input.toSeq).toDF("docs")


    val tokenizer = new RegexTokenizer()
      .setInputCol("docs")
      .setOutputCol("rawTokens")
    val stopWordsRemover = new StopWordsRemover()
      .setInputCol("rawTokens")
      .setOutputCol("tokens")

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

    documents
  }

}
