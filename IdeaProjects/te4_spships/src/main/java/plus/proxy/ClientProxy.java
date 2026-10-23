package plus.proxy;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import plus.client.camera.Camera;
import plus.client.event.Handler;
import plus.client.event.SpKeyHandler;
import plus.client.entity.LivingBlockRenderer;
import plus.client.invlib.BodyASlotRenderer;
import plus.common.mashine.item.IMultiModel;
import plus.common.network.PacketSyncRenderItem;
import plus.common.network.spship.SpModePacket;
import plus.common.network.entity.EnableSpFromEntity;
import plus.common.spship.entity.LivingBlock;


@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy{
    @Override
    public void preInit(){
        super.preInit();
        network.registerMessage(
                SpModePacket.ClientHandler.class,  SpModePacket.class, 2, Side.CLIENT);
        network.registerMessage(
                EnableSpFromEntity.ClientHandler.class,  EnableSpFromEntity.class, 3, Side.CLIENT);
        network.registerMessage(PacketSyncRenderItem.ClientHandler.class,
                PacketSyncRenderItem.class, 1, Side.CLIENT);

        Camera.initEventHandlers();
        Handler.initEventHandlers();
    }


    @Override
    public void init() {
        super.init();
        new SpKeyHandler(network, "Space","Shoot", Keyboard.KEY_N, false);
        new BodyASlotRenderer(0.72f, 180f,0f,0f,20f,
                0, 0, 0.2f, 52);
    }


    @SubscribeEvent
    public void onRegistryModel(ModelRegistryEvent e) {
        for(Object o:toReg){
            if (o instanceof Item) {
                regItem((Item) o);
            }
        }
    }


    public void regItem(Item item) {
        if(item instanceof IMultiModel){
            String[] names = ((IMultiModel) item).getModels();
            String prefix = item.getRegistryName().toString()+"_";
            for(int i=0;i<names.length;i++){
                ModelResourceLocation mrl = new ModelResourceLocation(prefix + names[i],
                        "inventory");
                ModelBakery.registerItemVariants(item, mrl);
                ModelLoader.setCustomModelResourceLocation(item, i, mrl);
            }
        } else {
            ModelResourceLocation mrl = new ModelResourceLocation(item.getRegistryName(),
                    "inventory");
            ModelBakery.registerItemVariants(item, mrl);
            ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
        }
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