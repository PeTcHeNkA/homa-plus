package plus.space.common.proxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.lwjgl.input.Keyboard;
import plus.fserver.utls.WorldConstructor;
import plus.space.SpaceWorldsMod;
import plus.space.client.StarTextureRepo;
import plus.space.client.event.KeyHandler;
import plus.space.client.gui.GuiStarMap;
import plus.space.common.world.pro.EOrbitProvider;
import plus.space.server.SolarMapServerProvider;
import plus.space.server.SolarRepository;
import plus.space.server.gen.CustomThreeGen;
import plus.space.server.biome.TsonBiomeServer;
import plus.space.server.world.TempWorld;
import java.util.Random;


public class ServerProxy extends CommonProxy {
    private final CustomThreeGen cThreeGen = new CustomThreeGen();


    @Override
    public void preInit(boolean addClient) {
        EOrbitProvider.registerIfNeed();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(SpaceWorldsMod.MOD_ID);
        if(addClient){
            new KeyHandler(GuiStarMap::open, "spworlds", "gui", Keyboard.KEY_N);
            StarTextureRepo.init();
            plus.space.client.net.SolarMapClientProvider.init(network, 1);
            plus.space.client.SolarMap.init();
        }

        SolarMapServerProvider.init(network, 1);

        WorldConstructor.register(102100, TempWorld::new, EOrbitProvider.ORBIT);
    }


    @Override
    public void onServerStart(FMLServerStartingEvent event) {
        SolarRepository.init(event.getServer());
    }


    @Override
    public boolean genThreePatch(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        TsonBiomeServer biome;
        Biome biome0 = worldIn.getBiome(pos);
        if(biome0 instanceof TsonBiomeServer){
            biome = (TsonBiomeServer) biome0;
            cThreeGen.generateTree(worldIn, biome, pos, state, rand);
            return true;
        } else {
            return false;
        }
    }
}