package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketStatistics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.tson.utl.Te4HashSet;


public class StatisticsManagerServer extends StatisticsManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final File statsFile;
    private final Te4HashSet<StatBase> dirty = new Te4HashSet<>();
    private int lastStatRequest = -300;

    public StatisticsManagerServer(MinecraftServer p_i45306_1_, File p_i45306_2_) {
        //this.server = p_i45306_1_;
        this.statsFile = p_i45306_2_;
    }

    public void readStatFile() {
        if (this.statsFile.isFile()) {
            try {
                super.clear();
                super.putAll(this.parseJson(FileUtils.readFileToString(this.statsFile)));
            } catch (IOException var2) {
                IOException lvt_1_1_ = var2;
                LOGGER.error("Couldn't read statistics file {}", this.statsFile, lvt_1_1_);
            } catch (JsonParseException var3) {
                JsonParseException lvt_1_2_ = var3;
                LOGGER.error("Couldn't parse statistics file {}", this.statsFile, lvt_1_2_);
            }
        }

    }

    public void saveStatFile() {
        try {
            FileUtils.writeStringToFile(this.statsFile, dumpJson(this));
        } catch (IOException var2) {
            IOException lvt_1_1_ = var2;
            LOGGER.error("Couldn't save stats", lvt_1_1_);
        }

    }


    @Override
    public void unlockAchievement(EntityPlayer p_150873_1_, StatBase p_150873_2_, int p_150873_3_) {
        super.unlockAchievement(p_150873_1_, p_150873_2_, p_150873_3_);
        this.dirty.add(p_150873_2_);
    }


    private Set<StatBase> getDirty() {
        Te4HashSet<StatBase> result = new Te4HashSet<>();
        result.addAll(this.dirty);
        this.dirty.clear();
        return result;
    }


    public Map<StatBase, TupleIntJsonSerializable> parseJson(String p_150881_1_) {
        JsonElement lvt_2_1_ = (new JsonParser()).parse(p_150881_1_);
        if (!lvt_2_1_.isJsonObject()) {
            return Maps.newHashMap();
        } else {
            JsonObject lvt_3_1_ = lvt_2_1_.getAsJsonObject();
            Map<StatBase, TupleIntJsonSerializable> lvt_4_1_ = Maps.newHashMap();
            Iterator var5 = lvt_3_1_.entrySet().iterator();

            while(true) {
                while(var5.hasNext()) {
                    Map.Entry<String, JsonElement> lvt_6_1_ = (Map.Entry)var5.next();
                    StatBase lvt_7_1_ = StatList.getOneShotStat((String)lvt_6_1_.getKey());
                    if (lvt_7_1_ != null) {
                        TupleIntJsonSerializable lvt_8_1_ = new TupleIntJsonSerializable();
                        if (((JsonElement)lvt_6_1_.getValue()).isJsonPrimitive() && ((JsonElement)lvt_6_1_.getValue()).getAsJsonPrimitive().isNumber()) {
                            lvt_8_1_.setIntegerValue(((JsonElement)lvt_6_1_.getValue()).getAsInt());
                        } else if (((JsonElement)lvt_6_1_.getValue()).isJsonObject()) {
                            JsonObject lvt_9_1_ = ((JsonElement)lvt_6_1_.getValue()).getAsJsonObject();
                            if (lvt_9_1_.has("value") && lvt_9_1_.get("value").isJsonPrimitive() && lvt_9_1_.get("value").getAsJsonPrimitive().isNumber()) {
                                lvt_8_1_.setIntegerValue(lvt_9_1_.getAsJsonPrimitive("value").getAsInt());
                            }

                            if (lvt_9_1_.has("progress") && lvt_7_1_.getSerializableClazz() != null) {
                                try {
                                    Constructor<? extends IJsonSerializable> lvt_10_1_ = lvt_7_1_.getSerializableClazz().getConstructor();
                                    IJsonSerializable lvt_11_1_ = (IJsonSerializable)lvt_10_1_.newInstance();
                                    lvt_11_1_.fromJson(lvt_9_1_.get("progress"));
                                    lvt_8_1_.setJsonSerializableValue(lvt_11_1_);
                                } catch (Throwable var12) {
                                    Throwable lvt_10_2_ = var12;
                                    LOGGER.warn("Invalid statistic progress in {}", this.statsFile, lvt_10_2_);
                                }
                            }
                        }

                        lvt_4_1_.put(lvt_7_1_, lvt_8_1_);
                    } else {
                        LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.statsFile, lvt_6_1_.getKey());
                    }
                }

                return lvt_4_1_;
            }
        }
    }

    public static String dumpJson(Map<StatBase, TupleIntJsonSerializable> p_150880_0_) {
        JsonObject lvt_1_1_ = new JsonObject();
        Iterator var2 = p_150880_0_.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<StatBase, TupleIntJsonSerializable> lvt_3_1_ = (Map.Entry)var2.next();
            if (((TupleIntJsonSerializable)lvt_3_1_.getValue()).getJsonSerializableValue() != null) {
                JsonObject lvt_4_1_ = new JsonObject();
                lvt_4_1_.addProperty("value", ((TupleIntJsonSerializable)lvt_3_1_.getValue()).getIntegerValue());

                try {
                    lvt_4_1_.add("progress", ((TupleIntJsonSerializable)lvt_3_1_.getValue()).getJsonSerializableValue().getSerializableElement());
                } catch (Throwable var6) {
                    Throwable lvt_5_1_ = var6;
                    LOGGER.warn("Couldn't save statistic {}: error serializing progress", ((StatBase)lvt_3_1_.getKey()).getStatName(), lvt_5_1_);
                }

                lvt_1_1_.add(((StatBase)lvt_3_1_.getKey()).statId, lvt_4_1_);
            } else {
                lvt_1_1_.addProperty(((StatBase)lvt_3_1_.getKey()).statId, ((TupleIntJsonSerializable)lvt_3_1_.getValue()).getIntegerValue());
            }
        }

        return lvt_1_1_.toString();
    }


    public void markAllDirty() {
        this.dirty.addAll(super.keySet());
    }


    public void sendStats(EntityPlayerMP player) {
        MinecraftServer server = player.getServer();

        int counter = server == null?0:server.getTickCounter();
        HashMap<StatBase, Integer> map = new HashMap<>();
        if (counter - this.lastStatRequest > 300) {
            this.lastStatRequest = counter;
            Iterator var4 = this.getDirty().iterator();

            while(var4.hasNext()) {
                StatBase stat = (StatBase)var4.next();
                map.put(stat, this.readStat(stat));
            }
        }

        player.connection.sendPacket(new SPacketStatistics(map));
    }
}
