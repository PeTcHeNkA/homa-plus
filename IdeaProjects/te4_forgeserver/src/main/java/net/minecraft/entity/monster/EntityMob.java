package net.minecraft.entity.monster;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaMobs;


public abstract class EntityMob extends EntityCreature implements IMob {
    public EntityMob(World worldIn) {
        super(worldIn);
        this.experienceValue = 5;
    }


    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }


    @Override
    public void onLivingUpdateTick() {
        this.updateArmSwingProgress();
        float f = this.getBrightness();

        if (f > 0.5F) this.idleTime += 2;
        super.onLivingUpdateTick();
    }


    //todo maybe ignore this?
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) this.setDead();
    }


    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_HOSTILE_SWIM;
    }


    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_HOSTILE_SPLASH;
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isEntityInvulnerable(source) && super.attackEntityFrom(source, amount);
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_HOSTILE_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_HOSTILE_DEATH;
    }


    @Override
    protected SoundEvent getFallSound(int heightIn) {
        return heightIn > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
    }


    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;

        if (entityIn instanceof EntityLivingBase) {
            f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0 && entityIn instanceof EntityLivingBase) {
                ((EntityLivingBase)entityIn).knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                entityIn.setFire(j * 4);
            }

            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                ItemStack itemstack = this.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, this) && itemstack1.getItem().isShield(itemstack1, entityplayer)) {
                    float f1 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

                    if (this.rand.nextFloat() < f1) {
                        entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                        this.world.setEntityState(entityplayer, (byte)30);
                    }
                }
            }
            this.applyEnchantments(this, entityIn);
        }
        return flag;
    }


    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return 0.5F - this.world.getLightBrightness(pos);
    }


    @Override
    public boolean getCanSpawnHere() {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && this.isValidLightLevel() && super.getCanSpawnHere();
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }


    @Override
    protected boolean canDropLoot() {
        return true;
    }


    public boolean isPreventingPlayerRest(EntityPlayer playerIn) {
        return true;
    }


    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        if(forSpawnCount && isNoDespawnRequired())return false;
        return type == EnumCreatureType.MONSTER;
    }


    protected boolean isValidLightLevel() {
        BlockPos blockpos = world.localTempPos.setPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        if (this.world.getLightFor(EnumSkyBlock.SKY, blockpos) > (this.rand.nextInt() & 31) ) {
            return false;
        } else {
            int i;
            if (this.world.isThundering()) {
                int prev = this.world.getSkylightSubtracted();
                this.world.setSkylightSubtracted(10);
                i = this.world.getLightFromNeighbors(blockpos);
                this.world.setSkylightSubtracted(prev);
            } else
                i = this.world.getLightFromNeighbors(blockpos);
            return i <= this.rand.nextInt(8);
        }
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.MONSTERS.lodLvl(elcaMinRange);
    }
}