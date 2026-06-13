# Add project specific ProGuard rules here.

# Keep Room entities and DAOs from being obfuscated
-keep class com.autobook.app.data.local.entity.** { *; }
-keep class com.autobook.app.data.local.dao.** { *; }

# Room generated implementations
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
