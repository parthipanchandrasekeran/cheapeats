package com.parthipan.cheapeats.data.menu

data class MenuItem(
    val name: String,
    val description: String,
    val price: String
)

data class MenuCategory(
    val name: String,
    val items: List<MenuItem>,
    val note: String? = null
)

data class RestaurantMenu(
    val restaurantName: String,
    val address: String,
    val website: String?,
    val categories: List<MenuCategory>
)
