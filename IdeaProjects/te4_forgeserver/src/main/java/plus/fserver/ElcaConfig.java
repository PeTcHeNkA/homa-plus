package plus.fserver;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.Te4HashMap;
import java.io.File;
import java.util.Map;
import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonBoolUtils.*;


public class ElcaConfig {
    //fast-water-check
    //fix-shulcer-nbt-view
    private static Te4HashMap<String,Boolean> VARS;

    public static File NATIVES;
    public static final String SERVER_ID            = "ElcaServer";
    public static final String DEFAULT_REJECT_MSG   = "Connection rejected";
    public static final String REJECT_SERVER_FULL   = "The server is full!";
    public static final String REJECT_NON_WHITELIST = "You are not white-listed on this server!";
    public static final int MEAN_OVERLOAD           = 50;
    public static final long DEFAULT_SOFT_TIME = 1000*60*60;

    public static int END_PORTAL_TRANSFER_DIM    = 1;

    public static int TIME_PACKET_RATE           = 20 * 3;
    public static int CHUNK_TICK_RADIUS_1        = 4 * 16;
    public static int CHUNK_TICK_RADIUS_2        = 5 * 16;
    public static int MEAN_BALANCE_MOB           = 35;
    public static int MEAN_BALANCE_BLOCK         = 35;
    public static int GOAL_TICK_RATE             = 4;
    public static int RECHECK_CHUNKS_SPAWNER_RATE= 7;
    public static int RECHECK_SPAWNER_RATE       = 80;
    public static int CHECK_TO_UNLOAD_RATE       = 600;
    public static int SAVE_WORLD_RATE            = 600 * 20;
    public static int NAVIGATE_CACHE_CLEAN_RATE  = 200;
    public static int STD_UPDATE_NAVIGATION_RATE = 20;
    public static int MAX_UPDATE_NAVIGATION_RATE = 40;
    public static int NAVIGATE_RESET_RATE        = NAVIGATE_CACHE_CLEAN_RATE * 50;
    public static int NETWORK_COMPRESSION_LVL    = 4;
    public static int VILLAGE_TICK_RATE          = 20;
    public static int NETWORK_TICK_RATE          = 25; //50 default,
    public static int BEACON_TICK_RATE           = 85;
    public static int IMMEDIATE_UPDATE_LIMIT     = 24;
    //public static int ASY_THREAD_POOL_SIZE       = Runtime.getRuntime().availableProcessors();
    //public static int ENTITY_TRACKER_UPDATE_RATE = 4;


    //Statistic: 0: 33%, 1: 50%, 2: 65%, 3: 75%, 4:80%, 5: 90%, 6: 98%. lower -> faster
    public static int MAX_ENTITY_COLLISIONS      = 3;
    public static int MAX_ENTITY_COLLISIONS_MOVE = 1;
    public static int MAX_SPONGE_POWER           = 64;
    public static int MAX_PISTON_POWER           = 12;
    public static int MIN_CHANGED_BLOCKS_BUF     = 24;
    public static int RESET_LOW_LEVEL_MOBS       = 200;
    public static int MAX_WORLD_SPAWNER_RANGE    = 7;
    public static int BALANCE_BLOCK_TICK         = 3;
    public static int BALANCE_BLOCK_CRITICAL     = 7;

    public static int MAX_CONNECTIONS_PER        = 15;
    public static int MAX_CONNECTIONS_TIME       = 1000;
    public static int MAX_CONNECTION_WARN        = 10;
    public static int INIT_PALETTE_LINEAR_BITS   = 4; //should also set on client
    public static int READ_TIMEOUT_TIMER         = 17;
    public static int CACHE_CHUNK_DATA           = 24;

    //public static boolean FAST_LIGHT                 = true;
    public static boolean SLOW_SWIM_AI                = true;
    public static boolean SLOW_TEMP_CHECK             = true;
    public static boolean SLOW_WANDER                 = true;
    public static boolean DISABLE_SESSION_LOCK        = true;
    public static boolean IGNORE_ERRORS               = true;
    public static boolean REMOVE_ERROR_ENTITIES       = true;
    public static boolean OVERWORLD_ASYNC_POPULATION  = true;

