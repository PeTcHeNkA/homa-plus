package plus.mobs.frame;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.model.AnimatedGeoModel;


public class MobModel<T extends AbcMob> extends AnimatedGeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;


    public MobModel(String model, String texture, String animation){
        this.model = get(model);
        this.texture = get(texture);
        this.animation = get(animation);
    }


    private static ResourceLocation get(String s){
        return new ResourceLocation(GeckoLib.ModID, s);
    }


    @Override
    public ResourceLocation getModelLocation(T testMob) {
        return model;
    }


    @Override
    public ResourceLocation getTextureLocation(T testMob) {
        return texture;
    }


    @Override
    public ResourceLocation getAnimationFileLocation(T testMob) {
        return animation;
    }
}