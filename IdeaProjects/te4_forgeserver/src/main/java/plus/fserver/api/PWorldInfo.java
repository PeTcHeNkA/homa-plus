package plus.fserver.api;

import plus.fserver.world.generator.ChunkGenSettingsPlus;


public interface PWorldInfo {
    default ChunkGenSettingsPlus getChunkGenSettings(){throw new UnsupportedOperationException();};
}
