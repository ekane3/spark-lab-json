
package com.tp.spark.core

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd._
import com.tp.spark.utils._
import com.tp.spark.utils.TweetUtils.Tweet

/**
 *
 *  We still use the dataset with the 8198 reduced tweets. Here an example of a tweet:
 *
 *  {"id":"572692378957430785",
 *    "user":"Srkian_nishu :)",
 *    "text":"@always_nidhi @YouTube no i dnt understand bt i loved of this mve is rocking",
 *    "place":"Orissa",
 *    "country":"India"}
 *
 *  We want to make some computations on the tweets:
 *  - Find all the persons mentioned on tweets
 *  - Count how many times each person is mentioned
 *  - Find the 10 most mentioned persons by descending order
 *
 */
object Ex2TweetMining {

  val pathToFile = "data/reduced-tweets.json"

  /**
   *  Load the data from the json file(data/reduced-tweets.json) and return an RDD of Tweet
   */
  def loadData(): RDD[Tweet] = {
    // create spark configuration and spark context
    val conf = new SparkConf()
        .setAppName("Tweet mining")
        .setMaster("local[*]")

    val sc = SparkContext.getOrCreate(conf)

    // Load the data and parse it into a Tweet.
    // Look at the Tweet Object in the TweetUtils class.
    sc.textFile(pathToFile)
        .mapPartitions(TweetUtils.parseFromJson(_))

  }

  /**
   *  Find all the persons mentioned on tweets (case sensitive, duplicates allowed)
   */
  def mentionOnTweet(): RDD[String] = {
     val tweets = loadData()

    // Filter the tweets and get the mentioned persons
    tweets.flatMap(tweet => tweet.text.split(" ").filter(word => (word.startsWith("@") && !(word.endsWith("@")) ) ) )
  }
  // Find all user mentionned on tweets text (case sensitive, duplicates allowed)
  def mentionOnTweetText(): RDD[String] = {
    val tweets = loadData()

    // Filter the tweets and get the mentioned persons
    tweets.flatMap(tweet => tweet.text.split(" ").filter(word => (word.startsWith("@") && !(word.endsWith("@")) ) ) )
  }

  /**
   *  Count how many times each person is mentioned
   */
  def countMentions(): RDD[(String, Int)] = {
    val mentions = mentionOnTweet()

    // Count the mentions
    mentions.map(mention => (mention, 1)).reduceByKey((x, y) => x + y)
  }

  /**
   *  Find the 10 most mentioned persons by descending order
   */
  def top10mentions(): Array[(String, Int)] = {
    val mentions = countMentions()

    // Sort the mentions in descending order
    mentions.sortBy(_._2, ascending=false).take(10)
  }

}
