package com.cesoft.rawagent.remote.entity

import com.google.gson.annotations.SerializedName

data class GroqDto(
    @SerializedName("object")
    val ob: String,
    val choices: List<GroqResChoices>,
    val usage: GroqResUsage,
)
data class GroqResChoices(
    val index: Int,
    val message: GroqResMessage,
)
data class GroqResMessage(
    val	role: String,
    val content: String,
    @SerializedName("finish_reason")
    val finishReason: String,
)
data class GroqResUsage(
    val queue_time: Float,
    val prompt_tokens: Int,
    val prompt_time: Float,
    val completion_tokens: Int,
    val completion_time: Float,
    val total_tokens: Int,
    val total_time: Float,
)