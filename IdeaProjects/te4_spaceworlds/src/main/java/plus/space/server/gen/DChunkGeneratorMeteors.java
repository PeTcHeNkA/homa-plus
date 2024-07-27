package plus.space.server.gen;

import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import plus.fserver.event.NetherGenInitEvent;
import plus.space.server.SolarWorldItem;


public class DChunkGeneratorMeteors extends DChunkGeneratorOst{
    public DChunkGeneratorMeteors(WorldServer world, SolarWorldItem item) {
        super(world, makeFor(world, item), world.getSeed());
    }


    public static NetherGenInitEvent makeFor(WorldServer world, SolarWorldItem item){
        NetherGenInitEvent event = new NetherGenInitEvent(world, false);
        event.liquidLvl  = -1;
        event.borderTop  = false;
        event.borderDown = false;
        event.maxHeight = 200;
        event.minHeight = 50;

        event.fillState = Blocks.STONE.getDefaultState();
        event.sandState = Blocks.OBSIDIAN.getDefaultState();

        event.perlinNoiseY  = 512*10;
        event.perlinNoiseXZ = 512*10;
        event.depthFactor   = 7;

        event.fillFactor    = 55;
        event.minFillFactor = -75;

//        {
//            event.liquidLvl = 80;
//            event.liquidState = Blocks.WATER.getDefaultState();
//            event.borderTop = false;
//
//            event.maxHeight   = 250;
//            event.perlinNoiseY  = 512*10;
//            event.perlinNoiseXZ = 512*10;
//            event.depthFactor   = 7;
//
//            event.noiseScaleZX  = 5;
//            event.noiseScaleY   = 5;
//            event.fillFactor    = 55;
//            event.minFillFactor = -75;
//            //event.heightAddFactor /= 2;
//        }
        return event;
    }
}
