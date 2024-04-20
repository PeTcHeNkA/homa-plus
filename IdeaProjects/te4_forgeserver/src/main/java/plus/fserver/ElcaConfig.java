package plus.fserver;


public class ElcaConfig {
    public static final String SERVER_ID            = "ElcaServer";
    public static final String DEFAULT_REJECT_MSG   = "Connection rejected";
    public static final String REJECT_SERVER_FULL   = "The server is full!";
    public static final String REJECT_NON_WHITELIST = "You are not white-listed on this server!";

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

    public static int BB_CACHE_ALIVE             = 500;
    public static int EE_CACHE_ALIVE             = 500;

    //Statistic: 0: 33%, 1: 50%, 2: 65%, 3: 75%, 4:80%, 5: 90%, 6: 98%. lower -> faster
    public static int MAX_ENTITY_COLLISIONS      = 3;
    public static int MAX_ENTITY_COLLISIONS_MOVE = 1;

    public static int MIN_CHANGED_BLOCKS_BUF     = 24;

    public static boolean FAST_LIGHT                 = true;
    public static boolean SLOW_SWIM_AI               = true;
    public static boolean SLOW_TEMP_CHECK            = true;
    public static boolean SLOW_WANDER                = true;
    public static boolean DISABLE_SESSION_LOCK       = true;
    public static boolean IGNORE_ERRORS              = true;
    public static boolean REMOVE_ERROR_ENTITIES      = true;
    public static boolean OVERWORLD_ASYNC_POPULATION = true;

    public static boolean TICK_ONLY_TRACKED_ENTITIES = true;

    public static boolean REMOVE_SERVER_THREAD       = true;
}