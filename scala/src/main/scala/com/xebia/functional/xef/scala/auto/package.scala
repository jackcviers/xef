package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.auto.llm.openai.*
import com.xebia.functional.xef.auto.{FromJson, JVMConversation, PromptConfiguration}
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.llm.models.functions.{CFunction, Json}
import com.xebia.functional.xef.llm.models.images.*
import com.xebia.functional.xef.vectorstores.{ConversationId, LocalVectorStore, VectorStore}
import io.circe.Decoder
import io.circe.parser.parse
import org.reactivestreams.{Subscriber, Subscription}

import java.util
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import scala.jdk.CollectionConverters.*

class ScalaConversation(store: VectorStore, conversationId: Option[ConversationId]) extends JVMConversation(store, conversationId.orNull)

def addContext(context: Array[String])(using conversation: ScalaConversation): Unit =
  conversation.addContextFromArray(context).join()

def prompt[A: Decoder: SerialDescriptor](
    prompt: String,
    chat: ChatWithFunctions = OpenAI.FromEnvironment.DEFAULT_SERIALIZATION,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using
    conversation: ScalaConversation
): A =
  val fromJson = new FromJson[A] {
    def fromJson(json: String): A =
      parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity)
  }
  conversation.prompt(chat, prompt, generateCFunctions.asJava, fromJson, promptConfiguration).join()

def promptMessage(
    question: String,
    chat: Chat = OpenAI.FromEnvironment.DEFAULT_CHAT,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using conversation: ScalaConversation): String =
  conversation.promptMessage(chat, question, promptConfiguration).join()

def promptMessages(
    question: String,
    chat: Chat = OpenAI.FromEnvironment.DEFAULT_CHAT,
    functions: List[CFunction] = List(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using
    conversation: ScalaConversation
): List[String] =
  conversation.promptMessages(chat, question, functions.asJava, promptConfiguration).join().asScala.toList

def promptStreaming(
    question: String,
    chat: Chat = OpenAI.FromEnvironment.DEFAULT_CHAT,
    functions: List[CFunction],
    promptConfiguration: PromptConfiguration
)(using
    conversation: ScalaConversation
): LazyList[String] =
  val publisher = conversation.promptStreaming(chat, question, promptConfiguration, functions.asJava)
  val queue = new LinkedBlockingQueue[String]()
  publisher.subscribe(new Subscriber[String] {
    // TODO change to fs2 or similar
    def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)

    def onNext(t: String): Unit = queue.add(t); ()

    def onError(t: Throwable): Unit = throw t

    def onComplete(): Unit = ()
  })
  LazyList.continually(queue.take)

def images(
    prompt: String,
    images: Images = OpenAI.FromEnvironment.DEFAULT_IMAGES,
    numberImages: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using
    conversation: ScalaConversation
): ImagesGenerationResponse =
  conversation.images(images, prompt, numberImages, size, promptConfiguration).join()

def conversation[A](
    block: ScalaConversation ?=> A,
    conversationId: Option[ConversationId] = Some(ConversationId(UUID.randomUUID().toString))
): A = block(using ScalaConversation(LocalVectorStore(OpenAIEmbeddings(OpenAI.FromEnvironment.DEFAULT_EMBEDDING)), conversationId))

private def generateCFunctions[A: SerialDescriptor]: List[CFunction] =
  val descriptor = SerialDescriptor[A].serialDescriptor
  val serialName = descriptor.getSerialName
  val fnName =
    if (serialName.contains(".")) serialName.substring(serialName.lastIndexOf("."), serialName.length)
    else serialName
  List(CFunction(fnName, "Generated function for $fnName", Json.encodeJsonSchema(descriptor)))
