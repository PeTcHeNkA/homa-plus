package plus.fserver.api;

import plus.fserver.world.generator.ChunkGenSettingsPlus;


public interface ElcaWorldInfo {
    default ChunkGenSettingsPlus getChunkGenSettings(){throw new UnsupportedOperationException();};
}
