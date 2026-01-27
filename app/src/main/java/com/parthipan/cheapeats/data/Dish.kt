package com.parthipan.cheapeats.data

/**
 * Represents a dish/menu item from a restaurant.
 */
data class Dish(
    val id: String,
    val name: String,
    val description: String,
    val price: Float,
    val category: String,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val imageUrl: String? = null
) {
    val formattedPrice: String
        get() = "$${String.format("%.2f", price)}"

    val isUnder15: Boolean
        get() = price < 15f
}

/**
 * Sample dishes for demo purposes.
 * In production, these would come from an API.
 */
object SampleDishes {

    private val timHortonsDishes = listOf(
        Dish("th1", "Double Double Coffee", "Medium roast coffee with 2 cream, 2 sugar", 2.29f, "Beverages",
            imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=200"),
        Dish("th2", "Boston Cream Donut", "Chocolate-topped donut with vanilla filling", 1.99f, "Donuts",
            imageUrl = "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=200"),
        Dish("th3", "Bacon Breakfast Sandwich", "Bacon, egg & cheese on a biscuit", 5.49f, "Breakfast",
            imageUrl = "https://images.unsplash.com/photo-1550507992-eb63ffee0847?w=200"),
        Dish("th4", "Chicken Wrap", "Grilled chicken with fresh veggies", 7.99f, "Lunch",
            imageUrl = "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=200"),
        Dish("th5", "Timbits (10 pack)", "Assorted bite-sized donuts", 3.99f, "Donuts"),
        Dish("th6", "Farmers Wrap", "Bacon, egg, cheese with hash brown", 6.49f, "Breakfast"),
        Dish("th7", "Chili", "Hearty beef chili", 4.99f, "Lunch"),
        Dish("th8", "Iced Capp", "Frozen coffee drink", 3.99f, "Beverages",
            imageUrl = "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=200")
    )

    private val mcdDishes = listOf(
        Dish("mc1", "Big Mac", "Two beef patties, special sauce, lettuce, cheese", 7.49f, "Burgers",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200"),
        Dish("mc2", "McChicken", "Crispy chicken patty with mayo and lettuce", 6.99f, "Chicken",
            imageUrl = "https://images.unsplash.com/photo-1606755962773-d324e0a13086?w=200"),
        Dish("mc3", "Medium Fries", "Golden crispy fries", 4.29f, "Sides",
            imageUrl = "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=200"),
        Dish("mc4", "6pc McNuggets", "Crispy chicken nuggets", 6.49f, "Chicken",
            imageUrl = "https://images.unsplash.com/photo-1562967914-608f82629710?w=200"),
        Dish("mc5", "McDouble", "Two beef patties with cheese", 4.49f, "Burgers"),
        Dish("mc6", "Junior Chicken", "Crispy chicken sandwich", 3.49f, "Chicken"),
        Dish("mc7", "Apple Pie", "Warm apple pie", 1.99f, "Desserts"),
        Dish("mc8", "McFlurry", "Soft serve with toppings", 4.99f, "Desserts",
            imageUrl = "https://images.unsplash.com/photo-1579954115545-a95591f28bfc?w=200"),
        Dish("mc9", "Quarter Pounder", "Quarter pound beef with cheese", 8.99f, "Burgers"),
        Dish("mc10", "Filet-O-Fish", "Fish fillet with tartar sauce", 6.99f, "Fish")
    )

    private val subwayDishes = listOf(
        Dish("sw1", "6\" Turkey Breast", "Turkey breast sub with veggies", 8.99f, "Subs",
            imageUrl = "https://images.unsplash.com/photo-1539252554453-80ab65ce3586?w=200"),
        Dish("sw2", "6\" Veggie Delite", "Fresh vegetables on bread", 6.49f, "Subs", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1554433607-66b5efe9d304?w=200"),
        Dish("sw3", "6\" Meatball Marinara", "Italian meatballs with marinara sauce", 7.99f, "Subs",
            imageUrl = "https://images.unsplash.com/photo-1513442542250-854d436a73f2?w=200"),
        Dish("sw4", "6\" Chicken Teriyaki", "Sweet teriyaki glazed chicken", 9.49f, "Subs"),
        Dish("sw5", "Cookie", "Freshly baked cookie", 1.29f, "Desserts",
            imageUrl = "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=200"),
        Dish("sw6", "Chips", "Baked or regular chips", 1.99f, "Sides"),
        Dish("sw7", "6\" BLT", "Bacon, lettuce, tomato sub", 7.49f, "Subs"),
        Dish("sw8", "6\" Italian BMT", "Pepperoni, salami, ham", 8.49f, "Subs")
    )

    private val tacoBellDishes = listOf(
        Dish("tb1", "Crunchy Taco", "Seasoned beef in crunchy shell", 2.99f, "Tacos",
            imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=200"),
        Dish("tb2", "Soft Taco", "Seasoned beef in soft tortilla", 2.99f, "Tacos",
            imageUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=200"),
        Dish("tb3", "Bean Burrito", "Refried beans with cheese", 2.49f, "Burritos", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=200"),
        Dish("tb4", "Crunchwrap Supreme", "Beef, cheese, sour cream wrapped", 6.99f, "Specialty"),
        Dish("tb5", "Nachos BellGrande", "Chips with beef, cheese, beans", 7.49f, "Sides",
            imageUrl = "https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?w=200"),
        Dish("tb6", "Quesadilla", "Grilled cheese quesadilla", 5.49f, "Specialty",
            imageUrl = "https://images.unsplash.com/photo-1618040996337-56904b7850b9?w=200"),
        Dish("tb7", "Chalupa Supreme", "Fried shell with beef and toppings", 5.99f, "Specialty"),
        Dish("tb8", "Cinnamon Twists", "Sweet cinnamon pastry", 1.99f, "Desserts")
    )

    private val pizzaPizzaDishes = listOf(
        Dish("pp1", "Pepperoni Slice", "Classic pepperoni pizza slice", 4.99f, "Pizza",
            imageUrl = "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=200"),
        Dish("pp2", "Cheese Slice", "Classic cheese pizza slice", 3.99f, "Pizza", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=200"),
        Dish("pp3", "Hawaiian Slice", "Ham and pineapple", 4.99f, "Pizza",
            imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200"),
        Dish("pp4", "Veggie Slice", "Assorted vegetables", 4.99f, "Pizza", isVegetarian = true),
        Dish("pp5", "Garlic Bread", "Toasted garlic bread", 3.49f, "Sides",
            imageUrl = "https://images.unsplash.com/photo-1619535860434-ba1d8fa12536?w=200"),
        Dish("pp6", "Caesar Salad", "Romaine with caesar dressing", 5.99f, "Salads",
            imageUrl = "https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=200"),
        Dish("pp7", "Wings (6pc)", "Crispy chicken wings", 8.99f, "Sides"),
        Dish("pp8", "Medium Pepperoni Pizza", "12\" pepperoni pizza", 14.99f, "Pizza"),
        Dish("pp9", "Dipping Sauce", "Garlic or marinara sauce", 0.99f, "Sides")
    )

    private val phoRestaurantDishes = listOf(
        Dish("ph1", "Pho Tai", "Beef pho with rare steak", 13.99f, "Pho",
            imageUrl = "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=200"),
        Dish("ph2", "Pho Dac Biet", "Special combo pho", 14.99f, "Pho",
            imageUrl = "https://images.unsplash.com/photo-1503764654157-72d979d9af2f?w=200"),
        Dish("ph3", "Spring Rolls (3pc)", "Fresh Vietnamese spring rolls", 6.99f, "Appetizers",
            imageUrl = "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=200"),
        Dish("ph4", "Banh Mi", "Vietnamese sandwich", 8.99f, "Sandwiches",
            imageUrl = "https://images.unsplash.com/photo-1600688640154-9619e002df30?w=200"),
        Dish("ph5", "Bun Bo Hue", "Spicy beef noodle soup", 13.99f, "Noodles", isSpicy = true),
        Dish("ph6", "Vermicelli Bowl", "Rice noodles with grilled meat", 12.99f, "Rice & Noodles",
            imageUrl = "https://images.unsplash.com/photo-1569058242567-93de6f36f8e6?w=200"),
        Dish("ph7", "Iced Vietnamese Coffee", "Strong coffee with condensed milk", 4.99f, "Beverages",
            imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=200"),
        Dish("ph8", "Chicken Pho", "Rice noodles in chicken broth", 12.99f, "Pho")
    )

    private val sushiRestaurantDishes = listOf(
        Dish("su1", "California Roll (8pc)", "Crab, avocado, cucumber", 8.99f, "Rolls",
            imageUrl = "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=200"),
        Dish("su2", "Salmon Nigiri (2pc)", "Fresh salmon over rice", 5.99f, "Nigiri",
            imageUrl = "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=200"),
        Dish("su3", "Spicy Tuna Roll", "Spicy tuna with cucumber", 9.99f, "Rolls", isSpicy = true,
            imageUrl = "https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=200"),
        Dish("su4", "Miso Soup", "Traditional miso soup", 2.99f, "Soup",
            imageUrl = "https://images.unsplash.com/photo-1607301406259-dfb186e15de8?w=200"),
        Dish("su5", "Edamame", "Steamed soybeans", 4.99f, "Appetizers", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1564894809611-1742fc40ed80?w=200"),
        Dish("su6", "Dragon Roll", "Eel and avocado roll", 14.99f, "Specialty Rolls"),
        Dish("su7", "Gyoza (6pc)", "Pan-fried dumplings", 7.99f, "Appetizers",
            imageUrl = "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=200"),
        Dish("su8", "Bento Box", "Assorted sushi with sides", 16.99f, "Combos"),
        Dish("su9", "Vegetable Roll", "Assorted veggie roll", 6.99f, "Rolls", isVegetarian = true)
    )

    private val indianRestaurantDishes = listOf(
        Dish("in1", "Butter Chicken", "Creamy tomato curry with chicken", 14.99f, "Curry",
            imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=200"),
        Dish("in2", "Vegetable Samosa (2pc)", "Crispy pastry with spiced potatoes", 4.99f, "Appetizers", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=200"),
        Dish("in3", "Naan Bread", "Traditional Indian flatbread", 2.99f, "Breads", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=200"),
        Dish("in4", "Dal Tadka", "Yellow lentils with spices", 10.99f, "Vegetarian", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=200"),
        Dish("in5", "Chicken Tikka", "Grilled marinated chicken", 12.99f, "Tandoori",
            imageUrl = "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=200"),
        Dish("in6", "Mango Lassi", "Sweet mango yogurt drink", 4.99f, "Beverages", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=200"),
        Dish("in7", "Pakora", "Vegetable fritters", 5.99f, "Appetizers", isVegetarian = true),
        Dish("in8", "Biryani", "Spiced rice with meat", 13.99f, "Rice Dishes",
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=200")
    )

    private val genericDishes = listOf(
        Dish("gn1", "House Salad", "Fresh mixed greens", 7.99f, "Salads", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=200"),
        Dish("gn2", "Soup of the Day", "Chef's daily soup", 5.99f, "Soup",
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=200"),
        Dish("gn3", "Grilled Chicken Sandwich", "Chicken breast with veggies", 11.99f, "Sandwiches",
            imageUrl = "https://images.unsplash.com/photo-1553909489-cd47e0907980?w=200"),
        Dish("gn4", "Fish & Chips", "Battered fish with fries", 13.99f, "Mains",
            imageUrl = "https://images.unsplash.com/photo-1579208030886-b1a5ed34fff6?w=200"),
        Dish("gn5", "Veggie Burger", "Plant-based patty", 12.99f, "Burgers", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1520072959219-c595dc870360?w=200"),
        Dish("gn6", "Caesar Salad", "Romaine, croutons, parmesan", 9.99f, "Salads"),
        Dish("gn7", "Chicken Wings", "Crispy wings with sauce", 11.99f, "Appetizers",
            imageUrl = "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=200"),
        Dish("gn8", "French Fries", "Crispy golden fries", 4.99f, "Sides", isVegetarian = true,
            imageUrl = "https://images.unsplash.com/photo-1576107232684-1279f390859f?w=200"),
        Dish("gn9", "Onion Rings", "Battered onion rings", 5.99f, "Sides", isVegetarian = true),
        Dish("gn10", "Soft Drink", "Coke, Sprite, or Fanta", 2.49f, "Beverages")
    )

    /**
     * Get dishes for a restaurant based on its name/cuisine.
     * Returns dishes filtered to those under $15 CAD.
     */
    fun getDishesForRestaurant(restaurantName: String, cuisine: String): List<Dish> {
        val allDishes = when {
            restaurantName.contains("Tim Hortons", ignoreCase = true) -> timHortonsDishes
            restaurantName.contains("McDonald", ignoreCase = true) -> mcdDishes
            restaurantName.contains("Subway", ignoreCase = true) -> subwayDishes
            restaurantName.contains("Taco Bell", ignoreCase = true) -> tacoBellDishes
            restaurantName.contains("Pizza Pizza", ignoreCase = true) -> pizzaPizzaDishes
            cuisine.contains("Vietnamese", ignoreCase = true) ||
                restaurantName.contains("Pho", ignoreCase = true) -> phoRestaurantDishes
            cuisine.contains("Japanese", ignoreCase = true) ||
                cuisine.contains("Sushi", ignoreCase = true) -> sushiRestaurantDishes
            cuisine.contains("Indian", ignoreCase = true) -> indianRestaurantDishes
            else -> genericDishes
        }
        return allDishes
    }

    /**
     * Get only dishes under $15 CAD.
     */
    fun getDishesUnder15(restaurantName: String, cuisine: String): List<Dish> {
        return getDishesForRestaurant(restaurantName, cuisine).filter { it.isUnder15 }
    }
}
