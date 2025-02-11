package com.xebia.functional.xef.vectorstores.postgresql

import kotlinx.uuid.UUID

data class PGCollection(val uuid: UUID, val collectionName: String)

enum class PGDistanceStrategy(val strategy: String) {
  Euclidean("<->"),
  InnerProduct("<#>"),
  CosineDistance("<=>")
}

val createCollections: String =
  """CREATE TABLE IF NOT EXISTS xef_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );"""
    .trimIndent()

val createMemoryTable: String =
  """CREATE TABLE IF NOT EXISTS xef_memory (
       uuid TEXT PRIMARY KEY,
       conversation_id TEXT NOT NULL,
       role TEXT NOT NULL,
       content TEXT UNIQUE NOT NULL,
       timestamp BIGINT NOT NULL,
       approx_tokens INT NOT NULL
     );"""
    .trimIndent()

val createEmbeddings: String =
  """CREATE TABLE xef_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES xef_collections(uuid),
       embedding BLOB,
       content TEXT
     );"""
    .trimIndent()

val addVectorExtension: String = "CREATE EXTENSION IF NOT EXISTS vector;"

val createCollectionsTable: String =
  """CREATE TABLE IF NOT EXISTS xef_collections (
       uuid TEXT PRIMARY KEY,
       name TEXT UNIQUE NOT NULL
     );"""
    .trimIndent()

fun createEmbeddingTable(vectorSize: Int): String =
  """CREATE TABLE IF NOT EXISTS xef_embeddings (
       uuid TEXT PRIMARY KEY,
       collection_id TEXT REFERENCES xef_collections(uuid),
       embedding vector($vectorSize),
       content TEXT
     );"""
    .trimIndent()


/*
uuid TEXT PRIMARY KEY,
       conversation_id TEXT NOT NULL,
       role TEXT NOT NULL,
       content TEXT UNIQUE NOT NULL,
       timestamp TIMESTAMP NOT NULL,
 */
val addNewMemory: String =
  """INSERT INTO xef_memory(uuid, conversation_id, role, content, timestamp)
     VALUES (?, ?, ?, ?, ?)
     ON CONFLICT DO NOTHING;"""
    .trimIndent()

val addNewCollection: String =
  """INSERT INTO xef_collections(uuid, name)
     VALUES (?, ?)
     ON CONFLICT DO NOTHING;"""
    .trimIndent()

val deleteCollection: String =
  """DELETE FROM xef_collections
     WHERE uuid = ?;"""
    .trimIndent()

val deleteMemory: String =
  """DELETE FROM xef_memory
     WHERE uuid = ?;"""
    .trimIndent()

val getCollection: String =
  """SELECT * FROM xef_collections
     WHERE name = ?;"""
    .trimIndent()

val getCollectionById: String =
  """SELECT * FROM xef_collections
     WHERE uuid = ?;"""
    .trimIndent()

val getMemoriesByConversationId: String =
    """WITH ConversationTokens AS (
    SELECT
        uuid,
        conversation_id,
        role,
        content,
        timestamp,
        approx_tokens,
        SUM(approx_tokens) OVER (PARTITION BY conversation_id ORDER BY timestamp DESC) AS running_tokens
    FROM
        xef_memory
    WHERE
        conversation_id = ?
    )
    SELECT
        uuid,
        conversation_id,
        role,
        content,
        timestamp,
        approx_tokens
    FROM
        ConversationTokens
    WHERE
        running_tokens <= ?
    ORDER BY
        timestamp DESC;
    """.trimIndent()

val addNewDocument: String =
  """INSERT INTO xef_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?, ?);"""
    .trimIndent()

val deleteCollectionDocs: String =
  """DELETE FROM xef_embeddings
     WHERE collection_id = ?;"""
    .trimIndent()

val addNewText: String =
  """INSERT INTO xef_embeddings(uuid, collection_id, embedding, content)
     VALUES (?, ?, ?::vector, ?);"""
    .trimIndent()

fun searchSimilarDocument(distance: PGDistanceStrategy): String =
  """SELECT content FROM xef_embeddings
     WHERE collection_id = ?
     ORDER BY embedding
     ${distance.strategy} ?::vector
     LIMIT ?;"""
    .trimIndent()
