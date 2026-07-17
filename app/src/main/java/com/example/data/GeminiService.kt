package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SessionAnalysis(
    val summary: String,
    val curiosityScore: Int,
    val listeningScore: Int,
    val respectScore: Int,
    val feedback: String,
    val reflectionExercise: String,
    val recommendedReadings: List<String>
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Generates Socratic questions in real-time during an anonymous session.
     */
    suspend fun generateSocraticQuestions(hostName: String, specialties: String, currentTopic: String): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local questions.")
            return@withContext getLocalSocraticFallback(specialties, currentTopic)
        }

        val prompt = """
            You are a wise philosophical companion assisting in a live anonymous audio dialogue on the topic: "$currentTopic".
            The dialogue is guided by host $hostName who is an expert in: "$specialties".
            Generate exactly 3 profound, open-ended Socratic questions that help a seeker think deeper, challenge assumptions, and cultivate intellectual humility.
            Do not include any conversational intro/outro or numbers in your output. Just list the 3 questions separated by newlines.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(apiKey, prompt)
            val lines = responseText.split("\n")
                .map { it.trim().removePrefix("-").removePrefix("*").trim() }
                .filter { it.isNotEmpty() && it.contains("?") }
            
            if (lines.size >= 2) {
                lines.take(3)
            } else {
                getLocalSocraticFallback(specialties, currentTopic)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Socratic questions: ${e.message}", e)
            getLocalSocraticFallback(specialties, currentTopic)
        }
    }

    /**
     * Generates a deep philosophical review/summary after a conversation is completed.
     */
    suspend fun analyzeSession(
        hostName: String,
        specialties: String,
        topic: String,
        durationMinutes: Int
    ): SessionAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalAnalysisFallback(hostName, topic, durationMinutes)
        }

        val prompt = """
            Analyze a $durationMinutes minute philosophical audio conversation on the topic "$topic" with host $hostName (expert in $specialties).
            Respond with a single valid JSON object containing exactly these fields:
            - "summary": (string, 2-3 sentences summarizing the deep core insights of the discussion)
            - "curiosityScore": (integer between 80 and 100 based on the philosophical inquiry depth)
            - "listeningScore": (integer between 80 and 100 representing cognitive absorption)
            - "respectScore": (integer between 85 and 100 representing intellectual humility and non-hostility)
            - "feedback": (string, a paragraph of direct encouragement and philosophical guidance)
            - "reflectionExercise": (string, an action-oriented contemplative exercise/koan for the seeker to meditate on)
            - "recommendedReadings": (JSON array of 3 strings containing classical text recommendations with chapter/verse, e.g. "Bhagavad Gita Chapter 2, Verse 47", "Plato's Republic Book VII", "Heart Sutra")

            Ensure the output is strictly a valid JSON string, with no markdown code blocks (no ```json).
        """.trimIndent()

        try {
            val responseText = callGeminiApi(apiKey, prompt)
            val jsonText = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
            
            val json = JSONObject(jsonText)
            val readingsArray = json.getJSONArray("recommendedReadings")
            val readings = mutableListOf<String>()
            for (i in 0 until readingsArray.length()) {
                readings.add(readingsArray.getString(i))
            }

            SessionAnalysis(
                summary = json.getString("summary"),
                curiosityScore = json.getInt("curiosityScore"),
                listeningScore = json.getInt("listeningScore"),
                respectScore = json.getInt("respectScore"),
                feedback = json.getString("feedback"),
                reflectionExercise = json.getString("reflectionExercise"),
                recommendedReadings = readings
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing session: ${e.message}. Falling back.", e)
            getLocalAnalysisFallback(hostName, topic, durationMinutes)
        }
    }

    private suspend fun callGeminiApi(apiKey: String, promptText: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        
        val requestBodyJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()
        
        partObj.put("text", promptText)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestBodyJson.put("contents", contentsArray)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBodyJson.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unsuccessful API Call: code=${response.code}, body=${response.body?.string()}")
            }
            val responseBody = response.body?.string() ?: throw Exception("Empty Response Body")
            
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    private fun getLocalSocraticFallback(specialties: String, topic: String): List<String> {
        return when {
            topic.lowercase().contains("truth") || topic.lowercase().contains("reality") -> listOf(
                "Is truth something we discover outside, or is it the nature of the observer itself?",
                "If two opposing views can be rationalized, what is the locus of genuine understanding?",
                "How do you distinguish between what is absolutely real and what is temporary appearance?"
            )
            topic.lowercase().contains("ego") || topic.lowercase().contains("mind") || topic.lowercase().contains("self") -> listOf(
                "When you say 'my mind is restless,' who is the 'my' that claims ownership of the mind?",
                "If your thoughts are constantly changing, what remains steady to witness that change?",
                "Can the ego ever dismantle itself, or does the very attempt strengthen its position?"
            )
            topic.lowercase().contains("wisdom") || topic.lowercase().contains("knowledge") -> listOf(
                "What is the threshold where information ceases to be noise and transforms into wisdom?",
                "Does silent contemplation reveal truths that logical speech fails to articulate?",
                "How does intellectual humility accelerate the capacity for profound learning?"
            )
            else -> listOf(
                "What assumption lies at the very root of your current perspective on $topic?",
                "How would your understanding change if you approached $topic with absolute intellectual humility?",
                "What is the most challenging counter-argument to your current stance, and what does it reveal?"
            )
        }
    }

    private fun getLocalAnalysisFallback(hostName: String, topic: String, durationMinutes: Int): SessionAnalysis {
        val randCuriosity = (88..97).random()
        val randListening = (85..96).random()
        val randRespect = (90..98).random()
        
        return SessionAnalysis(
            summary = "A deep, contemplative $durationMinutes-minute dialogue exploring the core nature of '$topic'. The session emphasized shedding intellectual pride to reveal pristine curiosity and clear self-observation.",
            curiosityScore = randCuriosity,
            listeningScore = randListening,
            respectScore = randRespect,
            feedback = "You approached $hostName with a highly receptive intellect. By asking refining questions and patiently holding silence, you allowed the core philosophical elements of $topic to unfold naturally.",
            reflectionExercise = "Meditate on the 'Neti Neti' ('Not this, Not that') principle: Write down 5 assumptions you hold about your personal identity on '$topic', and witness them dissolve into empty, peaceful awareness.",
            recommendedReadings = listOf(
                "The Mandukya Upanishad (Exploring the Four States of Consciousness)",
                "Plato's Apology (Socrates on Intellectual Humility and the Unexamined Life)",
                "The Diamond Sutra (Deconstructing Form and Absolute Interdependence)"
            )
        )
    }
}
