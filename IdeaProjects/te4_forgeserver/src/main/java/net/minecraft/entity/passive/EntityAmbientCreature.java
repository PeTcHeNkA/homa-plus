package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import plus.fserver.ElcaMobs;


public abstract class EntityAmbientCreature extends EntityLiving implements IAnimals {

    public EntityAmbientCreature(World worldIn) {
        super(worldIn);
    }


    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }


    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        if(forSpawnCount && isNoDespawnRequired())return false;
        return type == EnumCreatureType.AMBIENT;
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.AMBIENT.lodLvl(elcaMinRange);
    }
}