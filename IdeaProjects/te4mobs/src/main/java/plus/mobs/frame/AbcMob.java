package plus.mobs.frame;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.mobs.ai.model.AiDefaultModel;
import plus.mobs.utl.AnimationFolder;
import plus.tson.TsonMap;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import static plus.mobs.utl.TsonMobComp.getOrDef;


public abstract class AbcMob extends EntityMob implements IAnimatable {
    protected static final AiDefaultModel AI_DEFAULT_MODEL = new AiDefaultModel(false);

    @SideOnly(Side.CLIENT)
    protected final AnimationFactory factory = new AnimationFactory(this);


    public interface IEntityApplicator {
        void apply(AbcMob mob);
    }

    public static class MobHitbox {
        public final float width;
        public final float height;
        public final float eyeHeight;

        public MobHitbox(TsonMap map) {
            width     = (float) getOrDef(map, "width"     ,1  );
            height    = (float) getOrDef(map, "height"    ,2  );
            eyeHeight = (float) getOrDef(map, "eye-height",1.7);
        }
    }


    public AbcMob(World worldIn) {
        super(worldIn);
    }


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(
                this, "controller", 50, this::predicate));
    }


    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


    public abstract void setState(byte state);


    @SideOnly(Side.CLIENT)
    public abstract byte getState();


    @SideOnly(Side.CLIENT)
    public abstract AnimationFolder getAnimFolder();


    @SideOnly(Side.CLIENT)
    protected <E extends IAnimatable> PlayState predicate(AnimationEvent<E> e) {
        AnimationController<?> cont = e.getController();
        AnimationBuilder builder = new AnimationBuilder();
        AnimationFolder animFolder = getAnimFolder();
        byte state = getState();
        if(state==-1) {
            if(hurtTime>0){
                builder.addAnimation(animFolder.DAMAGED);
            } else if(isAirBorne){
                builder.addAnimation(animFolder.FLY);
            } else if(isJumping) {
                builder.addAnimation(animFolder.JUMP);
            } else if(isDead){
                builder.addAnimation(animFolder.DEATH);
            } else {
                if (e.getLimbSwingAmount() > -0.15F && e.getLimbSwingAmount() < 0.15F) {
                    builder.addAnimation(animFolder.IDLE, ILoopType.EDefaultLoopTypes.LOOP);
                } else {
                    builder.addAnimation(animFolder.WALK, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        } else if(state==2){
            builder.addAnimation(animFolder.ATTACK);
        }
        cont.setAnimation(builder);
        return PlayState.CONTINUE;
    }
}