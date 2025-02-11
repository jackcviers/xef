package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import io.circe.Decoder

import java.text.SimpleDateFormat
import java.util.Date

private final case class BreakingNewsAboutCovid(summary: String) derives SerialDescriptor, Decoder

@main def runBreakingNews: Unit =
  conversation {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, summon[ScalaConversation], 3)
    addContext(search.search(s"$currentDate Covid News").get())
    val news = prompt[BreakingNewsAboutCovid](s"Write a paragraph of about 300 words about: $currentDate Covid News")
    println(news.summary)
  }
