package plus.space.server.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.api.ElcaWorldProvider;
import plus.space.common.CommonCalculator;
import plus.space.common.GalName;
import plus.space.common.net.CallBackMsg;
import plus.space.common.proxy.CommonProxy;


public abstract class SolarWorld extends BaseWorld implements ISolarWorld {
    private int maxWorldTime;
    public SolarWorld(MinecraftServer server, ISaveHandler handler, int dimId, WorldServer delegate, Profiler prf) {
        super(server, handler, dimId, delegate, prf);
    }


    public void setBiomeProvider(BiomeProvider provider){
        ((ElcaWorldProvider)super.provider).setBiomeProvider(provider);
    }


    @Override
    public boolean spawnEntity(Entity entityIn) {
        if(super.spawnEntity(entityIn)){
            if(entityIn instanceof EntityPlayerMP){
                CommonProxy.network.sendTo(
                        new CallBackMsg(0, 3, getFullName().toString()), (EntityPlayerMP) entityIn
                );
            }
            return true;
        }
        return false;
    }


    @Override
    public GalName getFullName() {
        return item().planet.getFullName();
    }


    //TODO не то
    @Override
    public float getCelestialAngle(float partialTicks) {
        if(maxWorldTime==0){
            maxWorldTime = CommonCalculator.calcWorldDay(item().sunDistance);
        }
        int i = (int)(getWorldTime() % maxWorldTime);
        float f = ((float)i + partialTicks) / maxWorldTime - 0.25F;
        if (f < 0.0F) {
            ++f;
        } else if (f > 1.0F) {
            --f;
        }
        float f1 = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0D) / 2.0D);
        f = f + (f1 - f) / 3.0F;
        return f;
    }


    @Override
    public IChunkGenerator createChunkGenerator(){
        return item().getChunkGenConstructor().newGenerator(this, item());
    }
}