    //public static boolean TICK_ONLY_TRACKED_ENTITIES  = true;
    public static boolean REMOVE_SERVER_THREAD        = false;
    public static boolean SKIP_CHECK_NETWORK          = true;
    public static boolean PRINT_CAUSE_SERVER_STOP     = true;
    //public static boolean PROCESS_LIGHT_ON_SAVE       = false;
    public static boolean ASYNC_SAVE_CHUNKS           = true;
    //public static boolean FAST_ENTITY_EQUALS          = true;
    public static boolean ENABLE_LEGACY_PING_HANDLER  = false;
    public static boolean ENABLE_DEFAULT_JOIN_MESSAGE = true;
    //public static boolean ENABLE_DATA_FIXER          = false;


    private static File dir(String str){
        File file = new File(str);
        if(file.exists())return file;
        file.mkdirs();
        return file;
    }


    static {
        load();
    }


    public static void load(){
        if(VARS != null)return;
        Te4HashMap<String,Boolean> vars = VARS = new Te4HashMap<>();

        TsonFile file = new TsonFile("elca-config.tson");
        init(file);

        if(!ElcaCV.DEBUG_NO_SERVER){
            vars.put("SIDE_SERVER", FMLLaunchHandler.side().isServer());
        }
    }


    private static void readVars(TsonMap map){
        if(map == null)return;
        Te4HashMap<String,Boolean> vars = VARS;
        for (Map.Entry<String, TsonObj> entry : map.entrySet()) {
            String key = entry.getKey();
            TsonObj val = entry.getValue();
            if (val.isBool()) {
                vars.fput(key, val.getBool());
            }
        }
    }


    public static boolean getVar(String key, boolean def){
        if(VARS == null)load();
        return VARS.getOrDefault(key, def);
    }


