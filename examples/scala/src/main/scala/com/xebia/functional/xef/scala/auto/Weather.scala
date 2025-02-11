package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.reasoning.serpapi.Search

private def getQuestionAnswer(question: String)(using conversation: ScalaConversation): String =
  val search: Search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, conversation, 3)
  addContext(search.search("Weather in Cádiz, Spain").get())
  promptMessage(question)

@main def runWeather: Unit =
  val question = "Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?"
  println(conversation(getQuestionAnswer(question)).mkString("\n"))
