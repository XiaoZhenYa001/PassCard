# 保留应用程序入口点
-keep class com.passwordmanager.MainActivity { *; }

# 保留所有 Model 类
-keep class com.passwordmanager.model.** { *; }

# 保留所有 ViewModel 类
-keep class com.passwordmanager.viewmodel.** { *; }

# 保留所有 Room 数据库相关类
-keep class com.passwordmanager.data.** { *; }

# 保留所有自定义 View 类
-keep class com.passwordmanager.ui.** { *; }

# 保留所有工具类
-keep class com.passwordmanager.utils.** { *; }

# 保留 Android 四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# 保留 Parcelable 序列化的类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable 序列化的类
-keep class * implements java.io.Serializable { *; }

# 保留 R 文件中的所有类及其内部类
-keep class **.R$* { *; }

# 保留 Glide 相关
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# 保留 Room 数据库相关
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.TypeConverter

# AndroidX 相关
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }

# 保留注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses

# 保留 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留自定义控件
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# 保留基本组件的构造方法
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留 Fragment
-keep public class * extends androidx.fragment.app.Fragment

# 保留 RecyclerView 相关
-keep public class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ItemDecoration
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder

# 保留所有实现了 Adapter 的类
-keep public class * extends androidx.recyclerview.widget.RecyclerView$Adapter
-keep public class * extends android.widget.BaseAdapter
-keep public class * extends android.widget.CursorAdapter
-keep public class * extends android.widget.ArrayAdapter

# 保留数据绑定相关
-keep class androidx.databinding.** { *; }
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留 Lambda 表达式
-dontwarn java.lang.invoke.**
-dontwarn **$$Lambda$*

# 移除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 不混淆 WebView 的 JavaScript 接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 不混淆 Serializable 接口的子类中指定的某些成员变量和方法
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}