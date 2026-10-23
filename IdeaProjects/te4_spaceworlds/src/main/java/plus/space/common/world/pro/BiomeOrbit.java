package plus.space.common.world.pro;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeVoid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.ElcaBiomeRegistrator;
import plus.space.common.IHasId;


public class BiomeOrbit extends BiomeVoid implements IHasId {
    public static BiomeOrbit[] ALL = new BiomeOrbit[0];
    public static BiomeOrbit ORBIT;


    private final int id;
    public BiomeOrbit(int id, String name) {
        super((new BiomeProperties("Planet orbit").setRainDisabled()));
        setRegistryName(name);
        this.id = id;
    }


    public boolean ignorePlayerSpawnSuitability() {
        return false;
    }


    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float currentTemperature) {
        return 0xDDDDDD;
    }


    public static void addToRegBiomes(){
        ORBIT = reg(new BiomeOrbit(201,"planet_orbit"));
    }


    private static <T extends Biome> T reg(T biome){
        int id = ((IHasId) biome).getId();
        ElcaBiomeRegistrator.addToQueue(biome, id);
        BiomeOrbit[] newAll = new BiomeOrbit[ALL.length+1];
        System.arraycopy(ALL, 0, newAll, 0, ALL.length);
        ALL = newAll;
        return biome;
    }


    @Override
    public int getId() {
        return id;
    }
}







