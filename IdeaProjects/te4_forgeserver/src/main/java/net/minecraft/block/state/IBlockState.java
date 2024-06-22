package net.minecraft.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import plus.fserver.api.ElcaBlockState;


public interface IBlockState extends IBlockBehaviors, IBlockProperties, ElcaBlockState {
    Collection < IProperty<? >> getPropertyKeys();

    <T extends Comparable<T>> T getValue(IProperty<T> property);

    <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value);

    <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property);

    ImmutableMap < IProperty<?>, Comparable<? >> getProperties();

    Block getBlock();

    default IBlockState getClean(){return this;}

    boolean hasTile();
}