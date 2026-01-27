package com.parthipan.cheapeats.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class VertexAiService(private val context: Context) {

    companion object {
        /**
         * System prompt defining the AI assistant's behavior within CheapEats
         */
        private const val SYSTEM_PROMPT = """
You are the AI assistant inside the CheapEats Android app, built for Toronto users.

Your job is to help users quickly decide where and what to eat for under $15 CAD.

Context you should always assume:
- City is Toronto
- Users care about price, distance, and TTC convenience
- The app already provides live restaurant data, distances, and prices
- You must NOT invent or guess missing data

What you should do automatically, without asking questions:
- Understand the user's search intent (for example: "cheap Mexican food")
- Focus on food that is filling, practical, and good value
- Prioritize places that are open now, under $15, and close to TTC stations
- Rank restaurants by best overall value and convenience

For each restaurant shown in the app:
- Pick the best item to order under $15
- Write one short, practical line explaining why this place is a good choice
- Add a brief TTC-related note if the walk is convenient
- Keep explanations short, human, and Toronto-local
- Do not use hype, emojis, or marketing language
"""
    }

    private val projectId = "project-90e82d65-ab87-4f3a-83c"
    private val location = "us-central1"
    private val modelId = "gemini-1.5-flash-001"
    private val imagenModelId = "imagen-3.0-generate-001"

    private val credentials: GoogleCredentials by lazy {
        context.assets.open("project-90e82d65-ab87-4f3a-83c-20574965fed9.json").use { stream ->
            GoogleCredentials.fromStream(stream)
                .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val endpoint: String
        get() = "https://$location-aiplatform.googleapis.com/v1/projects/$projectId/locations/$location/publishers/google/models/$modelId:generateContent"

    private val imagenEndpoint: String
        get() = "https://$location-aiplatform.googleapis.com/v1/projects/$projectId/locations/$location/publishers/google/models/$imagenModelId:predict"

    private fun getAccessToken(): String {
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val requestBody = VertexRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    topK = 40,
                    topP = 0.95f,
                    maxOutputTokens = 1024
                )
            )

            val jsonBody = gson.toJson(requestBody)
            val token = getAccessToken()

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code} - $responseBody")
            }

            val vertexResponse = gson.fromJson(responseBody, VertexResponse::class.java)
            vertexResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getSearchSuggestions(
        query: String,
        restaurants: List<Restaurant>
    ): List<Restaurant> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext restaurants

        try {
            val restaurantInfo = restaurants.joinToString("\n") { r ->
                "${r.id}|${r.name}|${r.cuisine}|${r.priceLevel}|${r.rating}|${String.format("%.1f", r.distance)}|${r.nearTTC}"
            }

            val prompt = """
                $SYSTEM_PROMPT

                TASK: Given the user's search query and a list of restaurants,
                return the IDs of the most relevant restaurants in order of relevance.

                User query: "$query"

                Available restaurants (format: id|name|cuisine|priceLevel|rating|distance_mi|nearTTC):
                $restaurantInfo

                Return ONLY a comma-separated list of restaurant IDs that match the query, ordered by relevance.
                Consider:
                - Name matches
                - Cuisine type matches
                - Price level ($ = 1 = under $15, $$ = 2, $$$ = 3)
                - TTC proximity for convenience
                - Semantic understanding (e.g., "cheap" means low price, "quick" means nearby)

                If no restaurants match, return "NONE".
                Response format: id1,id2,id3 (no spaces, no explanations)
            """.trimIndent()

            val resultText = generateContent(prompt).trim()

            if (resultText == "NONE" || resultText.isBlank()) {
                return@withContext emptyList()
            }

            val matchedIds = resultText.split(",").map { it.trim() }
            val restaurantMap = restaurants.associateBy { it.id }

            matchedIds.mapNotNull { id -> restaurantMap[id] }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to basic search on error
            restaurants.filter { restaurant ->
                restaurant.name.contains(query, ignoreCase = true) ||
                        restaurant.cuisine.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun getRestaurantRecommendation(
        preferences: String,
        restaurants: List<Restaurant>
    ): String = withContext(Dispatchers.IO) {
        try {
            val restaurantInfo = restaurants.joinToString("\n") { r ->
                val ttcNote = if (r.nearTTC) {
                    r.nearestStation?.let { ", near $it station" } ?: ", near TTC"
                } else ""
                val priceNote = r.averagePrice?.let { " (~$${it.toInt()})" } ?: ""
                "- ${r.name}: ${r.cuisine}$priceNote, ${r.rating} stars, ${String.format("%.1f", r.distance)} mi$ttcNote"
            }

            val prompt = """
                $SYSTEM_PROMPT

                User is looking for: "$preferences"

                Available restaurants:
                $restaurantInfo

                Give a brief, practical recommendation (2-3 sentences max). Focus on value and convenience.
                Mention TTC accessibility if relevant. No hype or marketing language.
            """.trimIndent()

            generateContent(prompt).trim()
        } catch (e: Exception) {
            e.printStackTrace()
            "I'm having trouble connecting right now. Try searching by cuisine or price!"
        }
    }

    /**
     * Generate a logo image using Vertex AI Imagen model
     * @param prompt Description of the logo to generate
     * @return File path of the saved logo image, or null if generation failed
     */
    suspend fun generateLogo(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("VertexAI", "Generating logo with prompt: $prompt")

            val requestBody = """
                {
                    "instances": [
                        {
                            "prompt": "$prompt"
                        }
                    ],
                    "parameters": {
                        "sampleCount": 1,
                        "aspectRatio": "1:1",
                        "safetySetting": "block_some",
                        "personGeneration": "dont_allow"
                    }
                }
            """.trimIndent()

            val token = getAccessToken()

            val request = Request.Builder()
                .url(imagenEndpoint)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d("VertexAI", "Imagen response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e("VertexAI", "Imagen API failed: ${response.code} - $responseBody")
                return@withContext null
            }

            // Parse response to get base64 image
            val imagenResponse = gson.fromJson(responseBody, ImagenResponse::class.java)
            val base64Image = imagenResponse.predictions?.firstOrNull()?.bytesBase64Encoded

            if (base64Image == null) {
                Log.e("VertexAI", "No image in response")
                return@withContext null
            }

            // Decode base64 and save to file
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // Save to app's files directory
            val logoFile = File(context.filesDir, "cheapeats_logo.png")
            FileOutputStream(logoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            Log.d("VertexAI", "Logo saved to: ${logoFile.absolutePath}")
            logoFile.absolutePath
        } catch (e: Exception) {
            Log.e("VertexAI", "Error generating logo", e)
            e.printStackTrace()
            null
        }
    }
}

// Request/Response data classes
data class VertexRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

data class Content(
    val role: String,
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val maxOutputTokens: Int
)

data class VertexResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ResponseContent?
)

data class ResponseContent(
    val parts: List<Part>?
)

// Imagen API response classes
data class ImagenResponse(
    val predictions: List<ImagenPrediction>?
)

data class ImagenPrediction(
    val bytesBase64Encoded: String?,
    val mimeType: String?
)
