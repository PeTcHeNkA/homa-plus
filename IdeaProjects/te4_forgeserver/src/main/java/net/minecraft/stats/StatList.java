package net.minecraft.stats;

import net.minecraft.block.*;
import javax.annotation.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.text.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.init.*;
import net.minecraft.entity.*;
import com.google.common.collect.*;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import plus.tson.utl.Te4HashSet;


public class StatList {
    private static final boolean client = FMLLaunchHandler.side() == Side.CLIENT;
    protected static final Map<String, StatBase> ID_TO_STAT_MAP;
    public static final List<StatBase> ALL_STATS;
    public static final List<StatBase> BASIC_STATS;
    public static final List<StatCrafting> USE_ITEM_STATS;
    public static final List<StatCrafting> MINE_BLOCK_STATS;
    public static final StatBase LEAVE_GAME;
    public static final StatBase PLAY_ONE_MINUTE;
    public static final StatBase TIME_SINCE_DEATH;
    public static final StatBase SNEAK_TIME;
    public static final StatBase WALK_ONE_CM;
    public static final StatBase CROUCH_ONE_CM;
    public static final StatBase SPRINT_ONE_CM;
    public static final StatBase SWIM_ONE_CM;
    public static final StatBase FALL_ONE_CM;
    public static final StatBase CLIMB_ONE_CM;
    public static final StatBase FLY_ONE_CM;
    public static final StatBase DIVE_ONE_CM;
    public static final StatBase MINECART_ONE_CM;
    public static final StatBase BOAT_ONE_CM;
    public static final StatBase PIG_ONE_CM;
    public static final StatBase HORSE_ONE_CM;
    public static final StatBase AVIATE_ONE_CM;
    public static final StatBase JUMP;
    public static final StatBase DROP;
    public static final StatBase DAMAGE_DEALT;
    public static final StatBase DAMAGE_TAKEN;
    public static final StatBase DEATHS;
    public static final StatBase MOB_KILLS;
    public static final StatBase ANIMALS_BRED;
    public static final StatBase PLAYER_KILLS;
    public static final StatBase FISH_CAUGHT;
    public static final StatBase TALKED_TO_VILLAGER;
    public static final StatBase TRADED_WITH_VILLAGER;
    public static final StatBase CAKE_SLICES_EATEN;
    public static final StatBase CAULDRON_FILLED;
    public static final StatBase CAULDRON_USED;
    public static final StatBase ARMOR_CLEANED;
    public static final StatBase BANNER_CLEANED;
    public static final StatBase BREWINGSTAND_INTERACTION;
    public static final StatBase BEACON_INTERACTION;
    public static final StatBase DROPPER_INSPECTED;
    public static final StatBase HOPPER_INSPECTED;
    public static final StatBase DISPENSER_INSPECTED;
    public static final StatBase NOTEBLOCK_PLAYED;
    public static final StatBase NOTEBLOCK_TUNED;
    public static final StatBase FLOWER_POTTED;
    public static final StatBase TRAPPED_CHEST_TRIGGERED;
    public static final StatBase ENDERCHEST_OPENED;
    public static final StatBase ITEM_ENCHANTED;
    public static final StatBase RECORD_PLAYED;
    public static final StatBase FURNACE_INTERACTION;
    public static final StatBase CRAFTING_TABLE_INTERACTION;
    public static final StatBase CHEST_OPENED;
    public static final StatBase SLEEP_IN_BED;
    public static final StatBase OPEN_SHULKER_BOX;
    private static StatBase[] BLOCKS_STATS;
    private static StatBase[] CRAFTS_STATS;
    private static StatBase[] OBJECT_USE_STATS;
    private static StatBase[] OBJECT_BREAK_STATS;
    private static StatBase[] OBJECTS_PICKED_UP_STATS;
    private static StatBase[] OBJECTS_DROPPED_STATS;


