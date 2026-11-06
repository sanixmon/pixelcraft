# Preserve file names and line numbers for readable stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,RuntimeVisibleAnnotations,AnnotationDefault

# Keep Kotlin Metadata required by kotlinx.serialization in R8 full mode
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# kotlinx.serialization core R8 rules (AGP 8+ R8 full mode)
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepnames class <2>$$serializer {
    static <2>$$serializer INSTANCE;
}

-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

-if @kotlinx.serialization.Serializable class **
-keep class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Preserve serialized models, custom font entries, generated serializers, & enum names in PixelCraft snapshots
-keep @kotlinx.serialization.Serializable class com.app.pixelcraft.** { *; }
-keep class com.app.pixelcraft.**$$serializer { *; }
-keep class com.app.pixelcraft.**$Companion { *; }
-keepclassmembers enum com.app.pixelcraft.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep AndroidViewModel(Application) constructor invoked reflectively by Activity.viewModels()
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}
