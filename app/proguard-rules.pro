# Add project specific ProGuard rules here.

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson model classes
-keepclassmembers class com.lockedin.feature.aichat.** { *; }
-keepclassmembers class com.lockedin.feature.tools.dictionary.** { *; }

# Keep Room entities
-keep class com.lockedin.data.db.entity.** { *; }
