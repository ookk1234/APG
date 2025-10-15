-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-ignorewarnings
-dontwarn
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes *Annotation*
-keep public class * extends android.support.design.widget.CoordinatorLayout.Behavior { *; }
-keep public class * extends android.support.design.widget.ViewOffsetBehavior { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepattributes *Annotation*
-keepattributes Signature
-keep @androidx.annotation.Keep class ** { *; } # 如果使用AndroidX的@Keep注解
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepattributes Signature