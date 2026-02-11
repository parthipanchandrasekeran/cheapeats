package com.parthipan.cheapeats.data.menu

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

class MenuRepository(private val context: Context) {

    private val menus: List<RestaurantMenu> by lazy { loadMenus() }

    fun findMenu(restaurantName: String, restaurantAddress: String): RestaurantMenu? {
        val normalizedName = normalize(restaurantName)
        val normalizedAddr = normalize(restaurantAddress)

        // Tier 1: Exact normalized name match
        menus.firstOrNull { normalize(it.restaurantName) == normalizedName }?.let { return it }

        // Tier 2: Substring containment (either direction)
        menus.firstOrNull {
            val menuName = normalize(it.restaurantName)
            normalizedName.contains(menuName) || menuName.contains(normalizedName)
        }?.let { return it }

        // Tier 3: Word-overlap scoring with 50% threshold
        val words = normalizedName.split(" ").filter { it.length > 1 }.toSet()
        if (words.isEmpty()) return null

        return menus
            .map { menu ->
                val menuWords = normalize(menu.restaurantName).split(" ").filter { it.length > 1 }.toSet()
                val overlap = words.intersect(menuWords).size
                val score = overlap.toFloat() / maxOf(words.size, menuWords.size)
                menu to score
            }
            .filter { it.second >= 0.5f }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun normalize(s: String): String =
        s.lowercase().replace(Regex("[^a-z0-9 ]"), "").trim().replace(Regex("\\s+"), " ")

    private fun loadMenus(): List<RestaurantMenu> {
        val json = context.assets.open("toronto_restaurant_menus.json")
            .bufferedReader().use { it.readText() }
        val root = JsonParser.parseString(json).asJsonObject
        val restaurants = root.getAsJsonArray("restaurants")
        return restaurants.mapNotNull { element ->
            val obj = element.asJsonObject
            val name = obj.get("restaurant")?.asString ?: return@mapNotNull null
            val address = obj.get("address")?.asString ?: ""
            val website = obj.get("website")?.asString
            val menuObj = obj.getAsJsonObject("menu") ?: return@mapNotNull null

            val categories = parseMenuObject(menuObj)
            if (categories.isEmpty()) return@mapNotNull null

            RestaurantMenu(name, address, website, categories)
        }
    }

    private fun parseMenuObject(menuObj: JsonObject): List<MenuCategory> {
        val categories = mutableListOf<MenuCategory>()

        for ((key, value) in menuObj.entrySet()) {
            when {
                // Skip metadata fields (strings, string arrays, non-item objects)
                value is JsonPrimitive -> continue
                value is JsonArray && value.size() > 0 && value[0] is JsonPrimitive -> continue

                // Flat category: "pizzas": [{name, price}...]
                value is JsonArray -> {
                    val items = parseItemsArray(value)
                    if (items.isNotEmpty()) {
                        categories.add(MenuCategory(formatCategoryName(key), items))
                    }
                }

                // Nested object — could be meal period or category with pricing
                value is JsonObject -> {
                    val obj = value.asJsonObject
                    if (obj.has("items") && obj.get("items") is JsonArray) {
                        // Category with pricing key: {"pricing": "...", "items": [...]}
                        val note = obj.get("pricing")?.let { pricingToNote(it) }
                        val items = parseItemsArray(obj.getAsJsonArray("items"))
                        if (items.isNotEmpty()) {
                            categories.add(MenuCategory(formatCategoryName(key), items, note))
                        }
                    } else if (hasNestedCategories(obj)) {
                        // Meal period: "lunch": {"starters": [...], "mains": [...]}
                        val prefix = formatCategoryName(key)
                        for ((subKey, subValue) in obj.entrySet()) {
                            when {
                                subValue is JsonArray && subValue.size() > 0 && subValue[0] is JsonObject -> {
                                    val items = parseItemsArray(subValue.asJsonArray)
                                    if (items.isNotEmpty()) {
                                        categories.add(
                                            MenuCategory("$prefix - ${formatCategoryName(subKey)}", items)
                                        )
                                    }
                                }
                                subValue is JsonObject && subValue.asJsonObject.has("items") -> {
                                    val inner = subValue.asJsonObject
                                    val note = inner.get("pricing")?.let { pricingToNote(it) }
                                    val items = parseItemsArray(inner.getAsJsonArray("items"))
                                    if (items.isNotEmpty()) {
                                        categories.add(
                                            MenuCategory("$prefix - ${formatCategoryName(subKey)}", items, note)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Metadata object (tasting_menu with just description/price) — try to extract as single item
                        val name = obj.get("description")?.asString ?: formatCategoryName(key)
                        val price = obj.get("price")?.asString ?: ""
                        if (price.isNotEmpty()) {
                            categories.add(
                                MenuCategory(
                                    formatCategoryName(key),
                                    listOf(MenuItem(name, "", price))
                                )
                            )
                        }
                    }
                }
            }
        }
        return categories
    }

    private fun parseItemsArray(array: JsonArray): List<MenuItem> {
        return array.mapNotNull { element ->
            if (element !is JsonObject) return@mapNotNull null
            val obj = element.asJsonObject
            val name = obj.get("name")?.asString ?: return@mapNotNull null
            val description = obj.get("description")?.asString ?: ""
            val price = obj.get("price")?.asString ?: ""
            MenuItem(name, description, price)
        }
    }

    private fun hasNestedCategories(obj: JsonObject): Boolean {
        return obj.entrySet().any { (_, v) ->
            (v is JsonArray && v.size() > 0 && v[0] is JsonObject) ||
            (v is JsonObject && v.asJsonObject.has("items"))
        }
    }

    private fun pricingToNote(element: com.google.gson.JsonElement): String? {
        return when {
            element is JsonPrimitive -> element.asString.takeIf { it.isNotBlank() }
            element is JsonObject -> {
                element.entrySet().joinToString(" | ") { (k, v) ->
                    "${formatCategoryName(k)}: ${v.asString}"
                }
            }
            else -> null
        }
    }

    private fun formatCategoryName(key: String): String =
        key.replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
}
