package plus.fserver.mixins.light;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import plus.fserver.world.light.LightingEngine;
import plus.fserver.world.light.api.ILightingEngineProvider;


@Mixin(World.class)
public abstract class MixinWorld implements ILightingEngineProvider, IBlockAccess {
    @Shadow public abstract Chunk getChunk(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);
    private LightingEngine lightingEngine;
    
    @Inject(method = {"<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/storage/ISaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/profiler/Profiler;Z)V"}, at = { @At("RETURN") })
    private void onConstructed(final CallbackInfo ci) {
        this.lightingEngine = new LightingEngine((World)(Object)this);
    }

    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos){
        this.lightingEngine.scheduleLightUpdate(lightType, pos);
        return true;
    }


    @Override
    public LightingEngine getLightingEngine() {
        return this.lightingEngine;
    }
}