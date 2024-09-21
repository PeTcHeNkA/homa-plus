package plus.fserver.mixins.light;

import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.*;
import net.minecraft.network.play.server.*;
import net.minecraft.world.chunk.*;
import plus.fserver.world.light.api.ILightingEngineProvider;


@Mixin(SPacketChunkData.class)
public abstract class MixinSPacketChunkData {
    @Shadow public abstract boolean isFullChunk();

    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    protected int calculateChunkSize(Chunk chunkIn, boolean flag, int num) {
        ((ILightingEngineProvider)chunkIn).getLightingEngine().processLightUpdates();

        int i = 0;
        ExtendedBlockStorage[] storageArray = chunkIn.getBlockStorageArray();
        int j = 0;

        for (int k = storageArray.length; j < k; ++j) {
            ExtendedBlockStorage extendedblockstorage = storageArray[j];

            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (!this.isFullChunk() || !extendedblockstorage.isEmpty()) && (num & 1 << j) != 0) {
                i = i + extendedblockstorage.getData().getSerializedSize();
                i = i + 2048;

                if (flag) {
                    i += 2048;
                }
            }
        }
        if (this.isFullChunk()) {
            i += chunkIn.getBiomeArray().length;
        }
        return i;
    }
}