    @Nullable
    public static StatBase getBlockStats(final Block blockIn) {
        int id = Block.getIdFromBlock(blockIn);
        StatBase[] stats = BLOCKS_STATS;
        if(id >= stats.length)return null;
        return stats[id];
        //return StatList.BLOCKS_STATS[Block.getIdFromBlock(blockIn)];
    }


    @Nullable
    public static StatBase getCraftStats(final Item itemIn) {
        int id = Item.getIdFromItem(itemIn);
        StatBase[] stats = CRAFTS_STATS;
        if(id >= stats.length)return null;
        return stats[id];
        //return StatList.CRAFTS_STATS[Item.getIdFromItem(itemIn)];
    }


    @Nullable
    public static StatBase getObjectUseStats(final Item itemIn) {
        int id = Item.getIdFromItem(itemIn);
        StatBase[] stats = OBJECT_USE_STATS;
        if(id >= stats.length)return null;
        return stats[id];
        //return StatList.OBJECT_USE_STATS[Item.getIdFromItem(itemIn)];
    }


    @Nullable
    public static StatBase getObjectBreakStats(final Item itemIn) {
        int id = Item.getIdFromItem(itemIn);
        StatBase[] stats = OBJECT_BREAK_STATS;
        if(id >= stats.length)return null;
        return stats[id];
        //return StatList.OBJECT_BREAK_STATS[Item.getIdFromItem(itemIn)];
    }


    @Nullable
    public static StatBase getObjectsPickedUpStats(final Item itemIn) {
        int id = Item.getIdFromItem(itemIn);
        StatBase[] stats = OBJECTS_PICKED_UP_STATS;
        if(id >= stats.length)return null;
        return stats[id];
        //return StatList.OBJECTS_PICKED_UP_STATS[Item.getIdFromItem(itemIn)];
    }


    @Nullable
    public static StatBase getDroppedObjectStats(final Item itemIn) {
        int id = Item.getIdFromItem(itemIn);
        StatBase[] stats = OBJECTS_DROPPED_STATS;
        if(id >= stats.length)return null;
        return stats[id];
        //return StatList.OBJECTS_DROPPED_STATS[Item.getIdFromItem(itemIn)];
    }


    public static void init() {
        initMiningStats();
        initStats();
        initItemDepleteStats();
        initCraftableStats();
        initPickedUpAndDroppedStats();
    }


    public static int trimSize(){
        AtomicInteger num = new AtomicInteger();
        BLOCKS_STATS            = trimToSize(BLOCKS_STATS              , num);
        CRAFTS_STATS            = trimToSize(CRAFTS_STATS              , num);
        OBJECT_USE_STATS        = trimToSize(OBJECT_USE_STATS          , num);
        OBJECT_BREAK_STATS      = trimToSize(OBJECT_BREAK_STATS        , num);
        OBJECTS_PICKED_UP_STATS = trimToSize(OBJECTS_PICKED_UP_STATS   , num);
        OBJECTS_DROPPED_STATS   = trimToSize(OBJECTS_DROPPED_STATS     , num);
        return num.get()*8;
    }


    private static void rollBackSize(){
        BLOCKS_STATS              = Arrays.copyOf(BLOCKS_STATS           ,4096 );
        CRAFTS_STATS              = Arrays.copyOf(CRAFTS_STATS           ,32000);
        OBJECT_USE_STATS          = Arrays.copyOf(OBJECT_USE_STATS       ,32000);
        OBJECT_BREAK_STATS        = Arrays.copyOf(OBJECT_BREAK_STATS     ,32000);
        OBJECTS_PICKED_UP_STATS   = Arrays.copyOf(OBJECTS_PICKED_UP_STATS,32000);
        OBJECTS_DROPPED_STATS     = Arrays.copyOf(OBJECTS_DROPPED_STATS  ,32000);
    }


