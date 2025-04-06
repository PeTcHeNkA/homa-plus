package net.minecraft.entity.passive;

import com.google.common.base.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public abstract class EntityTameable extends EntityAnimal implements IEntityOwnable {
    protected static final DataParameter<Byte> TAMED;
    protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID;
    protected EntityAISit aiSit;

    public EntityTameable(World world) {
        super(world);
        this.setupTamedAI();
    }


    @Override
    public boolean isTeamable() {
        return true;
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TAMED, (byte)0);
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        if (this.getOwnerId() == null) {
            nbt.setString("OwnerUUID", "");
        } else {
            nbt.setString("OwnerUUID", this.getOwnerId().toString());
        }
        if(this.isSitting()) nbt.setBoolean("Sitting", true);
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        String lvt_2_1_;
        if (nbt.hasKey("OwnerUUID", 8)) {
            lvt_2_1_ = nbt.getString("OwnerUUID");
        } else {
            String lvt_3_1_ = nbt.getString("Owner");
            lvt_2_1_ = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), lvt_3_1_);
        }

        if (!lvt_2_1_.isEmpty()) {
            try {
                this.setOwnerId(UUID.fromString(lvt_2_1_));
                this.setTamed(true);
            } catch (Throwable var4) {
                this.setTamed(false);
            }
        }

        if (this.aiSit != null) {
            this.aiSit.setSitting(nbt.getBoolean("Sitting"));
        }

        this.setSitting(nbt.getBoolean("Sitting"));
    }


    @Override
    public boolean canBeLeashedTo(EntityPlayer p_184652_1_) {
        return !this.getLeashed();
    }


    protected void playTameEffect(boolean p_70908_1_) {
        EnumParticleTypes lvt_2_1_ = EnumParticleTypes.HEART;
        if (!p_70908_1_) {
            lvt_2_1_ = EnumParticleTypes.SMOKE_NORMAL;
        }

        for(int lvt_3_1_ = 0; lvt_3_1_ < 7; ++lvt_3_1_) {
            double lvt_4_1_ = this.rand.nextGaussian() * 0.02;
            double lvt_6_1_ = this.rand.nextGaussian() * 0.02;
            double lvt_8_1_ = this.rand.nextGaussian() * 0.02;
            this.world.spawnParticle(lvt_2_1_, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)0.5F + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, lvt_4_1_, lvt_6_1_, lvt_8_1_, new int[0]);
        }

    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte p_70103_1_) {
        if (p_70103_1_ == 7) {
            this.playTameEffect(true);
        } else if (p_70103_1_ == 6) {
            this.playTameEffect(false);
        } else {
            super.handleStatusUpdate(p_70103_1_);
        }
    }


    public boolean isTamed() {
        return (this.dataManager.get(TAMED) & 4) != 0;
    }


    public void setTamed(boolean p_70903_1_) {
        byte lvt_2_1_ = this.dataManager.get(TAMED);
        if (p_70903_1_) {
            this.dataManager.set(TAMED, (byte)(lvt_2_1_ | 4));
        } else {
            this.dataManager.set(TAMED, (byte)(lvt_2_1_ & -5));
        }
        this.setupTamedAI();
    }


    protected void setupTamedAI() {}


    public boolean isSitting() {
        return (this.dataManager.get(TAMED) & 1) != 0;
    }


    public void setSitting(boolean p_70904_1_) {
        byte lvt_2_1_ = this.dataManager.get(TAMED);
        if (p_70904_1_) {
            this.dataManager.set(TAMED, (byte)(lvt_2_1_ | 1));
        } else {
            this.dataManager.set(TAMED, (byte)(lvt_2_1_ & -2));
        }
    }


    @Nullable
    public UUID getOwnerId() {
        return (UUID)((Optional<?>)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
    }


    public void setOwnerId(@Nullable UUID uuid) {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(uuid));
    }


    public void setTamedBy(EntityPlayer player) {
        this.setTamed(true);
        this.setOwnerId(player.getUniqueID());
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP)player, this);
        }
    }


    @Nullable
    public EntityLivingBase getOwner() {
        try {
            UUID ownerId = this.getOwnerId();
            return ownerId == null ? null : this.world.getPlayerEntityByUUID(ownerId);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }


    public boolean isOwner(EntityLivingBase p_152114_1_) {
        return p_152114_1_ == this.getOwner();
    }


    public EntityAISit getAISit() {
        return this.aiSit;
    }


    public boolean shouldAttackEntity(EntityLivingBase p_142018_1_, EntityLivingBase p_142018_2_) {
        return true;
    }


    public Team getTeam() {
        if (this.isTamed()) {
            EntityLivingBase lvt_1_1_ = this.getOwner();
            if (lvt_1_1_ != null) {
                return lvt_1_1_.getTeam();
            }
        }

        return super.getTeam();
    }

    public boolean isOnSameTeam(Entity p_184191_1_) {
        if (this.isTamed()) {
            EntityLivingBase lvt_2_1_ = this.getOwner();
            if (p_184191_1_ == lvt_2_1_) {
                return true;
            }

            if (lvt_2_1_ != null) {
                return lvt_2_1_.isOnSameTeam(p_184191_1_);
            }
        }

        return super.isOnSameTeam(p_184191_1_);
    }

    public void onDeath(DamageSource p_70645_1_) {
        if (!this.world.isRemote && this.world.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
            this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
        }

        super.onDeath(p_70645_1_);
    }

    static {
        TAMED = EntityDataManager.createKey(EntityTameable.class, DataSerializers.BYTE);
        OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityTameable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    }
}