    public static void init(TsonMap map){
        NATIVES                    = dir(map.getOrDefaultStr("elca-natives", "elca_natives"));

        map.ifContainsMap("vars", ElcaConfig::readVars);

        TIME_PACKET_RATE           = calc(map, "time-packet-rate"            , TIME_PACKET_RATE           );
        CHUNK_TICK_RADIUS_1        = calc(map, "chunk-tick-radius-m"         , CHUNK_TICK_RADIUS_1        );
        CHUNK_TICK_RADIUS_2        = calc(map, "chunk-tick-radius-s"         , CHUNK_TICK_RADIUS_2        );
        BALANCE_BLOCK_TICK         = calc(map, "balance-block-tick"          , BALANCE_BLOCK_TICK         );
        BALANCE_BLOCK_CRITICAL     = calc(map, "balance-block-critical"      , BALANCE_BLOCK_CRITICAL     );

        MEAN_BALANCE_BLOCK         = calc(map, "mean-balance-block"          ,     35                 );
        MEAN_BALANCE_MOB           = calc(map, "mean-balance-mob"            ,     35                 );

        GOAL_TICK_RATE             = calc(map, "goal-tick-rate"              , GOAL_TICK_RATE             );
        RECHECK_SPAWNER_RATE       = calc(map, "recheck-spawner-rate"        , RECHECK_SPAWNER_RATE       );
        RECHECK_CHUNKS_SPAWNER_RATE= calc(map, "recheck-chunks-spawner-rate" , RECHECK_CHUNKS_SPAWNER_RATE);
        CHECK_TO_UNLOAD_RATE       = calc(map, "check-to-unload-rate"        , CHECK_TO_UNLOAD_RATE       );
        SAVE_WORLD_RATE            = calc(map, "save-world-rate"             , SAVE_WORLD_RATE            );
        NAVIGATE_CACHE_CLEAN_RATE  = calc(map, "navigation-cache-clean-rate" , NAVIGATE_CACHE_CLEAN_RATE  );
        STD_UPDATE_NAVIGATION_RATE = calc(map, "std-update-navigation-rate"  , STD_UPDATE_NAVIGATION_RATE );
        MAX_UPDATE_NAVIGATION_RATE = calc(map, "max-update-navigation-rate"  , MAX_UPDATE_NAVIGATION_RATE );
        NAVIGATE_RESET_RATE        = calc(map, "navigate-reset-rate"         , NAVIGATE_RESET_RATE        );

        NETWORK_COMPRESSION_LVL    = calc(map, "network-compression-lvl"     , NETWORK_COMPRESSION_LVL    );
        //ASY_THREAD_POOL_SIZE       = calc(file, "async-thread-pool-size"      , ASY_THREAD_POOL_SIZE       );

        MAX_ENTITY_COLLISIONS      = calc(map, "max-entity-collisions"       , MAX_ENTITY_COLLISIONS      );
        MAX_ENTITY_COLLISIONS_MOVE = calc(map, "max-entity-collisions-move"  , MAX_ENTITY_COLLISIONS_MOVE );
        MIN_CHANGED_BLOCKS_BUF     = calc(map, "min-changed-blocks-buf"      , MIN_CHANGED_BLOCKS_BUF     );
        RESET_LOW_LEVEL_MOBS       = calc(map, "reset-low-level-mobs"        , RESET_LOW_LEVEL_MOBS       );
        MAX_WORLD_SPAWNER_RANGE    = calc(map, "max-world-spawner-range"     , MAX_WORLD_SPAWNER_RANGE    );

        MAX_SPONGE_POWER           = calc(map, "max-sponge-power"            , MAX_SPONGE_POWER           );
        MAX_PISTON_POWER           = calc(map, "max-piston-power"            , MAX_PISTON_POWER           );
        BEACON_TICK_RATE           = calc(map, "beacon-tick-rate"            , BEACON_TICK_RATE           );
        IMMEDIATE_UPDATE_LIMIT     = calc(map, "immediate-update-limit"      , IMMEDIATE_UPDATE_LIMIT     );

        SLOW_SWIM_AI               = calc(map, "slow-swim-ai"                , SLOW_SWIM_AI               );
        SLOW_TEMP_CHECK            = calc(map, "slow-temp-check"             , SLOW_TEMP_CHECK            );
        SLOW_WANDER                = calc(map, "slow-wander"                 , SLOW_WANDER                );
        DISABLE_SESSION_LOCK       = calc(map, "disable-session-lock"        , DISABLE_SESSION_LOCK       );

        IGNORE_ERRORS              = calc(map, "ignore-errors"               , IGNORE_ERRORS              );
        REMOVE_ERROR_ENTITIES      = calc(map, "remove-error-entities"       , REMOVE_ERROR_ENTITIES      );

        OVERWORLD_ASYNC_POPULATION = calc(map, "overworld-async-population"  , OVERWORLD_ASYNC_POPULATION );
        SKIP_CHECK_NETWORK         = calc(map, "skip-check-network"          , SKIP_CHECK_NETWORK         );
        PRINT_CAUSE_SERVER_STOP    = calc(map, "print-case-stop"             , PRINT_CAUSE_SERVER_STOP    );
        ENABLE_DEFAULT_JOIN_MESSAGE= calc(map, "endable-def-join-msg"        , ENABLE_DEFAULT_JOIN_MESSAGE);

        MAX_CONNECTIONS_PER        = calc(map, "max-connections-per"         , MAX_CONNECTIONS_PER        );
        MAX_CONNECTIONS_TIME       = calc(map, "max-connections-time"        , MAX_CONNECTIONS_TIME       );

        INIT_PALETTE_LINEAR_BITS   = calc(map, "init-palette-linear-bits"    , INIT_PALETTE_LINEAR_BITS   );
        CACHE_CHUNK_DATA           = calc(map, "cache-chunk-data"            , CACHE_CHUNK_DATA           );

        if(INIT_PALETTE_LINEAR_BITS > 4 || INIT_PALETTE_LINEAR_BITS < 2){
            throw new Error("Linear palette bits should be in 2, 3 or 4");
        }
    }
}