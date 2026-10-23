package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaMobs;
import plus.fserver.utls.NbtUtils;
import plus.fserver.utls.math.MathUtls;


public abstract class EntityWaterMob extends EntityLiving implements IAnimals {
    public EntityWaterMob(World worldIn) {
        super(worldIn);
    }


    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }


    @Override
    public boolean getCanSpawnHere() {
        return true;
    }


    @Override
    public boolean isNotColliding() {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }


    @Override
    public int getTalkInterval() {
        return 120;
    }


    @Override
    protected boolean canDespawn() {
        return true;
    }


    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 1 + this.rand.nextInt(3);
    }


    @Override
    public void onEntityUpdate() {
        int i = this.getAir();
        super.onEntityUpdate();

        if (this.isEntityAlive() && !this.isInWater()) {
            --i;
            this.setAir(i);

            if (this.getAir() == -20) {
                this.setAir(MathUtls.INT_0);
                this.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        } else this.setAir(MathUtls.INT_300);
    }


    @Override
    public boolean isPushedByWater() {
        return false;
    }


    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        if(forSpawnCount && isNoDespawnRequired())return false;
        return type == EnumCreatureType.WATER_CREATURE;
    }


    public int lodLvl(){
        return ElcaMobs.GLOBAL.lodLvl(elcaMinRange);
    }
}