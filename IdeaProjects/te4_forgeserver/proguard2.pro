-keep class net.** { *; }
-keep class com.** { *; }
-keep class org.** { *; }
-keep class io.** { *; }

-keep class com.google.common.collect.HashMultimap { *; }
-keep class plus.fserver.ElcaMobs { *; }
-keep class plus.fserver.ElcaConfig { *; }
-keep,allowoptimization,allowshrinking class plus.tson.** { *; }
-keep class plus.fserver.api.** { *; }
-keep class plus.fserver.block.** { *; }
-keep class plus.fserver.event.** { *; }
-keep class plus.fserver.mixins.** { *; }
-keep class plus.fserver.plugin.** { *; }

-keepclasseswithmembers @plus.fserver.api.Keep class *
-keepclassmembers @plus.fserver.api.Keep class *
-keep @plus.fserver.api.Keep interface *

-keepclassmembers class * {
  @plus.fserver.api.Keep <fields>;
  @plus.fserver.api.Keep <methods>;
}
-keep @plus.fserver.api.Keep enum * {
  **[] $VALUES;
  public *;
}

-keepattributes *Annotation*
-ignorewarnings