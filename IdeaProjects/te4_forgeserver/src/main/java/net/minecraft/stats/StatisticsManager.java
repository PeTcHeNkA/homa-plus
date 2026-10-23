package net.minecraft.stats;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.TupleIntJsonSerializable;


public class StatisticsManager extends ConcurrentHashMap<StatBase, TupleIntJsonSerializable> {
    public StatisticsManager() {}


    public void increaseStat(EntityPlayer p_150871_1_, StatBase p_150871_2_, int p_150871_3_) {
        this.unlockAchievement(p_150871_1_, p_150871_2_, this.readStat(p_150871_2_) + p_150871_3_);
    }


    public void unlockAchievement(EntityPlayer p_150873_1_, StatBase p_150873_2_, int p_150873_3_) {
        TupleIntJsonSerializable lvt_4_1_ = super.get(p_150873_2_);
        if (lvt_4_1_ == null) {
            lvt_4_1_ = new TupleIntJsonSerializable();
            super.put(p_150873_2_, lvt_4_1_);
        }
        lvt_4_1_.setIntegerValue(p_150873_3_);
    }


    public int readStat(StatBase p_77444_1_) {
        TupleIntJsonSerializable lvt_2_1_ = super.get(p_77444_1_);
        return lvt_2_1_ == null ? 0 : lvt_2_1_.getIntegerValue();
    }
}