    private static void initCraftableStats() {
        final Set<Item> set = new Te4HashSet<>();
        for (final IRecipe irecipe : CraftingManager.REGISTRY) {
            final ItemStack itemstack = irecipe.getRecipeOutput();
            if (!itemstack.isEmpty()) {
                set.add(irecipe.getRecipeOutput().getItem());
            }
        }
        for (final ItemStack itemstack2 : FurnaceRecipes.instance().getSmeltingList().values()) {
            set.add(itemstack2.getItem());
        }
        for (final Item item : set) {
            if (item != null) {
                final int i = Item.getIdFromItem(item);
                final String s = getItemName(item);
                if (s == null) {
                    continue;
                }
                StatList.CRAFTS_STATS[i] = new StatCrafting("stat.craftItem.", s, translation("stat.craftItem", item), item).registerStat();
            }
        }
        replaceAllSimilarBlocks(StatList.CRAFTS_STATS, true);
    }


    private static ITextComponent translation(String key, Item item){
        if(client)
            return new TextComponentTranslation(key, new ItemStack(item).getTextComponent());
        return StatBase.EMPTY;
    }


    private static ITextComponent translation(String key){
        if(client)
            return new TextComponentTranslation(key);
        return StatBase.EMPTY;
    }


    private static StatBase[] trimToSize(StatBase[] arr, AtomicInteger num){
        int lastNonNull = -1;
        for (int i = 0, s = arr.length; i < s; i++){
            if(arr[i] != null)lastNonNull = i;
        }
        if(lastNonNull+1 < arr.length){
            num.addAndGet(arr.length - lastNonNull+1);
            return Arrays.copyOf(arr, lastNonNull+1);
        }
        return arr;
    }


