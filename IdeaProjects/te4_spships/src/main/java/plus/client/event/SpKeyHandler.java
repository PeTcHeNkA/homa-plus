package plus.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import plus.asyutl.thread.PopThreadedPool;
import plus.client.ISpREntity;
import plus.client.camera.Camera;
import plus.common.network.spship.SpShootPacket;
import plus.utls.MathUtils;

import static plus.utls.MathUtils.wrapDegrees180;
import static plus.utls.MathUtils.wrapDegrees360;

@SideOnly(Side.CLIENT)
public class SpKeyHandler extends KeyHandler {
    private static final GameSettings settings = Minecraft.getMinecraft().gameSettings;
    private final PopThreadedPool<Runnable> keyHelper = new PopThreadedPool<>(1);
    private boolean isPressed = false;

    public SpKeyHandler(SimpleNetworkWrapper wrapper, String group, String lore, int i, boolean modify) {
        super(wrapper, SpKeyHandler::build, group, lore, i, modify);
    }

    private static SpShootPacket build(){
        if(settings.thirdPersonView == 1){
            Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
            ISpREntity ispr = (ISpREntity) entity;
            return new SpShootPacket(
                    wrapDegrees360((float) -ispr.getDCache()[0]),
                    wrapDegrees180(Camera.spshipRotY-90) + 90
            );
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            return new SpShootPacket(
                    player.rotationYawHead, player.rotationPitch
            );
        }
    }


    private void loop(){
        while (isPressed){
            wrapper.sendToServer(packetBuilder.build());
            try {Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            isPressed = isPressed();
        }
    }

    public boolean isPressed(){
        return Keyboard.isKeyDown(getKeyCode());
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if(Camera.isSelfSpshipActive) {
            if (!isPressed) {
                isPressed = isPressed();
            } else if (!keyHelper.isActive()) {
                keyHelper.put(this::loop);
            }
        }
    }
}