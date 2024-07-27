package plus.space.server;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.utls.WorldConstructor;
import plus.space.common.SNames;
import plus.space.server.world.SolarWorld;
import plus.tson.TsonMap;
import static plus.space.server.SolarRepository.types;


public class SolarWorldItem {
    public final SolarRepository repository;
    public final boolean allowToggleSp;
    public final boolean defaultEnableSp;
    public final TsonMap worldGenInfo;
    public final boolean hasOxygen;
    public final String name;
    public final int dimID;
    public final int raceID;
    public final SolarRepository.Type type;
    public final SolarPlanet planet;
    public final int sunDistance;


    public SolarWorldItem(SolarRepository repository, SolarPlanet planet){
        this.planet = planet;
        this.repository = repository;
        TsonMap settings = planet.settings;

        int typeID      = settings.getOrDefaultInt  ( SNames.TYPE,             0     );
        name            = settings.getStr           ( SNames.NAME                        );
        dimID           = settings.getInt           ( SNames.ID                          );
        allowToggleSp   = settings.getOrDefaultBool ( SNames.ALLOW_TOGGLE_SP,  true  );
        defaultEnableSp = settings.getOrDefaultBool ( SNames.ENABLE_SP_DEFAULT,false );
        hasOxygen       = settings.getOrDefaultBool ( SNames.HAS_OXYGEN,       true  );
        worldGenInfo    = settings.getOrCreateMap   ( SNames.SETTINGS                    );
        raceID          = settings.getOrDefaultInt  ( SNames.RACE_ID,          0     );
        sunDistance     = settings.getInt           ( SNames.SUN_RANGE                   );

        type = types[typeID];
        repository.useWorldId(dimID);

        if(type == SolarRepository.Type.PLANET){
            repository.planetLib.use(name);
        } else {
            if (repository.hasStation(this)) {
                repository.stationLib.use(name);
            } else {
                repository.meteorBeltLib.use(name);
            }
        }
        WorldConstructor.register(dimID, newConstructor(), type.dimType);
    }


    public IChunkGeneratorConstructor getChunkGenConstructor(){
        return repository.getGenerator(this);
    }


    private WorldConstructor.Constructor newConstructor(){
        return new WorldConstructor.Constructor() {
            @Override
            public WorldServerMulti create(MinecraftServer server,
                                           ISaveHandler saveHandler,
                                           int dimId,
                                           WorldServer delegate, Profiler profiler) {
                return new SolarWorld(server, saveHandler,dimId,delegate,profiler){
                    @Override
                    public SolarWorldItem item() {
                        return SolarWorldItem.this;
                    }
                };
            }
        };
    }
}