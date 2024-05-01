package plus.fserver;

import plus.tson.TsonFile;
import java.io.File;
import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonBoolUtils.*;


public class ElcaConfig {
    public static File NATIVES;
    public static final String SERVER_ID            = "ElcaServer";
    public static final String DEFAULT_REJECT_MSG   = "Connection rejected";
    public static final String REJECT_SERVER_FULL   = "The server is full!";
    public static final String REJECT_NON_WHITELIST = "You are not white-listed on this server!";

    public static int END_PORTAL_TRANSFER_DIM    = 1;

    public static int TIME_PACKET_RATE           = 20 * 3;
    public static int CHUNK_TICK_RADIUS_1        = 4 * 16;
    public static int CHUNK_TICK_RADIUS_2        = 5 * 16;
    public static int MEAN_BALANCE_MOB           = 40 * 12;
    public static int GOAL_TICK_RATE             = 4;
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
    public static int ASY_THREAD_POOL_SIZE       = Runtime.getRuntime().availableProcessors();
    //public static int ENTITY_TRACKER_UPDATE_RATE = 4;


    //Statistic: 0: 33%, 1: 50%, 2: 65%, 3: 75%, 4:80%, 5: 90%, 6: 98%. lower -> faster
    public static int MAX_ENTITY_COLLISIONS      = 3;
    public static int MAX_ENTITY_COLLISIONS_MOVE = 1;
    public static int MAX_SPONGE_POWER           = 64;

    public static int MIN_CHANGED_BLOCKS_BUF     = 24;

    //public static boolean FAST_LIGHT                 = true;
    public static boolean SLOW_SWIM_AI               = true;
    public static boolean SLOW_TEMP_CHECK            = true;
    public static boolean SLOW_WANDER                = true;
    public static boolean DISABLE_SESSION_LOCK       = true;
    public static boolean IGNORE_ERRORS              = true;
    public static boolean REMOVE_ERROR_ENTITIES      = true;
    public static boolean OVERWORLD_ASYNC_POPULATION = true;

    //public static boolean TICK_ONLY_TRACKED_ENTITIES = true;
    public static boolean REMOVE_SERVER_THREAD       = false;
    public static boolean SKIP_CHECK_NETWORK         = true;
    public static boolean PRINT_CAUSE_SERVER_STOP    = true;


    private static File dir(String str){
        File file = new File(str);
        if(file.exists())return file;
        file.mkdirs();
        return file;
    }


    static {
        TsonFile file = new TsonFile("elca-config.tson");

        NATIVES                    = dir(file.getOrDefaultStr("elca-natives", "elca_natives"));

        TIME_PACKET_RATE           = calc(file, "time-packet-rate"            ,  TIME_PACKET_RATE          );
        CHUNK_TICK_RADIUS_1        = calc(file, "chunk-tick-radius-m"         , CHUNK_TICK_RADIUS_1        );
        CHUNK_TICK_RADIUS_2        = calc(file, "chunk-tick-radius-s"         , CHUNK_TICK_RADIUS_2        );

        MEAN_BALANCE_MOB           = calc(file, "mean-balance-mob"            ,     40                 )*12;
        GOAL_TICK_RATE             = calc(file, "goal-tick-rate"              , GOAL_TICK_RATE             );
        RECHECK_SPAWNER_RATE       = calc(file, "recheck-spawner-rate"        , RECHECK_SPAWNER_RATE       );
        CHECK_TO_UNLOAD_RATE       = calc(file, "check-to-unload-rate"        , CHECK_TO_UNLOAD_RATE       );
        SAVE_WORLD_RATE            = calc(file, "save-world-rate"             , SAVE_WORLD_RATE            );
        NAVIGATE_CACHE_CLEAN_RATE  = calc(file, "navigation-cache-clean-rate" , NAVIGATE_CACHE_CLEAN_RATE  );
        STD_UPDATE_NAVIGATION_RATE = calc(file, "std-update-navigation-rate"  , STD_UPDATE_NAVIGATION_RATE );
        MAX_UPDATE_NAVIGATION_RATE = calc(file, "max-update-navigation-rate"  , MAX_UPDATE_NAVIGATION_RATE );
        NAVIGATE_RESET_RATE        = calc(file, "navigate-reset-rate"         , NAVIGATE_RESET_RATE        );

        NETWORK_COMPRESSION_LVL    = calc(file, "network-compression-lvl"     , NETWORK_COMPRESSION_LVL    );
        ASY_THREAD_POOL_SIZE       = calc(file, "async-thread-pool-size"      , ASY_THREAD_POOL_SIZE       );

        MAX_ENTITY_COLLISIONS      = calc(file, "max-entity-collisions"       , MAX_ENTITY_COLLISIONS      );
        MAX_ENTITY_COLLISIONS_MOVE = calc(file, "max-entity-collisions-move"  , MAX_ENTITY_COLLISIONS_MOVE );
        MIN_CHANGED_BLOCKS_BUF     = calc(file, "min-changed-blocks-buf"      , MIN_CHANGED_BLOCKS_BUF     );

        MAX_SPONGE_POWER           = calc(file, "max-sponge-power"            , MAX_SPONGE_POWER           );

        SLOW_SWIM_AI               = calc(file, "slow-swim-ai"                , SLOW_SWIM_AI               );
        SLOW_TEMP_CHECK            = calc(file, "slow-temp-check"             , SLOW_TEMP_CHECK            );
        SLOW_WANDER                = calc(file, "slow-wander"                 , SLOW_WANDER                );
        DISABLE_SESSION_LOCK       = calc(file, "disable-session-lock"        , DISABLE_SESSION_LOCK       );

        IGNORE_ERRORS              = calc(file, "ignore-errors"               , IGNORE_ERRORS              );
        REMOVE_ERROR_ENTITIES      = calc(file, "remove-error-entities"       , REMOVE_ERROR_ENTITIES      );

        OVERWORLD_ASYNC_POPULATION = calc(file, "overworld-async-population"  , OVERWORLD_ASYNC_POPULATION );
        SKIP_CHECK_NETWORK         = calc(file, "skip-check-network"          , SKIP_CHECK_NETWORK         );
        PRINT_CAUSE_SERVER_STOP    = calc(file, "print-case-stop"             , PRINT_CAUSE_SERVER_STOP    );
    }
}