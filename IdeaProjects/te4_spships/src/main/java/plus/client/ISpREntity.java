package plus.client;

import net.minecraft.item.ItemStack;


public interface ISpREntity {
    double getSpXZRot();
    double getSpYRot();

    double getPrevSpXZRot();
    double getPrevSpYRot();

    default void clearSpRot(){};

    boolean isActive();
    void setActive(boolean b);

    void setDCache(double[] cache);
    double[] getDCache();

    float getScale();
    default ItemStack getSpModel(){throw new UnsupportedOperationException();};
    default boolean needRender(){return true;}
}