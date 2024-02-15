package plus.fserver.world.generator.struct;

import net.minecraft.block.Block;


public interface IWorldStruct extends IStruct{
    boolean canPlaceOn(Block check);
    boolean isUnderground();
}