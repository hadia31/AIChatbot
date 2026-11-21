package com.example.aichatbot

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IntentClassifier(context: Context) {

    private val interpreter: Interpreter
    private val wordIndex: Map<String, Int>
    private val vocabSize: Int
    private val labels: List<String>

    init {
        val bytes = context.assets.open("chat_classifier.tflite").readBytes()
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        buffer.put(bytes)
        buffer.rewind()
        interpreter = Interpreter(buffer)

        val vocab = context.assets.open("vocab.txt").bufferedReader().readLines()
            .map { it.trim() }.filter { it.isNotEmpty() }
        vocabSize = vocab.size
        wordIndex = vocab.withIndex().associate { it.value to it.index }

        labels = context.assets.open("labels.txt").bufferedReader().readLines()
            .map { it.trim() }.filter { it.isNotEmpty() }
    }

    // Returns the predicted category and the model's confidence (0 to 1)
    fun classify(text: String): Pair<String, Float> {
        val input = Array(1) { FloatArray(vocabSize) }
        Regex("[a-z]+").findAll(text.lowercase()).forEach { match ->
            wordIndex[match.value]?.let { input[0][it] = 1f }
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)

        var best = 0
        for (i in output[0].indices) {
            if (output[0][i] > output[0][best]) best = i
        }
        return Pair(labels[best], output[0][best])
    }
}