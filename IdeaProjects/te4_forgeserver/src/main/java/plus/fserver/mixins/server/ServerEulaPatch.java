package plus.fserver.mixins.server;

import net.minecraft.server.ServerEula;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.io.File;


@Mixin(ServerEula.class)
public abstract class ServerEulaPatch {
    /**
     * @author Te4hno
     * @reason Auto Accept EULA
     */
    @Overwrite
    private boolean loadEULAFile(File inFile){
        return true;
    }
}