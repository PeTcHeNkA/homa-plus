package plus.gen.schem;

import net.minecraft.world.World;
import plus.gen.settings.SettingsFolder;
import java.util.Random;


public interface ITSchem {
    void build(World world, int x, int y, int z, SettingsFolder folder, Random rand);
}