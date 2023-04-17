package plus.common.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import plus.common.spship.SpShip;


public interface PlayerSpship {
    RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
    boolean isSpshipMode();

    int getSpModelId();
    void setSpModelId(int id);

    void setSpHitbox();
    void restoreSpHitbox();

    void shoot(float yaw, float pith);

    void onEnableSp();
    void onDisableSp();

    void setSpMode(boolean mode);

    SpShip getSp();
}