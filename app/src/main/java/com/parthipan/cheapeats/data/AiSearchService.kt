package com.parthipan.cheapeats.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiSearchService(
    private val apiKey: String
) {
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            }
        )
    }

    suspend fun getSearchSuggestions(
        query: String,
        restaurants: List<Restaurant>
    ): List<Restaurant> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext restaurants

        try {
            val restaurantInfo = restaurants.joinToString("\n") { r ->
                "${r.id}|${r.name}|${r.cuisine}|${r.priceLevel}|${r.rating}"
            }

            val prompt = """
                You are a restaurant search assistant. Given the user's search query and a list of restaurants,
                return the IDs of the most relevant restaurants in order of relevance.

                User query: "$query"

                Available restaurants (format: id|name|cuisine|priceLevel|rating):
                $restaurantInfo

                Return ONLY a comma-separated list of restaurant IDs that match the query, ordered by relevance.
                Consider:
                - Name matches
                - Cuisine type matches
                - Price level if mentioned ($ = 1, $$ = 2, $$$ = 3)
                - Semantic understanding (e.g., "cheap" means low price, "highly rated" means high rating)

                If no restaurants match, return "NONE".
                Response format: id1,id2,id3 (no spaces, no explanations)
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val resultText = response.text?.trim() ?: return@withContext emptyList()

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
                "- ${r.name}: ${r.cuisine} cuisine, ${"$".repeat(r.priceLevel)} price, ${r.rating} rating, ${r.distance} mi away"
            }

            val prompt = """
                You are a friendly food recommendation assistant for CheapEats app.

                User preferences: "$preferences"

                Available restaurants:
                $restaurantInfo

                Give a brief, friendly recommendation (2-3 sentences max) for which restaurant(s)
                would best match their preferences. Be conversational and helpful.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: "I couldn't generate a recommendation right now. Try searching for a specific cuisine!"
        } catch (e: Exception) {
            e.printStackTrace()
            "I'm having trouble connecting right now. Try searching by cuisine or price!"
        }
    }
}
