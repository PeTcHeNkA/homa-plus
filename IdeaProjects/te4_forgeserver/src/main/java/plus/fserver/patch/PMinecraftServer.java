package plus.fserver.patch;

import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.server.IAsyWorldTicker;


public interface PMinecraftServer {
    default ISaveHandler getSaveHandler(){return null;};
    default IAsyWorldTicker ticker(){return null;}
}