package plus.fserver.world.generator.schem;

import net.minecraft.world.World;
import plus.fserver.world.generator.settings.SettingsFolder;
import plus.fserver.world.generator.vr.MultiChunkCache;


public interface ITSchem {
    void build(World world, int x, int y, int z, SettingsFolder settingsFolder);
    void placeInGen(MultiChunkCache gen, int x, int y, int z, SettingsFolder settingsFolder);
}