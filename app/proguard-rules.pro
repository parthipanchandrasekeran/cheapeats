# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==================== Gson ====================
# Gson uses generic type information stored in a class file when working with fields.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**

# Keep Gson serialized/deserialized classes
-keep class com.parthipan.cheapeats.data.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ==================== Google Maps ====================
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# ==================== Google Play Services ====================
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ==================== Room ====================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==================== Kotlin Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ==================== Compose ====================
-dontwarn androidx.compose.**

# ==================== Google AI / Generative AI ====================
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ==================== Google Auth ====================
-keep class com.google.auth.** { *; }
-dontwarn com.google.auth.**

# ==================== Keep BuildConfig ====================
-keep class com.parthipan.cheapeats.BuildConfig { *; }

# ==================== Prevent R8 from removing model classes ====================
-keep class com.parthipan.cheapeats.data.Restaurant { *; }
-keep class com.parthipan.cheapeats.data.RankedRestaurant { *; }
-keep class com.parthipan.cheapeats.data.CheapEatsApiResponse { *; }
-keep class com.parthipan.cheapeats.data.RestaurantResult { *; }
-keep class com.parthipan.cheapeats.data.NewPlacesApiResponse { *; }
-keep class com.parthipan.cheapeats.data.NewPlaceResult { *; }
-keep class com.parthipan.cheapeats.data.DisplayName { *; }
-keep class com.parthipan.cheapeats.data.LocationResult { *; }
-keep class com.parthipan.cheapeats.data.NewPlacePhoto { *; }
-keep class com.parthipan.cheapeats.data.OpeningHours { *; }
-keep class com.parthipan.cheapeats.data.CurrentOpeningHours { *; }
