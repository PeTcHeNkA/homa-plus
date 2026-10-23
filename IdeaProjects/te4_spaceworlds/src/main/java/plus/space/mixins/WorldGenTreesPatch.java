package plus.space.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import plus.space.common.world.biome.IWorldGenThreesPatch;


@Mixin(WorldGenTrees.class)
public class WorldGenTreesPatch implements IWorldGenThreesPatch {
    @Shadow @Final private IBlockState metaWood;
    @Shadow @Final private IBlockState metaLeaves;

    @Override
    public IBlockState getWood(){
        return metaWood;
    }


    @Override
    public IBlockState getLeaves(){
        return metaLeaves;
    }
}