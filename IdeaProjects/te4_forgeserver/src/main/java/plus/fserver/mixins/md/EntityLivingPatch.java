package plus.fserver.mixins.md;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import plus.fserver.api.MeanTicker;
import plus.fserver.server.ai.CustomGoalSelector;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(EntityLiving.class)
public abstract class EntityLivingPatch extends EntityLivingBase{
    @Shadow @Final @Mutable public EntityAITasks tasks;
    @Shadow @Final @Mutable public EntityAITasks targetTasks;

    public EntityLivingPatch(World worldIn) {
        super(worldIn);
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(World worldIn, CallbackInfo ci){
        MeanTicker ticker;
        if(worldIn.isRemote){
            ticker = MeanTicker.EMPTY;
        } else {
            ticker = (MeanTicker) worldIn;
        }

        tasks = new CustomGoalSelector(tasks, worldIn.profiler, ticker);
        targetTasks = new CustomGoalSelector(targetTasks, worldIn.profiler, ticker);
    }


    @Override
    public void onDeath(@NotNull DamageSource cause) {
        if (ForgeHooks.onLivingDeath(this, cause)) return;
        if (!this.dead) {
            Entity entity = cause.getTrueSource();
            EntityLivingBase entitylivingbase = this.getAttackingEntity();

            if (this.scoreValue >= 0 && entitylivingbase != null) {
                entitylivingbase.awardKillScore(this, this.scoreValue, cause);
            }

            if (entity != null) {
                entity.onKillEntity(this);
            }

            this.dead = true;
            this.getCombatTracker().reset();

            if (!this.world.isRemote) {
                int i = net.minecraftforge.common.ForgeHooks.getLootingLevel(this, entity, cause);

                captureDrops = true;
                capturedDrops.clear();

                if (this.canDropLoot() && this.world.getGameRules().doMobLoot.getBoolean()) {
                    boolean flag = this.recentlyHit > 0;
                    this.dropLoot(flag, i, cause);
                }
                captureDrops = false;

                if (!ForgeHooks.onLivingDrops(this, cause, capturedDrops, i, recentlyHit > 0)) {
                    for (EntityItem item : capturedDrops) {
                        world.spawnEntity(item);
                    }
                }
            }
            this.world.setEntityState(this, (byte)3);
        }
    }
}