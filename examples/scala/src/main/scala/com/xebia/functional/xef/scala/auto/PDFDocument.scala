package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.reasoning.pdf.PDF
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import io.circe.Decoder

import scala.io.StdIn.readLine

private final case class AIResponse(answer: String) derives SerialDescriptor, Decoder

val pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

@main def runPDFDocument: Unit =
  conversation {
    val pdf = PDF(OpenAI.FromEnvironment.DEFAULT_CHAT, OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, summon[ScalaConversation])
    addContext(Array(pdf.readPDFFromUrl.readPDFFromUrl(pdfUrl).get()))
    while (true) {
      println("Enter your question: ")
      val line = scala.io.StdIn.readLine()
      val response = prompt[AIResponse](line)
      println(s"${response.answer}\n---\n")
    }
  }
