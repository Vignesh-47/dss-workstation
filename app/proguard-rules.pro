# Add project specific ProGuard rules here.
-dontwarn com.google.crypto.tink.**
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
