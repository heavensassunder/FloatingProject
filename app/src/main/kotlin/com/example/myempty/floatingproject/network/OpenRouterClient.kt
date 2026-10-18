package com.example.myempty.floatingproject.network

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenRouterClient(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Send a chat completion request. Uses a simple system + user message model.
     * Returns assistant text on success or throws an exception.
     */
    suspend fun chat(systemPrompt: String, userPrompt: String, model: String = "openai/gpt-4o-mini", maxTokens: Int = 200): String {
        return withContext(Dispatchers.IO) {
            val root = JSONObject().apply {
                put("model", model)
                put("max_tokens", maxTokens)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
            }

            val body = root.toString().toRequestBody(JSON)
            val req = Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string() ?: throw Exception("Empty response from OpenRouter")
                if (!resp.isSuccessful) {
                    // include body for easier debugging
                    throw Exception("OpenRouter error ${resp.code}: $text")
                }

                // Expecting structure: { choices: [{ message: { content: "..." }}, ... ] }
                val json = JSONObject(text)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val first = choices.getJSONObject(0)
                    val message = first.optJSONObject("message")
                    val content = message?.optString("content") ?: message?.optString("text") // fallback
                    return@withContext content ?: ""
                } else {
                    // fallback: sometimes response structure varies
                    json.optString("text", "")
                }
            }
        }
    }

    /**
     * If you want to send a screenshot inline as Base64 in a message (MVP simple approach),
     * convert the bitmap externally and pass the base64 as part of userPrompt.
     *
     * For a more correct multimodal request, refer to OpenRouter docs about attachments/multipart.
     */
    fun bitmapToBase64(jpegBytes: ByteArray): String {
        return Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
    }
}
