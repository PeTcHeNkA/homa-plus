package plus.gen.legacy_templates;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import plus.gen.schem.ITSchem;
import plus.gen.settings.SettingsFolder;
import java.util.Random;


public class McTemplate extends VTemplate implements ITSchem {
    public McTemplate(NBTTagCompound nbt){
        read(nbt);
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settings, Random rand) {
        addBlocksToWorld(
                world,
                x,y,z,
                settings.get(rand)
        );
    }
}