package plus.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.utls.Builder;

@SideOnly(Side.CLIENT)
public class KeyHandler extends KeyBinding {
    protected final SimpleNetworkWrapper wrapper;
    protected final Builder<IMessage> packetBuilder;

    public KeyHandler(SimpleNetworkWrapper wrapper,
                        Builder<IMessage> packetBuilder, String group, String lore, int i, boolean modify){
        super(lore, i, group);
        if(modify){
            modifyKeyBind(this);
        }
        this.wrapper = wrapper;
        this.packetBuilder = packetBuilder;
        ClientRegistry.registerKeyBinding(this);
        MinecraftForge.EVENT_BUS.register(this);
    }


    private static void modifyKeyBind(KeyHandler openKey){
        for(int i = 0; i< Minecraft.getMinecraft().gameSettings.keyBindings.length; i++){
            KeyBinding defKeyBind = Minecraft.getMinecraft().gameSettings.keyBindings[i];

            if(defKeyBind.getKeyCodeDefault()==openKey.getKeyCodeDefault()){
                defKeyBind.setKeyCode(0);
                Minecraft.getMinecraft().gameSettings.keyBindings[i] = openKey;
                return;
            }
        }
    }


    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if(isPressed()){
            wrapper.sendToServer(packetBuilder.build());
        }
    }
}