    private static void initMiningStats() {
        for (final Block block : Block.REGISTRY) {
            final Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                final int i = Block.getIdFromBlock(block);
                final String s = getItemName(item);
                if (s == null || !block.getEnableStats()) {
                    continue;
                }
                BLOCKS_STATS[i] = new StatCrafting("stat.mineBlock.", s, translation("stat.mineBlock", Item.getItemFromBlock(block)), item).registerStat();
                MINE_BLOCK_STATS.add((StatCrafting)BLOCKS_STATS[i]);
            }
        }
        replaceAllSimilarBlocks(BLOCKS_STATS, false);
    }


    private static void initStats() {
        for (final Item item : Item.REGISTRY) {
            if (item != null) {
                final int i = Item.getIdFromItem(item);
                final String s = getItemName(item);
                if (s == null) {
                    continue;
                }
                OBJECT_USE_STATS[i] = new StatCrafting("stat.useItem.", s, translation("stat.useItem", item), item).registerStat();
                if (item instanceof ItemBlock) {
                    continue;
                }
                USE_ITEM_STATS.add((StatCrafting)OBJECT_USE_STATS[i]);
            }
        }
        replaceAllSimilarBlocks(OBJECT_USE_STATS, true);
    }

    private static void initItemDepleteStats() {
        for (final Item item : Item.REGISTRY) {
            if (item != null) {
                final int i = Item.getIdFromItem(item);
                final String s = getItemName(item);
                if (s == null || !item.isDamageable()) {
                    continue;
                }
                OBJECT_BREAK_STATS[i] = new StatCrafting("stat.breakItem.", s, translation("stat.breakItem", item), item).registerStat();
            }
        }
        replaceAllSimilarBlocks(OBJECT_BREAK_STATS, true);
    }


    private static void initPickedUpAndDroppedStats() {
        for (final Item item : Item.REGISTRY) {
            if (item != null) {
                final int i = Item.getIdFromItem(item);
                final String s = getItemName(item);
                if (s == null) {
                    continue;
                }
                OBJECTS_PICKED_UP_STATS[i] = new StatCrafting("stat.pickup.", s, translation("stat.pickup", item), item).registerStat();
                OBJECTS_DROPPED_STATS[i]   = new StatCrafting("stat.drop."  , s, translation("stat.drop"  , item), item).registerStat();
            }
        }
        replaceAllSimilarBlocks(OBJECT_BREAK_STATS, true);
    }


    @Nullable
    private static String getItemName(final Item itemIn) {
        final ResourceLocation loc = Item.REGISTRY.getNameForObject(itemIn);
        return (loc != null) ? loc.toString().replace(':', '.') : null;
    }


    private static void replaceAllSimilarBlocks(StatBase[] p_replaceAllSimilarBlocks_0_, boolean p_replaceAllSimilarBlocks_1_) {
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.WATER, Blocks.FLOWING_WATER, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.LAVA, Blocks.FLOWING_LAVA, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.LIT_PUMPKIN, Blocks.PUMPKIN, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.LIT_FURNACE, Blocks.FURNACE, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_ORE, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.POWERED_REPEATER, Blocks.UNPOWERED_REPEATER, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.POWERED_COMPARATOR, Blocks.UNPOWERED_COMPARATOR, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.REDSTONE_TORCH, Blocks.UNLIT_REDSTONE_TORCH, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.LIT_REDSTONE_LAMP, Blocks.REDSTONE_LAMP, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.DOUBLE_STONE_SLAB, Blocks.STONE_SLAB, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.DOUBLE_WOODEN_SLAB, Blocks.WOODEN_SLAB, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.DOUBLE_STONE_SLAB2, Blocks.STONE_SLAB2, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.GRASS, Blocks.DIRT, p_replaceAllSimilarBlocks_1_);
        mergeStatBases(p_replaceAllSimilarBlocks_0_, Blocks.FARMLAND, Blocks.DIRT, p_replaceAllSimilarBlocks_1_);
    }

    private static void mergeStatBases(StatBase[] stats, Block block1, Block block2, boolean b) {
        int i;
        int j;
        if (b) {
            i = Item.getIdFromItem(Item.getItemFromBlock(block1));
            j = Item.getIdFromItem(Item.getItemFromBlock(block2));
        } else {
            i = Block.getIdFromBlock(block1);
            j = Block.getIdFromBlock(block2);
        }

        if (stats[i] != null && stats[j] == null) {
            stats[j] = stats[i];
        } else {
            ALL_STATS.remove(stats[i]);
            MINE_BLOCK_STATS.remove(stats[i]);
            BASIC_STATS.remove(stats[i]);
            stats[i] = stats[j];
        }
    }


    @Nullable
    public static StatBase getStatKillEntity(final EntityList.EntityEggInfo eggInfo) {
        final String s = EntityList.getTranslationName(eggInfo.spawnedID);
        return (s == null) ? null : new StatBase("stat.killEntity." + s, (ITextComponent)new TextComponentTranslation("stat.entityKill", new Object[] { new TextComponentTranslation("entity." + s + ".name") })).registerStat();
    }


    @Nullable
    public static StatBase getStatEntityKilledBy(final EntityList.EntityEggInfo eggInfo) {
        final String s = EntityList.getTranslationName(eggInfo.spawnedID);
        return (s == null) ? null : new StatBase("stat.entityKilledBy." + s, new TextComponentTranslation("stat.entityKilledBy", new TextComponentTranslation("entity." + s + ".name"))).registerStat();
    }


    @Nullable
    public static StatBase getOneShotStat(final String statName) {
        return StatList.ID_TO_STAT_MAP.get(statName);
    }


    @Deprecated
    public static void reinit() {
        rollBackSize();

        StatList.ID_TO_STAT_MAP.clear();
        StatList.BASIC_STATS.clear();
        StatList.USE_ITEM_STATS.clear();
        StatList.MINE_BLOCK_STATS.clear();
        for (final StatBase[] sb : new StatBase[][] { StatList.BLOCKS_STATS, StatList.CRAFTS_STATS, StatList.OBJECT_USE_STATS, StatList.OBJECT_BREAK_STATS, StatList.OBJECTS_PICKED_UP_STATS, StatList.OBJECTS_DROPPED_STATS }) {
            for (int x = 0; x < sb.length; ++x) {
                if (sb[x] != null) {
                    StatList.ALL_STATS.remove(sb[x]);
                    sb[x] = null;
                }
            }
        }
        final List<StatBase> unknown = (List<StatBase>)Lists.newArrayList((Iterable)StatList.ALL_STATS);
        StatList.ALL_STATS.clear();
        for (final StatBase b : unknown) {
            b.registerStat();
        }
        initMiningStats();
        initStats();
        initItemDepleteStats();
        initCraftableStats();
        initPickedUpAndDroppedStats();

        trimSize();
    }


    static {
        ID_TO_STAT_MAP = Maps.newHashMap();
        ALL_STATS = Lists.newArrayList();
        BASIC_STATS = Lists.newArrayList();
        USE_ITEM_STATS = Lists.newArrayList();
        MINE_BLOCK_STATS = Lists.newArrayList();
        LEAVE_GAME                = new StatBasic("stat.leaveGame"               , translation("stat.leaveGame")).initIndependentStat().registerStat();
        PLAY_ONE_MINUTE           = new StatBasic("stat.playOneMinute"           , translation("stat.playOneMinute"), StatBase.timeStatType).initIndependentStat().registerStat();
        TIME_SINCE_DEATH          = new StatBasic("stat.timeSinceDeath"          , translation("stat.timeSinceDeath"), StatBase.timeStatType).initIndependentStat().registerStat();
        SNEAK_TIME                = new StatBasic("stat.sneakTime"               , translation("stat.sneakTime"), StatBase.timeStatType).initIndependentStat().registerStat();
        WALK_ONE_CM               = new StatBasic("stat.walkOneCm"               , translation("stat.walkOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        CROUCH_ONE_CM             = new StatBasic("stat.crouchOneCm"             , translation("stat.crouchOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        SPRINT_ONE_CM             = new StatBasic("stat.sprintOneCm"             , translation("stat.sprintOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        SWIM_ONE_CM               = new StatBasic("stat.swimOneCm"               , translation("stat.swimOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        FALL_ONE_CM               = new StatBasic("stat.fallOneCm"               , translation("stat.fallOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        CLIMB_ONE_CM              = new StatBasic("stat.climbOneCm"              , translation("stat.climbOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        FLY_ONE_CM                = new StatBasic("stat.flyOneCm"                , translation("stat.flyOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        DIVE_ONE_CM               = new StatBasic("stat.diveOneCm"               , translation("stat.diveOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        MINECART_ONE_CM           = new StatBasic("stat.minecartOneCm"           , translation("stat.minecartOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        BOAT_ONE_CM               = new StatBasic("stat.boatOneCm"               , translation("stat.boatOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        PIG_ONE_CM                = new StatBasic("stat.pigOneCm"                , translation("stat.pigOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        HORSE_ONE_CM              = new StatBasic("stat.horseOneCm"              , translation("stat.horseOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        AVIATE_ONE_CM             = new StatBasic("stat.aviateOneCm"             , translation("stat.aviateOneCm"), StatBase.distanceStatType).initIndependentStat().registerStat();
        JUMP                      = new StatBasic("stat.jump"                    , translation("stat.jump")).initIndependentStat().registerStat();
        DROP                      = new StatBasic("stat.drop"                    , translation("stat.drop")).initIndependentStat().registerStat();
        DAMAGE_DEALT              = new StatBasic("stat.damageDealt"             , translation("stat.damageDealt"), StatBase.divideByTen).registerStat();
        DAMAGE_TAKEN              = new StatBasic("stat.damageTaken"             , translation("stat.damageTaken"), StatBase.divideByTen).registerStat();
        DEATHS                    = new StatBasic("stat.deaths"                  , translation("stat.deaths")).registerStat();
        MOB_KILLS                 = new StatBasic("stat.mobKills"                , translation("stat.mobKills")).registerStat();
        ANIMALS_BRED              = new StatBasic("stat.animalsBred"             , translation("stat.animalsBred")).registerStat();
        PLAYER_KILLS              = new StatBasic("stat.playerKills"             , translation("stat.playerKills")).registerStat();
        FISH_CAUGHT               = new StatBasic("stat.fishCaught"              , translation("stat.fishCaught")).registerStat();
        TALKED_TO_VILLAGER        = new StatBasic("stat.talkedToVillager"        , translation("stat.talkedToVillager")).registerStat();
        TRADED_WITH_VILLAGER      = new StatBasic("stat.tradedWithVillager"      , translation("stat.tradedWithVillager")).registerStat();
        CAKE_SLICES_EATEN         = new StatBasic("stat.cakeSlicesEaten"         , translation("stat.cakeSlicesEaten")).registerStat();
        CAULDRON_FILLED           = new StatBasic("stat.cauldronFilled"          , translation("stat.cauldronFilled")).registerStat();
        CAULDRON_USED             = new StatBasic("stat.cauldronUsed"            , translation("stat.cauldronUsed")).registerStat();
        ARMOR_CLEANED             = new StatBasic("stat.armorCleaned"            , translation("stat.armorCleaned")).registerStat();
        BANNER_CLEANED            = new StatBasic("stat.bannerCleaned"           , translation("stat.bannerCleaned")).registerStat();
        BREWINGSTAND_INTERACTION  = new StatBasic("stat.brewingstandInteraction" , translation("stat.brewingstandInteraction")).registerStat();
        BEACON_INTERACTION        = new StatBasic("stat.beaconInteraction"       , translation("stat.beaconInteraction")).registerStat();
        DROPPER_INSPECTED         = new StatBasic("stat.dropperInspected"        , translation("stat.dropperInspected")).registerStat();
        HOPPER_INSPECTED          = new StatBasic("stat.hopperInspected"         , translation("stat.hopperInspected")).registerStat();
        DISPENSER_INSPECTED       = new StatBasic("stat.dispenserInspected"      , translation("stat.dispenserInspected")).registerStat();
        NOTEBLOCK_PLAYED          = new StatBasic("stat.noteblockPlayed"         , translation("stat.noteblockPlayed")).registerStat();
        NOTEBLOCK_TUNED           = new StatBasic("stat.noteblockTuned"          , translation("stat.noteblockTuned")).registerStat();
        FLOWER_POTTED             = new StatBasic("stat.flowerPotted"            , translation("stat.flowerPotted")).registerStat();
        TRAPPED_CHEST_TRIGGERED   = new StatBasic("stat.trappedChestTriggered"   , translation("stat.trappedChestTriggered")).registerStat();
        ENDERCHEST_OPENED         = new StatBasic("stat.enderchestOpened"        , translation("stat.enderchestOpened")).registerStat();
        ITEM_ENCHANTED            = new StatBasic("stat.itemEnchanted"           , translation("stat.itemEnchanted")).registerStat();
        RECORD_PLAYED             = new StatBasic("stat.recordPlayed"            , translation("stat.recordPlayed")).registerStat();
        FURNACE_INTERACTION       = new StatBasic("stat.furnaceInteraction"      , translation("stat.furnaceInteraction")).registerStat();
        CRAFTING_TABLE_INTERACTION= new StatBasic("stat.craftingTableInteraction", translation("stat.workbenchInteraction")).registerStat();
        CHEST_OPENED              = new StatBasic("stat.chestOpened"             , translation("stat.chestOpened")).registerStat();
        SLEEP_IN_BED              = new StatBasic("stat.sleepInBed"              , translation("stat.sleepInBed")).registerStat();
        OPEN_SHULKER_BOX          = new StatBasic("stat.shulkerBoxOpened"        , translation("stat.shulkerBoxOpened")).registerStat();

        BLOCKS_STATS              = new StatBase[4096];
        CRAFTS_STATS              = new StatBase[32000];
        OBJECT_USE_STATS          = new StatBase[32000];
        OBJECT_BREAK_STATS        = new StatBase[32000];
        OBJECTS_PICKED_UP_STATS   = new StatBase[32000];
        OBJECTS_DROPPED_STATS     = new StatBase[32000];
    }
}
