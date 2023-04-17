package plus.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import plus.client.event.SpKeyHandler;
import plus.client.entity.LivingBlockRenderer;
import plus.common.network.spship.SpModePacket;
import plus.common.network.entity.EnableSpFromEntity;
import plus.common.spship.entity.LivingBlock;


public class ClientProxy extends ServerProxy{
    @Override
    public void preInit(){
        super.preInit();
        network.registerMessage(
                SpModePacket.ClientHandler.class,  SpModePacket.class, 2, Side.CLIENT);
        network.registerMessage(
                EnableSpFromEntity.ClientHandler.class,  EnableSpFromEntity.class, 3, Side.CLIENT);
    }


    @Override
    public void init() {
        super.init();
        new SpKeyHandler(network, "Space","Shoot", Keyboard.KEY_N, false);
    }


    @Override
    public void regItem(Item item) {
        super.regItem(item);
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().
                register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }


    @Override
    public void postInit() {
        super.postInit();
    }


    @Override
    public void registryEntity(RegistryEvent.Register<EntityEntry> event){
        super.registryEntity(event);
        RenderingRegistry.registerEntityRenderingHandler(
                LivingBlock.class, LivingBlockRenderer::new
        );
    }


    public void runIfClient(Runnable r){
        r.run();
    }
}