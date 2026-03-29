# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in the SDK.

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
