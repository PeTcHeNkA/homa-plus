package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteriaStat;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.inject.VarOnly;


public class StatBase {
    static final ITextComponent EMPTY = new TextComponentString("");
    public final String statId;
    //@SideOnly(Side.CLIENT)
    private ITextComponent statName = EMPTY;

    public boolean isIndependent;
    private final IStatType formatter;
    private final IScoreCriteria objectiveCriteria;
    private Class<? extends IJsonSerializable> serializableClazz;
    private static final NumberFormat numberFormat;
    public static IStatType simpleStatType;
    private static final DecimalFormat decimalFormat;
    public static IStatType timeStatType;
    public static IStatType distanceStatType;
    public static IStatType divideByTen;

    public StatBase(String p_i45307_1_, ITextComponent statName, IStatType p_i45307_3_) {
        this.statId = p_i45307_1_;

        setStatName(statName);
        //this.statName = statName;

        this.formatter = p_i45307_3_;
        this.objectiveCriteria = new ScoreCriteriaStat(this);
        IScoreCriteria.INSTANCES.put(this.objectiveCriteria.getName(), this.objectiveCriteria);
    }


    @VarOnly(var = "SIDE_SERVER", code = true)
    private void setStatName(ITextComponent statName){
        this.statName = statName;
    }


    public StatBase(String p_i45308_1_, ITextComponent p_i45308_2_) {
        this(p_i45308_1_, p_i45308_2_, simpleStatType);
    }


    public StatBase initIndependentStat() {
        this.isIndependent = true;
        return this;
    }


    public StatBase registerStat() {
        if (StatList.ID_TO_STAT_MAP.containsKey(this.statId)) {
            throw new RuntimeException("Duplicate stat id: \"" + StatList.ID_TO_STAT_MAP.get(this.statId).statName + "\" and \"" + this.statName + "\" at id " + this.statId);
        } else {
            StatList.ALL_STATS.add(this);
            StatList.ID_TO_STAT_MAP.put(this.statId, this);
            return this;
        }
    }


    @SideOnly(Side.CLIENT)
    public String format(int p_75968_1_) {
        return this.formatter.format(p_75968_1_);
    }


    public ITextComponent getStatName() {
        ITextComponent copy = this.statName.createCopy();
        copy.getStyle().setColor(TextFormatting.GRAY);
        return copy;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            StatBase otherStat = (StatBase)other;
            return this.statId.equals(otherStat.statId);
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return this.statId.hashCode();
    }


    @Override
    public String toString() {
        return "Stat{id=" + this.statId + ", awardLocallyOnly=" + this.isIndependent + ", formatter=" + this.formatter + ", objectiveCriteria=" + this.objectiveCriteria + '}';
    }


    public IScoreCriteria getCriteria() {
        return this.objectiveCriteria;
    }


    public Class<? extends IJsonSerializable> getSerializableClazz() {
        return this.serializableClazz;
    }


    static {
        numberFormat = NumberFormat.getIntegerInstance(Locale.US);
        simpleStatType = new IStatType() {
            @SideOnly(Side.CLIENT)
            public String format(int p_75843_1_) {
                return StatBase.numberFormat.format((long)p_75843_1_);
            }
        };
        decimalFormat = new DecimalFormat("########0.00");
        timeStatType = new IStatType() {
            @SideOnly(Side.CLIENT)
            public String format(int p_75843_1_) {
                double lvt_2_1_ = (double)p_75843_1_ / 20.0;
                double lvt_4_1_ = lvt_2_1_ / 60.0;
                double lvt_6_1_ = lvt_4_1_ / 60.0;
                double lvt_8_1_ = lvt_6_1_ / 24.0;
                double lvt_10_1_ = lvt_8_1_ / 365.0;
                if (lvt_10_1_ > 0.5) {
                    return StatBase.decimalFormat.format(lvt_10_1_) + " y";
                } else if (lvt_8_1_ > 0.5) {
                    return StatBase.decimalFormat.format(lvt_8_1_) + " d";
                } else if (lvt_6_1_ > 0.5) {
                    return StatBase.decimalFormat.format(lvt_6_1_) + " h";
                } else {
                    return lvt_4_1_ > 0.5 ? StatBase.decimalFormat.format(lvt_4_1_) + " m" : lvt_2_1_ + " s";
                }
            }
        };
        distanceStatType = new IStatType() {
            @SideOnly(Side.CLIENT)
            public String format(int p_75843_1_) {
                double lvt_2_1_ = (double)p_75843_1_ / 100.0;
                double lvt_4_1_ = lvt_2_1_ / 1000.0;
                if (lvt_4_1_ > 0.5) {
                    return StatBase.decimalFormat.format(lvt_4_1_) + " km";
                } else {
                    return lvt_2_1_ > 0.5 ? StatBase.decimalFormat.format(lvt_2_1_) + " m" : p_75843_1_ + " cm";
                }
            }
        };
        divideByTen = new IStatType() {
            @SideOnly(Side.CLIENT)
            public String format(int p_75843_1_) {
                return StatBase.decimalFormat.format((double)p_75843_1_ * 0.1);
            }
        };
    }
}
