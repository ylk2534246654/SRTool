-dontshrink
#指定压缩级别
-optimizationpasses 5
#不跳过非公共的库的类成员
-dontskipnonpubliclibraryclassmembers
#混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-ignorewarnings


# 修改包名
-repackageclass "com.yx.srtool"
# 忽略访问修饰符，配合上一句使用
-allowaccessmodification
# 不要删除源文件名和行号
-keepattributes SourceFile,LineNumberTable

#Fragment不需要在AndroidManifest.xml中注册，需要额外保护下
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

#保留Google原生服务需要的类
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keep public class * extends android.widget.SearchView


-dontwarn ru.noties.markwon.**
-keep class ru.noties.markwon.**{*;}