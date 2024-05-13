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
-keep class net.minecraft.server.MinecraftServer { *; }
-keep class net.minecraft.server.ServerEula { *; }
-keep class net.minecraft.enchantment.EnumEnchantmentType { *; }
-keep class net.minecraft.block.BlockLog$EnumAxis { *; }
-keep class net.minecraft.block.BlockLeaves { *; }
-keep class net.minecraft.block.material.Material { *; }

-keep class net.minecraft.item.Item { *; }
-keep class net.minecraft.item.ItemArmor { *; }
-keep,allowoptimization class net.minecraft.entity.item.EntityFallingBlock { *; }
-keep class net.minecraft.util.math.BlockPos { *; }
-keep class net.minecraft.block.Block { *; }

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
-verbose
-optimizationpasses 3
-optimizations !class/marking/final,!class/unboxing/enum,!method/marking/private,!method/marking/final,!field/removal/writeonly,!code/removal/advanced