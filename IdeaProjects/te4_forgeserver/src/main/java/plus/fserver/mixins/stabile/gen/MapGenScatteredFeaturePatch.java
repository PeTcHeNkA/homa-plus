package plus.fserver.mixins.stabile.gen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.util.List;


@Mixin(MapGenScatteredFeature.class)
public abstract class MapGenScatteredFeaturePatch extends MapGenStructure {
    /**
     * @author Te4hno
     * @reason Speedy
     */
    @Overwrite //todo maybe error
    public boolean isSwampHut(BlockPos pos) {
        StructureStart res = structureMap.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if(res != null){
            List<StructureComponent> components = res.getComponents();
            if(!components.isEmpty() && components.get(0) instanceof ComponentScatteredFeaturePieces.SwampHut){
                return res.getBoundingBox().isVecInside(pos);
            }
        }
        return false;
    }
}