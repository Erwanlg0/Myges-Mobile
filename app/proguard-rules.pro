-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.work.ListenableWorker
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

-keep class com.elg.myges.adapters.secondary.storage.** { *; }
-keep class com.elg.myges.domain.model.** { *; }
-keep class com.elg.myges.adapters.secondary.notification.StudentSyncWorker { *; }
-keep interface com.elg.myges.adapters.secondary.api.MyGesApiService { *; }

-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
