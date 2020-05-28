
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-dontwarn android.databinding.**
-printseeds proguard/seeds.txt
-printusage proguard/unused.txt
-printmapping proguard/mapping.txt
#androidx包使用混淆
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

-keep class com.arcsoft.asg.libdeviceadapt.bean.**{*;}
-keep class com.arcsoft.asg.libdeviceadapt.repos.AdaptationInfoDataUtils{*;}
-keep class com.arcsoft.asg.libdeviceadapt.view.fragment.**{*;}
-keep class com.arcsoft.asg.libdeviceadapt.view.navigator.**{*;}
-keep class com.arcsoft.asg.libdeviceadapt.view.seekbar.SettingSeekBar{*;}