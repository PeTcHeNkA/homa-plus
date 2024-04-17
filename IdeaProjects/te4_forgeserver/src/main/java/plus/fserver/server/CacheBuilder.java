package plus.fserver.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import plus.fserver.ElcaConfig;
import plus.fserver.utls.AABKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class CacheBuilder{
    public static Map<AABKey, List<Entity>> buildEECache(AsyWorldTickerV2 ticker){
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(ElcaConfig.EE_CACHE_ALIVE, TimeUnit.MILLISECONDS);

        if(ticker != null) {
            caffeine.executor(ticker.service);
        }

        Cache<AABKey, List<Entity>> cache = caffeine.build();
        return cache.asMap();
    }



    public static Map<AABKey, List<AxisAlignedBB>> buildBBCache(AsyWorldTickerV2 ticker){

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(ElcaConfig.BB_CACHE_ALIVE, TimeUnit.MILLISECONDS);

        if(ticker != null) {
            caffeine.executor(ticker.service);
        }

        Cache<AABKey, List<AxisAlignedBB>> cache = caffeine.build();
        return cache.asMap();

//        Cache2kBuilder<AABKey, List<AxisAlignedBB>> builder = new Cache2kBuilder<AABKey, List<AxisAlignedBB>>(){}
//                .expireAfterWrite(ElcaConfig.BB_CACHE_ALIVE, TimeUnit.MILLISECONDS)
//                .disableStatistics(true).strictEviction(true);
//
//        if(ticker != null){
//            builder.asyncListenerExecutor(ticker.service);
//        }
//        return builder.build();
    }
}