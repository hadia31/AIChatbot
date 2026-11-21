package com.example.aichatbot

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView

    // Our own trained TensorFlow Lite model (intent classifier)
    private var classifier: IntentClassifier? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the .tflite model from the assets folder
        try {
            classifier = IntentClassifier(this)
        } catch (e: Exception) {
            classifier = null
        }

        recyclerView = findViewById(R.id.recyclerView)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        adapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage(text, true)
                etMessage.text.clear()

                // Step 1: run on-device inference with our TFLite model
                val intentTag = buildIntentTag(text)

                // Step 2: ask Gemini for the actual answer
                addMessage("Typing...", false)
                val placeholderIndex = messages.size - 1

                getAIReply(text) { reply ->
                    runOnUiThread {
                        messages[placeholderIndex] = Message(intentTag + reply, false)
                        adapter.notifyItemChanged(placeholderIndex)
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    // Uses the TFLite model to find the category of the user's message
    private fun buildIntentTag(text: String): String {
        val model = classifier ?: return ""
        return try {
            val (label, confidence) = model.classify(text)
            val name = label.replaceFirstChar { it.uppercase() }
            val percent = (confidence * 100).toInt()
            "Detected intent: $name ($percent%)\n\n"
        } catch (e: Exception) {
            ""
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Message(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun cleanText(text: String): String {
        return text
            .replace("**", "")
            .replace("### ", "")
            .replace("## ", "")
            .replace("# ", "")
            .replace("* ", "• ")
    }

    private fun getAIReply(prompt: String, attempt: Int = 1, onResult: (String) -> Unit) {
        val json = JSONObject().put(
            "contents", JSONArray().put(
                JSONObject().put(
                    "parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    )
                )
            )
        )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent")
            .addHeader("X-goog-api-key", BuildConfig.GEMINI_API_KEY)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("Network error. Please check your internet connection and try again.")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    if ((response.code == 503 || response.code == 429) && attempt < 3) {
                        Thread.sleep(2000)
                        getAIReply(prompt, attempt + 1, onResult)
                    } else if (response.code == 503) {
                        onResult("The AI server is busy right now. Please try again in a moment.")
                    } else if (response.code == 429) {
                        onResult("Too many requests. Please wait a moment and try again.")
                    } else {
                        onResult("Something went wrong (error ${response.code}). Please try again.")
                    }
                    return
                }

                try {
                    val reply = JSONObject(body)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    onResult(cleanText(reply.trim()))
                } catch (e: Exception) {
                    onResult("Could not read the reply. Please try again.")
                }
            }
        })
    }
}