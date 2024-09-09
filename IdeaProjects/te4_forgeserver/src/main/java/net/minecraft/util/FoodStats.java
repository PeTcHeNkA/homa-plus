package net.minecraft.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class FoodStats {
    private byte foodLevel = 20;
    private float foodSaturationLevel = 5.0F;
    private float foodExhaustionLevel;
    private byte foodTimer;

    public FoodStats() {}

    public void addStats(int food, float f) {
        this.foodLevel = (byte) Math.min(food + this.foodLevel, 20);
        this.foodSaturationLevel = Math.min(this.foodSaturationLevel + (float)food * f * 2.0F, this.foodLevel);
    }

    public void addStats(ItemFood food, ItemStack stack) {
        this.addStats(food.getHealAmount(stack), food.getSaturationModifier(stack));
    }


    public void onUpdate(EntityPlayer player) {
        EnumDifficulty lvt_2_1_ = player.world.getDifficulty();
        int prevFoodLevel = this.foodLevel;
        if (this.foodExhaustionLevel > 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else if (lvt_2_1_ != EnumDifficulty.PEACEFUL) {
                this.foodLevel = (byte) Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean lvt_3_1_ = player.world.getGameRules().getBoolean("naturalRegeneration");
        if (lvt_3_1_ && this.foodSaturationLevel > 0.0F && player.shouldHeal() && this.foodLevel >= 20) {
            ++this.foodTimer;
            if (this.foodTimer >= 10) {
                float lvt_4_1_ = Math.min(this.foodSaturationLevel, 6.0F);
                player.heal(lvt_4_1_ / 6.0F);
                this.addExhaustion(lvt_4_1_);
                this.foodTimer = 0;
            }
        } else if (lvt_3_1_ && this.foodLevel >= 18 && player.shouldHeal()) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                player.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                if (player.getHealth() > 10.0F || lvt_2_1_ == EnumDifficulty.HARD || player.getHealth() > 1.0F && lvt_2_1_ == EnumDifficulty.NORMAL) {
                    player.attackEntityFrom(DamageSource.STARVE, 1.0F);
                }

                this.foodTimer = 0;
            }
        } else {
            this.foodTimer = 0;
        }

    }

    public void readNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("foodLevel", 99)) {
            this.foodLevel = (byte) nbt.getInteger("foodLevel");
            this.foodTimer = (byte) nbt.getInteger("foodTickTimer");
            this.foodSaturationLevel = nbt.getFloat("foodSaturationLevel");
            this.foodExhaustionLevel = nbt.getFloat("foodExhaustionLevel");
        }
    }


    public void writeNBT(NBTTagCompound nbt) {
        nbt.setIntegerU("foodLevel", this.foodLevel);
        nbt.setIntegerU("foodTickTimer", this.foodTimer);
        nbt.setFloat("foodSaturationLevel", this.foodSaturationLevel);
        nbt.setFloat("foodExhaustionLevel", this.foodExhaustionLevel);
    }


    public int getFoodLevel() {
        return this.foodLevel;
    }


    public boolean needFood() {
        return this.foodLevel < 20;
    }


    public void addExhaustion(float p_75113_1_) {
        this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + p_75113_1_, 40.0F);
    }


    public float getSaturationLevel() {
        return this.foodSaturationLevel;
    }


    public void setFoodLevel(int lvl) {
        this.foodLevel = (byte) Math.min(foodLevel, lvl);
    }


    @SideOnly(Side.CLIENT)
    public void setFoodSaturationLevel(float f1) {
        this.foodSaturationLevel = f1;
    }
}