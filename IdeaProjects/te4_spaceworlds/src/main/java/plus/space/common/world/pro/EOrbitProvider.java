package plus.space.common.world.pro;

import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.OrbitSkyRenderer;


public class EOrbitProvider extends WorldProvider {
    public static DimensionType ORBIT;

    public static void registerIfNeed(){
        if(ORBIT != null)return;
        ORBIT = DimensionType.register("orbit", "orbit", 1001, EOrbitProvider.class, false);
    }


    @Override
    protected void init() {
        this.hasSkyLight = true;
        this.biomeProvider = new BiomeProviderSingle(BiomeOrbit.ORBIT);
    }


    public int getAverageGroundLevel() {
        return 16;
    }


    @SideOnly(Side.CLIENT)
    private OrbitSkyRenderer renderer;
    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getSkyRenderer() {
        if(renderer==null){
            renderer = new OrbitSkyRenderer(Minecraft.getMinecraft());
        }
        return renderer;
    }


    @SideOnly(Side.CLIENT)
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        if(renderer==null) {
            return super.calculateCelestialAngle(worldTime, partialTicks);
        } else {
            return renderer.getWorldCel(worldTime, partialTicks);
        }
    }


    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return 8.0F;
    }


    @Override
    public DimensionType getDimensionType() {
        return ORBIT;
    }


    @Override
    public IChunkGenerator createChunkGenerator() {
        throw new UnsupportedOperationException();
    }
}