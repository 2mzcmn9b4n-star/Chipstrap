# ProGuard rules for Chipstrap

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.chipstrap.rbx.**$$serializer { *; }
-keepclassmembers class com.chipstrap.rbx.** {
    *** Companion;
}
-keepclasseswithmembers class com.chipstrap.rbx.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
