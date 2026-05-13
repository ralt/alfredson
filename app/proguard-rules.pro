# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class io.ralt.alfredson.**$$serializer { *; }
-keepclassmembers class io.ralt.alfredson.** {
    *** Companion;
}
-keepclasseswithmembers class io.ralt.alfredson.** {
    kotlinx.serialization.KSerializer serializer(...);
}
