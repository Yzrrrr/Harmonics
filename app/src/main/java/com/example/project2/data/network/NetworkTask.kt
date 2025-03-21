package com.example.project2.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkChatItem(
    @SerialName("model") val model: String,
    //@SerialName("created_at") val createdAt: String,
    @SerialName("response") val response: String,
//    @SerialName("done") val done: Boolean,
//    @SerialName("done_reason") val doneReason: String,
//    @SerialName("context") val context: List<Int>,
//    @SerialName("total_duration") val totalDuration: Long,
//    @SerialName("load_duration") val loadDuration: Long,
//    @SerialName("prompt_eval_count") val promptEvalCount: Int,
//    @SerialName("prompt_eval_duration") val promptEvalDuration: Long,
//    @SerialName("eval_count") val evalCount: Int,
//    @SerialName("eval_duration") val evalDuration: Long
)

data class ChatRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean,
    val options: Map<String, Any>
)
