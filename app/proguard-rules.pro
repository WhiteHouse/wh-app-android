# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/eddie/dev/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn icepick.**
-keep class **$$Icicle { *; }
-keepnames class * { @icepick.Icicle *;}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-dontwarn java.lang.invoke.*

-useuniqueclassmembernames

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

-dontwarn org.mockito.**
-dontwarn org.junit.**
-dontwarn org.robolectric.**

-keepattributes Signature

-keepattributes *Annotation*

-keep class gov.whitehouse.data.** { *; }

-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }

-keep class gov.whitehouse.core.** { *; }

-keepclasseswithmembers class * { @retrofit.http.* <methods>; }

-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

-dontwarn com.squareup.okhttp.**

-dontwarn okio.**

-dontwarn com.actionbarsherlock.**

-dontwarn retrofit.appengine.UrlFetchClient

-keep class android.support.** { *; }
-keep class android.support.v7.widget.SearchView { *; }
