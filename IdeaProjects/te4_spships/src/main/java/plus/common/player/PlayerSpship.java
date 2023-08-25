package plus.common.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.DamageSource;
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

    //default void onSpAttack(DamageSource src, float amount){}

    SpShip getSp();

    void setSpShield(float shield);
    float getSpShield();
    float getSpSpeed();
}