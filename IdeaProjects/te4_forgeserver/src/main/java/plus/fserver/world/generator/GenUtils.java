package plus.fserver.world.generator;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import java.util.List;
import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class GenUtils {
    public static List<Biome.SpawnListEntry> getPossibleCreatures(World world, EnumCreatureType creatureType, BlockPos pos) {
        return (!world.isRemote || SERVER_COMPILE)?
                world.localSelector.getBiome(pos).getSpawnableList(creatureType) //safe
                :
                world.getBiome(pos).getSpawnableList(creatureType);
    }
}