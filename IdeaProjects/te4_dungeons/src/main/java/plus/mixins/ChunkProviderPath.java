package plus.mixins;


import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collection;


@Mixin(ChunkProviderServer.class)
public class ChunkProviderPath {
    @Final
    @Shadow
    public final Long2ObjectMap<Chunk> loadedChunks = new Long2ObjectOpenHashMap<>(8192);


    @Inject(method = "chunkExists", at = @At("HEAD"), cancellable = true)
    public void chunkExists(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        synchronized (this) {
            try {
                rreturn(cir, loadedChunks.containsKey(ChunkPos.asLong(x, z)));
            } catch (ArrayIndexOutOfBoundsException e) {
                rreturn(cir, false);
            }
        }
    }


    private void rreturn(CallbackInfoReturnable<Boolean> cir, boolean val){
        cir.setReturnValue(val);
    }


    @Shadow
    private void saveChunkExtraData(Chunk chunkIn){}


    @Shadow
    private void saveChunkData(Chunk chunkIn){}


    @Inject(method = "saveChunks", at = @At("HEAD"), cancellable = true)
    public void saveExists(boolean all, CallbackInfoReturnable<Boolean> cir) {
        int i = 0;
        Collection<Chunk> values = loadedChunks.values();
        if(values==null && values.isEmpty()){
            rreturn(cir, false);
        }

        try {
            values = Lists.newArrayList(values);
        } catch (NullPointerException e){
            rreturn(cir, false);
            return;
        }

        for (Chunk chunk : values) {
            if (all) {
                this.saveChunkExtraData(chunk);
            }
            if (chunk.needsSaving(all)) {
                this.saveChunkData(chunk);
                chunk.setModified(false);
                ++i;

                if (i == 24 && !all) {
                    rreturn(cir, false);
                }
            }
        }
        rreturn(cir, false);
    }


    @Inject(method = "getLoadedChunk", at = @At("HEAD"), cancellable = true)
    public void getLoadedChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        synchronized (this) {
            Chunk chunk = this.loadedChunks.get(ChunkPos.asLong(x, z));
            if (chunk != null) {
                chunk.unloadQueued = false;
            }
            cir.setReturnValue(chunk);
        }
    }
}