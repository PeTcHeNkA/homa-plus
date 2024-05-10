-keep,allowoptimization class net.minecraft.** { *; }
-keep class net.minecraftforge.** { *; }

-keep,allowoptimization,allowshrinking class net.minecraft.entity.Entity { *; }
-keep,allowoptimization,allowshrinking class net.minecraft.entity.EntityLiving { *; }
-keep,allowoptimization,allowshrinking class net.minecraft.entity.EntityLivingBase { *; }

-keep,allowoptimization,allowshrinking class net.minecraft.pathfinding.WalkNodeProcessor { *; }

-keep class com.** { *; }
-keep class org.** { *; }
-keep,allowoptimization,allowshrinking class plus.tson.** { *; }
-keep class plus.fserver.api.** { *; }
-keep class plus.fserver.block.** { *; }
-keep class plus.fserver.event.** { *; }
-keep class plus.fserver.mixins.** { *; }
-keep class plus.fserver.plugin.** { *; }

-keep,allowoptimization class plus.fserver.utls.collections.ArrayListE6 { *; }
-keep,allowoptimization class plus.fserver.utls.collections.ArrayListEV { *; }
-keep,allowoptimization class plus.fserver.utls.collections.FixedItemList { *; }
-keep,allowoptimization class plus.fserver.utls.WorldConstructor { *; }
-keep,allowoptimization class plus.fserver.utls.McTsonUtils { *; }
-keep,allowoptimization class plus.fserver.utls.TsonThreeRegistry { *; }
-keep class plus.fserver.ElcaMobs { *; }
-keep class plus.fserver.ElcaConfig { *; }

-keepattributes *Annotation*
-ignorewarnings