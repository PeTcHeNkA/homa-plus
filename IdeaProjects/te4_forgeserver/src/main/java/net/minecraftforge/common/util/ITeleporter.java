package net.minecraftforge.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;


public interface ITeleporter {
    void placeEntity(World var1, Entity var2, float var3);


    default boolean onPreTransfer(EntityPlayerMP player, int dimId){return false;}


    default void onPostTransfer(EntityPlayerMP player, int dimId){}


    default boolean onRemoveOnTransfer(EntityPlayerMP player, int oldId, int dimId){return false;}


    default boolean isVanilla() {
        return this.getClass() == Teleporter.class;
    }
}