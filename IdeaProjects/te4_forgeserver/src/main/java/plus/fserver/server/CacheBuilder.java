package plus.fserver.server;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import plus.fserver.ElcaConfig;
import plus.fserver.utls.AABKey;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CacheBuilder{
    public static Cache<AABKey, List<Entity>> buildEECache(AsyWorldTickerV2 ticker){
        Cache2kBuilder<AABKey, List<Entity>> builder = new Cache2kBuilder<AABKey, List<Entity>>(){}
                .expireAfterWrite(ElcaConfig.EE_CACHE_ALIVE, TimeUnit.MILLISECONDS)
                .disableStatistics(true).strictEviction(true);

        if(ticker != null){
            builder.asyncListenerExecutor(ticker.service);
        }
        return builder.build();
    }



    public static Cache<AABKey, List<AxisAlignedBB>> buildBBCache(AsyWorldTickerV2 ticker){
        Cache2kBuilder<AABKey, List<AxisAlignedBB>> builder = new Cache2kBuilder<AABKey, List<AxisAlignedBB>>(){}
                .expireAfterWrite(ElcaConfig.BB_CACHE_ALIVE, TimeUnit.MILLISECONDS)
                .disableStatistics(true).strictEviction(true);

        if(ticker != null){
            builder.asyncListenerExecutor(ticker.service);
        }
        return builder.build();
    }
}