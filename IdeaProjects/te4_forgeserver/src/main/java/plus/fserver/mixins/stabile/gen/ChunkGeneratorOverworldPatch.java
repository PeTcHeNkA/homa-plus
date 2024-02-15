package plus.fserver.mixins.stabile.gen;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.List;


@Mixin(ChunkGeneratorOverworld.class)
public class ChunkGeneratorOverworldPatch {
    @Shadow @Final private World world;
    @Shadow @Final private boolean mapFeaturesEnabled;
    @Shadow private MapGenScatteredFeature scatteredFeatureGenerator;
    @Shadow private ChunkGeneratorSettings settings;
    @Shadow private StructureOceanMonument oceanMonumentGenerator;

    /**
     * @author Te4hno
     * @reason Speedy
     */
    @Overwrite
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (this.mapFeaturesEnabled && creatureType == EnumCreatureType.MONSTER) {
            if (this.scatteredFeatureGenerator.isSwampHut(pos)) {
                return this.scatteredFeatureGenerator.getMonsters();
            }

            if (this.settings.useMonuments &&
                    this.oceanMonumentGenerator.isPositionInStructure(this.world, pos)) {
                return this.oceanMonumentGenerator.getMonsters();
            }
        }
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }
}