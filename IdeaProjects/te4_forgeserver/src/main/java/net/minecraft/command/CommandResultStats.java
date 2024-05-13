package net.minecraft.command;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import plus.fserver.api.Keep;


public class CommandResultStats {
    private static final CommandResultStats.Type[] VALUES = Type.values();
    private static final String[] STRING_RESULT_TYPES;
    private String[] entitiesID;
    private String[] objectives;

    public CommandResultStats() {
        this.entitiesID = STRING_RESULT_TYPES;
        this.objectives = STRING_RESULT_TYPES;
    }


    @Keep
    public void setCommandStatForSender(MinecraftServer p_184932_1_, final ICommandSender p_184932_2_, Type p_184932_3_, int p_184932_4_) {
        String lvt_5_1_ = this.entitiesID[p_184932_3_.getTypeID()];
        if (lvt_5_1_ != null) {
            ICommandSender lvt_6_1_ = new ICommandSender() {
                public String getName() {
                    return p_184932_2_.getName();
                }

                public ITextComponent getDisplayName() {
                    return p_184932_2_.getDisplayName();
                }

                public void sendMessage(ITextComponent p_145747_1_) {
                    p_184932_2_.sendMessage(p_145747_1_);
                }

                public boolean canUseCommand(int p_70003_1_, String p_70003_2_) {
                    return true;
                }

                public BlockPos getPosition() {
                    return p_184932_2_.getPosition();
                }

                public Vec3d getPositionVector() {
                    return p_184932_2_.getPositionVector();
                }

                public World getEntityWorld() {
                    return p_184932_2_.getEntityWorld();
                }

                public Entity getCommandSenderEntity() {
                    return p_184932_2_.getCommandSenderEntity();
                }

                public boolean sendCommandFeedback() {
                    return p_184932_2_.sendCommandFeedback();
                }

                public void setCommandStat(Type p_174794_1_, int p_174794_2_) {
                    p_184932_2_.setCommandStat(p_174794_1_, p_174794_2_);
                }

                public MinecraftServer getServer() {
                    return p_184932_2_.getServer();
                }
            };

            String lvt_7_2_;
            try {
                lvt_7_2_ = CommandBase.getEntityName(p_184932_1_, lvt_6_1_, lvt_5_1_);
            } catch (CommandException var12) {
                return;
            }

            String lvt_8_2_ = this.objectives[p_184932_3_.getTypeID()];
            if (lvt_8_2_ != null) {
                Scoreboard lvt_9_1_ = p_184932_2_.getEntityWorld().getScoreboard();
                ScoreObjective lvt_10_1_ = lvt_9_1_.getObjective(lvt_8_2_);
                if (lvt_10_1_ != null) {
                    if (lvt_9_1_.entityHasObjective(lvt_7_2_, lvt_10_1_)) {
                        Score lvt_11_1_ = lvt_9_1_.getOrCreateScore(lvt_7_2_, lvt_10_1_);
                        lvt_11_1_.setScorePoints(p_184932_4_);
                    }
                }
            }
        }
    }


    @Keep
    public void readStatsFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("CommandStats", 10)) {
            NBTTagCompound tag1 = tag.getCompoundTag("CommandStats");
            Type[] var3 = VALUES;
            int var4 = var3.length;
            String str1, str2;
            for(int var5 = 0; var5 < var4; ++var5) {
                Type type = var3[var5];
                str1 = type.getTypeName() + "Name";
                str2 = type.getTypeName() + "Objective";
                if (tag1.hasKey(str1, 8) && tag1.hasKey(str2, 8)) {
                    String lvt_9_1_ = tag1.getString(str1);
                    String lvt_10_1_ = tag1.getString(str2);
                    setScoreBoardStat(this, type, lvt_9_1_, lvt_10_1_);
                }
            }
        }
    }


    @Keep
    public void writeStatsToNBT(NBTTagCompound tag) {
        NBTTagCompound tag2 = new NBTTagCompound();
        Type[] var3 = CommandResultStats.Type.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Type type = var3[var5];
            String str1 = this.entitiesID[type.getTypeID()];
            String str2 = this.objectives[type.getTypeID()];
            if (str1 != null && str2 != null) {
                tag2.setString(type.getTypeName() + "Name",      str1);
                tag2.setString(type.getTypeName() + "Objective", str2);
            }
        }

        if (!tag2.isEmpty()) {
            tag.setTag("CommandStats", tag2);
        }

    }


    @Keep
    public static void setScoreBoardStat(CommandResultStats stats, Type type,
                                         @Nullable String str1, @Nullable String str2) {
        if (str1 != null && !str1.isEmpty() && str2 != null && !str2.isEmpty()) {
            if (stats.entitiesID == STRING_RESULT_TYPES || stats.objectives == STRING_RESULT_TYPES) {
                stats.entitiesID = new String[VALUES.length];
                stats.objectives = new String[VALUES.length];
            }
            stats.entitiesID[type.getTypeID()] = str1;
            stats.objectives[type.getTypeID()] = str2;
        } else {
            removeScoreBoardStat(stats, type);
        }
    }


    private static void removeScoreBoardStat(CommandResultStats stats, Type type) {
        if (stats.entitiesID != STRING_RESULT_TYPES && stats.objectives != STRING_RESULT_TYPES) {
            stats.entitiesID[type.typeID] = null;
            stats.objectives[type.typeID] = null;
            boolean flag = true;
            Type[] var3 = CommandResultStats.Type.values();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Type type1 = var3[var5];
                if (stats.entitiesID[type1.typeID] != null && stats.objectives[type1.typeID] != null) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                stats.entitiesID = STRING_RESULT_TYPES;
                stats.objectives = STRING_RESULT_TYPES;
            }
        }
    }


    @Keep
    public void addAllStats(CommandResultStats stats) {
        Type[] var2 = CommandResultStats.Type.values();
        int var3 = var2.length;
        Type type;
        for(int var4 = 0; var4 < var3; ++var4) {
            type = var2[var4];
            setScoreBoardStat(this, type, stats.entitiesID[type.getTypeID()], stats.objectives[type.getTypeID()]);
        }
    }

    static {
        STRING_RESULT_TYPES = new String[VALUES.length];
    }


    @Keep
    public enum Type {
        SUCCESS_COUNT(0, "SuccessCount"),
        AFFECTED_BLOCKS(1, "AffectedBlocks"),
        AFFECTED_ENTITIES(2, "AffectedEntities"),
        AFFECTED_ITEMS(3, "AffectedItems"),
        QUERY_RESULT(4, "QueryResult");

        final int typeID;
        final String typeName;

        Type(int num, String str) {
            this.typeID = num;
            this.typeName = str;
        }


        public int getTypeID() {
            return this.typeID;
        }


        public String getTypeName() {
            return this.typeName;
        }


        public static String[] getTypeNames() {
            String[] strings = new String[VALUES.length];
            int lvt11 = 0;
            Type[] var2 = values();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Type type = var2[var4];
                strings[lvt11++] = type.getTypeName();
            }
            return strings;
        }


        @Nullable
        public static Type getTypeByName(String str1) {
            Type[] var1 = values();
            int var2 = var1.length;
            for(int var3 = 0; var3 < var2; ++var3) {
                Type type = var1[var3];
                if (type.getTypeName().equals(str1)) {
                    return type;
                }
            }
            return null;
        }
    }
}