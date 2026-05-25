# Add project specific ProGuard rules here.
-keep class com.foddy.app.** { *; }
-dontwarn com.foddy.app.**

# Firebase Rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Gemini AI Rules
-keep class com.google.ai.client.generativeai.** { *; }
