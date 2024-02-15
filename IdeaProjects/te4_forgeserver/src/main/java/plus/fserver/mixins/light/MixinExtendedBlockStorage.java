package plus.fserver.mixins.light;

import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.*;


@Mixin(ExtendedBlockStorage.class)
public class MixinExtendedBlockStorage {
    @Shadow private NibbleArray skyLight;
    @Shadow private int blockRefCount;
    @Shadow private NibbleArray blockLight;
    private int lightRefCount;
    
    public MixinExtendedBlockStorage() {
        this.lightRefCount = -1;
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public void setSkyLight(final int x, final int y, final int z, final int value) {
        this.skyLight.set(x, y, z, value);
        this.lightRefCount = -1;
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public void setBlockLight(final int x, final int y, final int z, final int value) {
        this.blockLight.set(x, y, z, value);
        this.lightRefCount = -1;
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public void setBlockLight(final NibbleArray array) {
        this.blockLight = array;
        this.lightRefCount = -1;
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public void setSkyLight(final NibbleArray array) {
        this.skyLight = array;
        this.lightRefCount = -1;
    }


    /**
     * @author Phosphor
     * @reason faster light
     */
    @Overwrite
    public boolean isEmpty() {
        if (this.blockRefCount != 0) return false;
        if (this.lightRefCount == -1) {
            if (this.checkLightArrayEqual(this.skyLight, (byte)(-1)) && this.checkLightArrayEqual(this.blockLight, (byte)0))
                this.lightRefCount = 0;
            else
                this.lightRefCount = 1;
        }
        return this.lightRefCount == 0;
    }


    private boolean checkLightArrayEqual(final NibbleArray storage, final byte val) {
        if (storage == null) return true;
        for (final byte b : storage.getData()) if (b != val) return false;
        return true;
    }
}