# Media3 ExoPlayer rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit & Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sonora.music.core.network.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Audio FX
-keep class android.media.audiofx.** { *; }

