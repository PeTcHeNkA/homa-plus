package net.minecraft.world;

import java.util.HashMap;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;


public class GameRules {
    private final HashMap<String, Value> rules = new HashMap<>();

    public final Value doFireTick;
    public final Value mobGriefing;
    public final Value keepInventory;
    public final Value doMobLoot;
    public final Value doMobSpawning;
    public final Value spawnRadius;
    public final Value randomTickSpeed;
    public final Value maxEntityCramming;
    public final Value doTileDrops;
    public final Value doLimitedCrafting;
    public final Value doEntityDrops;
    public final Value doWeatherCycle;
    public final Value doDaylightCycle;

    public GameRules() {
        doFireTick        = this.addGameRule("doFireTick",    "true" ,     GameRules.ValueType.BOOLEAN_VALUE);
        mobGriefing       = this.addGameRule("mobGriefing",   "true" ,     GameRules.ValueType.BOOLEAN_VALUE);
        keepInventory     = this.addGameRule("keepInventory", "false",     GameRules.ValueType.BOOLEAN_VALUE);
        doMobSpawning     = this.addGameRule("doMobSpawning", "true" ,     GameRules.ValueType.BOOLEAN_VALUE);
        doMobLoot         = this.addGameRule("doMobLoot",     "true" ,     GameRules.ValueType.BOOLEAN_VALUE);
        spawnRadius       = this.addGameRule("spawnRadius",   "10"   ,     GameRules.ValueType.NUMERICAL_VALUE);
        randomTickSpeed   = this.addGameRule("randomTickSpeed","3"   ,     GameRules.ValueType.NUMERICAL_VALUE);
        maxEntityCramming = this.addGameRule("maxEntityCramming","24",     GameRules.ValueType.NUMERICAL_VALUE);
        doTileDrops       = this.addGameRule("doTileDrops", "true"   ,     GameRules.ValueType.BOOLEAN_VALUE);
        doLimitedCrafting = this.addGameRule("doLimitedCrafting","false",  GameRules.ValueType.BOOLEAN_VALUE);
        doEntityDrops     = this.addGameRule("doEntityDrops", "true" ,     GameRules.ValueType.BOOLEAN_VALUE);
        doWeatherCycle    = this.addGameRule("doWeatherCycle","true" ,     GameRules.ValueType.BOOLEAN_VALUE);
        doDaylightCycle   = this.addGameRule("doDaylightCycle","true",     GameRules.ValueType.BOOLEAN_VALUE);

        this.addGameRule("commandBlockOutput", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("naturalRegeneration", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("doDaylightCycle", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("logAdminCommands", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("showDeathMessages", "true", GameRules.ValueType.BOOLEAN_VALUE);

        this.addGameRule("sendCommandFeedback", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("reducedDebugInfo", "false", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("spectatorsGenerateChunks", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("disableElytraMovementCheck", "false", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("maxCommandChainLength", "65536", GameRules.ValueType.NUMERICAL_VALUE);
        this.addGameRule("announceAdvancements", "true", GameRules.ValueType.BOOLEAN_VALUE);
        this.addGameRule("gameLoopFunction", "-", GameRules.ValueType.FUNCTION);
    }


    public Value addGameRule(String key, String value, GameRules.ValueType type) {
        Value result = this.rules.get(key);
        if(result == null){
            this.rules.put(key, result = new GameRules.Value(value, type));
        } else {
            result.setValue(value);
        }
        return result;
    }


    public void setOrCreateGameRule(String key, String ruleValue) {
        GameRules.Value gamerules$value = this.rules.get(key);
        if (gamerules$value != null) {
            gamerules$value.setValue(ruleValue);
        } else {
            this.addGameRule(key, ruleValue, GameRules.ValueType.ANY_VALUE);
        }
    }


    public String getString(String name) {
        GameRules.Value gamerules$value = this.rules.get(name);
        return gamerules$value != null ? gamerules$value.getString() : "";
    }


    public boolean getBoolean(String name) {
        GameRules.Value gamerules$value = this.rules.get(name);
        return gamerules$value != null ? gamerules$value.getBoolean() : false;
    }


    public int getInt(String name) {
        GameRules.Value gamerules$value = this.rules.get(name);
        return gamerules$value != null ? gamerules$value.getInt() : 0;
    }


    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        for (String s : this.rules.keySet()) {
            GameRules.Value gamerules$value = this.rules.get(s);
            nbttagcompound.setString(s, gamerules$value.getString());
        }
        return nbttagcompound;
    }


    public void readFromNBT(NBTTagCompound nbt) {
        for (String s : nbt.getKeySet()) {
            this.setOrCreateGameRule(s, nbt.getString(s));
        }
    }


    public String[] getRules() {
        Set<String> set = this.rules.keySet();
        return set.toArray(new String[set.size()]);
    }


    public boolean hasRule(String name) {
        return this.rules.containsKey(name);
    }


    public boolean areSameType(String key, GameRules.ValueType otherValue) {
        GameRules.Value gamerules$value = this.rules.get(key);
        return gamerules$value != null && (gamerules$value.getType() == otherValue || otherValue == GameRules.ValueType.ANY_VALUE);
    }


    public static class Value {
        private String valueString;
        private boolean valueBoolean;
        private int valueInteger;
        private double valueDouble;
        private final GameRules.ValueType type;

        public Value(String value, GameRules.ValueType type) {
            this.type = type;
            this.setValue(value);
        }

        public void setValue(String value) {
            this.valueString = value;
            this.valueInteger = this.valueBoolean ? 1 : 0;

            char chr = value.charAt(0);
            if(chr >= '0' && chr <= '9') {
                try {
                    this.valueInteger = Integer.parseInt(value);
                } catch (NumberFormatException var4) {
                    ;
                }
                try {
                    this.valueDouble = Double.parseDouble(value);
                } catch (NumberFormatException var3) {
                    ;
                }
                valueBoolean = valueDouble > 0;
            } else {
                this.valueBoolean = Boolean.parseBoolean(value);
            }
        }


        public String getString() {
            return this.valueString;
        }


        public boolean getBoolean() {
            return this.valueBoolean;
        }


        public int getInt() {
            return this.valueInteger;
        }


        public GameRules.ValueType getType() {
            return this.type;
        }
    }


    public static enum ValueType {
        ANY_VALUE,
        BOOLEAN_VALUE,
        NUMERICAL_VALUE,
        FUNCTION;
    }
}