-keep class net.** { *; }
-keep class com.** { *; }
-keep class org.** { *; }
-keep class plus.tson.** { *; }
-keep class plus.fserver.api.** { *; }
-keep class plus.fserver.event.** { *; }
-keep class plus.fserver.mixins.** { *; }
-keep class plus.fserver.plugin.** { *; }

-keep class plus.fserver.utls.collections.ArrayListE6 { *; }
-keep class plus.fserver.utls.collections.ArrayListEV { *; }

-keep class plus.fserver.server.WorldConstructor { *; }

-keepattributes *Annotation*
-ignorewarnings