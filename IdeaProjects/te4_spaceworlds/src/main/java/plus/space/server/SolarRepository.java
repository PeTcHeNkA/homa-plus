package plus.space.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import plus.fserver.world.generator.biome.BiomeLib;
import plus.fserver.world.generator.biome.BiomeProviderEx;
import plus.space.common.GalName;
import plus.space.common.SNames;
import plus.space.common.utl.TsonRemapper;
import plus.space.common.world.pro.BiomeOrbit;
import plus.space.common.world.pro.EOrbitProvider;
import plus.fserver.utls.math.FastRandom;
import plus.space.server.gen.DChunkGeneratorMeteors;
import plus.space.server.gen.EOrbitGenerator;
import plus.space.server.gen.EPlanetGenerator;
import plus.tson.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


public class SolarRepository {
    protected static final Race[] races = new Race[4];
    public enum Race{
        NEUTRAL(0), IMPERIAL(1), VOIDS(2), PIRACY(3);
        public final int ID;
        //TODO
        int[] meteorGroupIds = new int[]{1,2,3,4,5};
        Race(int id) {
            ID = id;
        }
    }

    enum Type{                             //???
        PLANET(0, EOrbitProvider.ORBIT), ORBIT(1, EOrbitProvider.ORBIT), ASTEROID_BELT(2, EOrbitProvider.ORBIT);
        final int ID;
        final DimensionType dimType;
        Type(int id, DimensionType type) {
            this.ID = id;
            this.dimType = type;
        }
    }

    protected static final Type[] types = ((Supplier<Type[]>) () -> {
        Type[] types0 = Type.values();
        Arrays.sort(types0, Comparator.comparingInt(o -> o.ID));
        return types0;
    }).get();

//    static {
//
//        Arrays.sort(races, Comparator.comparingInt(o -> o.ID));
//    }

    private static class EmptySystem implements ISolarSystem{
        private final GalName name;
        private final int mapX, mapY, mapZ, textureId;
        private final float size;

        private EmptySystem(String name, int mapX, int mapY, int mapZ, int textureId, float size) {
            this.name = new GalName(name,null);
            this.mapX = mapX;
            this.mapY = mapY;
            this.mapZ = mapZ;
            this.textureId = textureId;
            this.size = size;
        }


        @Override
        public int getX() {
            return mapX;
        }


        @Override
        public int getY() {
            return mapY;
        }


        @Override
        public int getZ() {
            return mapZ;
        }


        @Override
        public int getTextureId() {
            return textureId;
        }


        @Override
        public float getSize() {
            return size;
        }


        @Override
        public GalName getFullName() {
            return name;
        }
    }

    final Map<String,ISolarSystem> systems = new ConcurrentHashMap<>();

    public final MinecraftServer server;
    private boolean needSave = false;

    private final TsonFile repository;

    final SNameLib starLib;
    final SNameLib planetLib;
    final SNameLib stationLib;
    final SNameLib meteorBeltLib;

    //final int[] meteorTextures = new int[]{399};

    private final BiomeCreatorLib biomePlanetCreatorLib;

    public static SolarRepository INST;
    public static SolarRepository init(MinecraftServer server){
        return INST = new SolarRepository(server);
    }


    private SolarRepository(MinecraftServer server) {
        SolarMapServerProvider.init(this);
        this.server = server;

        starLib = new SNameLib("star-names.txt");
        planetLib = new SNameLib("planet-names.txt");
        stationLib = new SNameLib("station-names.txt");
        meteorBeltLib = new SNameLib("meteor-belt-names.txt");

        biomePlanetCreatorLib = new BiomeCreatorLib(
                new TJsonFile("planet-biome-lib-gen.json", false)
        );

        long start = System.currentTimeMillis();
        repository = readSystems();
        if(systems.isEmpty()){
            generate(220, 220, 1500, 900, 550,3);
            saveIfNeed();
            TsonFile.write(
                    new File("exported_systems.json"),
                    new TJsonWriter(SNames.remapRepo(repository), 2, 48, false).toString()
            );
        }
        System.out.println("\n---------\nSolar repository loading time: "
                +(System.currentTimeMillis()-start)+"ms\n"+
                "Systems: "+systems.size()+"\n"+
                "Planets: "+planetLib.countUsages()+"\n"+
                "Stations: "+stationLib.countUsages()+"\n"+
                "Meteors: "+meteorBeltLib.countUsages()+"\n"+
                "\n---------"
        );
    }


    public void generate(int attempts, int limit, int maxX, int maxY, int maxZ, int nearMin){
        Random random = random();
        class Cord{
            final int x, y, z;
            Cord(){
                x = random.nextInt(maxX);
                y = random.nextInt(maxY);
                z = random.nextInt(maxZ);
            }
            boolean isNear(Cord other){
                return isNear(x,  other.x) || isNear(y, other.y) || isNear(z, other.z);
            }
            boolean isNear(int a, int b){
                return a>b?a-b<nearMin:b-a<nearMin;
            }
            boolean isNearAny(ArrayList<Cord> cords){
                for(Cord cord:cords) if(isNear(cord))return true;
                return false;
            }
        }
        //TODO проверить корректность расчета близости (по другому)
        int limit0 = limit;
        ArrayList<Cord> cords = new ArrayList<>(attempts);
        int generated = 0;
        long time = System.currentTimeMillis();
        while (attempts>0 && limit0>0){
            Cord cord = new Cord();
            if(!cord.isNearAny(cords)){
                --attempts;
                limit0 = limit;
                cords.add(cord);
                ISolarSystem ss = create(cord.x, cord.y, cord.z);
                getRealSystem(ss.getName());
                ++generated;
            } else {
                --limit;
            }
            long cur = System.currentTimeMillis();
            if(time+1000>cur){
                time = cur;
                System.out.printf("Generated systems: %s/%s, A:%s \n", generated, limit, attempts);
            }
        }
    }


    public void saveIfNeed(){
        if(!needSave)return;
        repository.save();
        needSave = false;
    }


    private TsonFile readSystems(){
        TsonFile repo = new TsonFile("solar-repo.tson");
        if(!repo.containsKey(SNames.SYSTEMS)){
            repo.addMap(SNames.SYSTEMS);
            return repo;
        }
        TsonMap systems = repo.getMap(SNames.SYSTEMS);
        for(String name:systems.keySet().toArray(new String[0])){
            this.systems.put(name, read(systems.getMap(name)));
        }
        TsonOptimizer optimizer = new TsonOptimizer();
        optimizer.feed(
                SNames.SYSTEMS, SNames.BIOMES, SNames.BIOME_ID, SNames.NAME,
                SNames.DENSITY, SNames.ID, SNames.DISTANCE, SNames.ENABLE_SP_DEFAULT,
                SNames.ALLOW_TOGGLE_SP, SNames.HAS_ADDITIONAL, SNames.GENERATOR,
                SNames.HAS_PORTAL, SNames.HAS_STATION, SNames.HAS_OXYGEN, SNames.MAIN,
                SNames.METEOR_END_CIRCLE, SNames.METEOR_START_CIRCLE, SNames.METEOR_GROUP_ID
        );

        return optimizer.optimize(repo);
    }


    private int worldIdGen = 120001;
    public void useWorldId(int id){
        if(id+1>=worldIdGen)worldIdGen = id+1;
    }


    public int getNextFreeWorldId(){
        synchronized (this) {
            int newId = worldIdGen;
            while (DimensionManager.isDimensionRegistered(newId)) {
                worldIdGen = ++newId;
            }
            worldIdGen = newId + 1;
            return newId;
        }
    }


    public Random random(){
        return FastRandom.INST;
        //return server.worlds[0].rand;
    }


    public IChunkGeneratorConstructor getGenerator(SolarWorldItem worldItem){
        switch (worldItem.type){
            case ASTEROID_BELT: return DChunkGeneratorMeteors::new;
            case ORBIT: return EOrbitGenerator::new;
            case PLANET: return (world, item) -> {
                world.setBiomeProvider(new BiomeProviderEx(world.getWorldInfo(), new BiomeLib(
                        item.planet.settings.getMap(SNames.SETTINGS).getMap(SNames.BIOMES)))
                );
                return new EPlanetGenerator(world, worldItem);
            };
        }///forge setdimension @p 121010
        throw new IllegalArgumentException();
    }


    boolean hasStation(SolarWorldItem item){
        return item.worldGenInfo.getOrDefaultBool(SNames.HAS_STATION, false);
    }


    public void createOrbitModelSWI(TsonMap model, int distance, Race race, AtomicBoolean hasPortal){
        model.put( SNames.ID, getNextFreeWorldId() );
        model.put( SNames.RACE_ID, race.ID);
        model.put( SNames.HAS_OXYGEN, false);
        model.put( SNames.ENABLE_SP_DEFAULT, true);
        model.put( SNames.DISTANCE, distance );
        TsonMap genSettings = model.addMap(SNames.SETTINGS);

        createOrbitGenSettings(genSettings, race, hasPortal);
        boolean hasStation = genSettings.getOrDefaultBool(SNames.HAS_STATION, false);
        if(hasStation){
            model.put( SNames.TYPE, Type.ORBIT.ID);

            model.put( SNames.NAME, stationLib.nextAndUse() );
            model.put( SNames.TEXTURE, SolarTextureChooser.choiceStationId(race) );
            model.put( SNames.SIZE,1 );
        } else {
            model.put( SNames.TYPE, Type.ASTEROID_BELT.ID);

            model.put( SNames.NAME, meteorBeltLib.nextAndUse() );
            model.put( SNames.TEXTURE, SolarTextureChooser.chooseMetBeltID() );
            model.put( SNames.SIZE, 0.8f+(FastRandom.INST.nextFloat()/2)   );
        }

        needSave = true;
    }


    public ISolarSystem getSystem(String name){
        return systems.get(name);
    }


    public ISolarSystem getRealSystem(String name){
        ISolarSystem system = getSystem(name);
        if(system==null)return null;
        if(system.isReal())return system;
        Race race = getRandomRaceByCords(system.getX(), system.getY());
        TsonMap model = repository.getMap(SNames.SYSTEMS).getMap(system.getName());
        createSolarSystemModel(system, model, race);
        system = new SolarSystem(this, model);
        systems.put(name, system);
        return system;
    }


    private Race getRandomRaceByCords(int posX, int posY){
        float px = posX/1000f;
        float py = posY/1000f;
        Random random = random();
        boolean flag1 = random.nextFloat()>px;
        boolean flag2 = random.nextFloat()>py;
        if(flag1){
            if(flag2){
                return Race.IMPERIAL;
            } else {
                return Race.NEUTRAL;
            }
        } else {
            if(flag2){
                return Race.PIRACY;
            } else {
                return Race.VOIDS;
            }
        }
    }


    public ISolarSystem read(TsonMap model){
        if(model.containsKey(SNames.ORBIT)){
            return new SolarSystem(this, model);
        } else {
            return new EmptySystem(
                    model.getStr(SNames.NAME),
                    model.getInt(SNames.POS_X),
                    model.getInt(SNames.POS_Y),
                    model.getInt(SNames.POS_Z),
                    model.getInt(SNames.TEXTURE),
                    model.getFloat(SNames.SIZE)
                    );
        }
    }


    @Deprecated
    private int getRandomTexture(Type type){
        int[] arr = null;
        if(type!=null){
            switch (type) {
                case ORBIT:
                    break;
            }
        }
        if(arr==null)throw new UnsupportedOperationException("Use SolarTextureChooser");
        return arr[random().nextInt(arr.length)];
    }


    public ISolarSystem create(int xPos, int yPos, int zPos){
        String name = starLib.nextAndUse();
        int texture = SolarTextureChooser.chooseStarId();
        float size = 0.7f+FastRandom.INST.nextFloat();
        TsonMap systemModel = new TsonMap();
        repository.getMap(SNames.SYSTEMS).put(name, systemModel);
        systemModel.put( SNames.NAME, name  );
        systemModel.put( SNames.POS_X, xPos );
        systemModel.put( SNames.POS_Y, yPos );
        systemModel.put( SNames.POS_Z, zPos );
        systemModel.put( SNames.TEXTURE, texture );
        systemModel.put( SNames.SIZE, size );

        EmptySystem ss = new EmptySystem(name, xPos, yPos, zPos, texture, size);
        systems.put(name, ss);

        needSave = true;
        return ss;
    }


    public void createSolarSystemModel(ISolarSystem empty, TsonMap solarModel, Race race){
        if(empty.isReal())throw new IllegalArgumentException();
        Random random = random();
        int cursor = random.nextInt(2);
        int count = cursor + (int) ((2+random.nextInt(6))*empty.getSize());
        TsonList orbit = solarModel.addList(SNames.ORBIT);
        AtomicBoolean hasPortal = new AtomicBoolean(false);
        for(int i=cursor;i<count;i++){
            addPlanetInModel(random, orbit, i, race, hasPortal);
        }
        if(!hasPortal.get()){
            TsonMap model;
            int errors = 0;
            do {
                model = orbit.getMap(random.nextInt(orbit.size()));
                if(++errors==5){
                    int distance = model.getInt(SNames.DISTANCE);
                    completePlanetModel(random, model, distance, race, hasPortal, 2);

                    if(hasPortal.get()){
                        needSave = true;
                        return;
                    } else {
                        break;
                    }
                }
            } while (!hasSpOrbits(model));

            TsonList prtModel = model.getList(SNames.ORBIT);
            TsonMap curModel;

            if(!hasSpOrbits(model)){
                int distance = prtModel.size();
                for (TsonObj obj:prtModel){
                    TsonMap innModel = obj.getMap();
                    int curDis = innModel.getInt(SNames.DISTANCE);
                    if(curDis>distance){
                        distance = curDis+1;
                    }
                }
                curModel = new TsonMap();
                curModel.put(SNames.DISTANCE, distance);
                createOrbitModelSWI(curModel, distance, somEdit(random, race), hasPortal);
            } else {
                curModel = prtModel.getMap(prtModel.size()==1?0:random.nextInt(prtModel.size()));
            }
            if(!hasPortal.get()){
                curModel.getMap(SNames.SETTINGS).put(SNames.HAS_PORTAL, true);
            }
        }
        needSave = true;
    }


    private boolean hasSpOrbits(TsonMap model){
        if(model.containsKey(SNames.ORBIT)){
            for (TsonObj obj:model.getList(SNames.ORBIT)){
                TsonMap curModel = obj.getMap();
                if(curModel.getInt(SNames.TYPE)==Type.ORBIT.ID){
                    return true;
                }
            }
        }
        return false;
    }


    //TODO ADD SATELLITE GEN
    private void addPlanetInModel(Random random, TsonList solarModelOrbits, int distance, Race race, AtomicBoolean hasPortal){
        TsonMap planetModel = new TsonMap();
        solarModelOrbits.add(planetModel);

        planetModel.put(SNames.DISTANCE, distance);
        planetModel.put(SNames.SUN_RANGE, distance);
        createPlanetModelSWI(distance, planetModel, somEdit(random, race), false);

        int count = random.nextInt((int) (10*planetModel.getFloat(SNames.SIZE))) - 3;
        completePlanetModel(random, planetModel, distance, race, hasPortal, count);
    }


    private void completePlanetModel(Random random, TsonMap planetModel, int distance, Race race, AtomicBoolean hasPortal, int count){
        if(count>0) {
            TsonList orbit = planetModel.addList(SNames.ORBIT);
            for (int innDistance = 0; innDistance < count; innDistance++) {
                TsonMap model = new TsonMap();
                model.put(SNames.DISTANCE, innDistance);
                model.put(SNames.SUN_RANGE, distance);
                orbit.add(model);

                if (random.nextInt(4) != 2) {
                    createOrbitModelSWI(model, distance, somEdit(random, race), hasPortal);
                } else {
                    createPlanetModelSWI(distance, model, somEdit(random, race), true);
                }
            }
        }
    }


    private Race somEdit(Random random, Race race){
        if(random.nextInt(6)==3){
            Race[] values = Race.values();
            return values[random.nextInt(values.length)];
        }
        return race;
    }


    //TODO ?
    public void createPlanetModelSWI(int sunDist, TsonMap planetModel, Race race, boolean isMoon){
        boolean hasOxygen;
        boolean maybeLife;
        if(sunDist>1 && sunDist < 5){
            hasOxygen = random().nextInt(3)!=1;
            if(isMoon)hasOxygen = !hasOxygen;
            maybeLife = true;
        } else {
            hasOxygen = maybeLife = false;
        }

        planetModel.put( SNames.ID, getNextFreeWorldId() );
        planetModel.put( SNames.RACE_ID, race.ID);
        planetModel.put( SNames.HAS_OXYGEN, hasOxygen);
        planetModel.put( SNames.ENABLE_SP_DEFAULT, false);
        planetModel.put( SNames.TYPE, Type.PLANET.ID);
        planetModel.put( SNames.NAME, planetLib.nextAndUse());

        TsonMap genSettings = planetModel.addMap(SNames.SETTINGS);
        createPlanetGenSettings(genSettings, race, isMoon || !maybeLife);
        BiomeLib lib = new BiomeLib(genSettings.getMap(SNames.BIOMES));

        if(hasOxygen || !isMoon) {
            SolarTextureChooser.Checker checker = SolarTextureChooser.choicePlanetId(lib);
            if(!checker.hasWater() && !hasOxygen && !checker.isClean()){
                planetModel.put(SNames.TEXTURE, SolarTextureChooser.chooceMoonID());
                planetModel.put(SNames.SIZE, 1.2f+(FastRandom.INST.nextFloat()*0.9f));
                needSave = true;
                return;
            }
            planetModel.put(SNames.TEXTURE, checker.calcPlanetTextureId());
        } else {
            planetModel.put(SNames.TEXTURE, SolarTextureChooser.chooceMoonID());
        }

        if(isMoon){
            planetModel.put(SNames.SIZE,(0.8f+(FastRandom.INST.nextFloat()*0.5f))*0.8f);
        } else {
            planetModel.put(SNames.SIZE, 0.8f+(FastRandom.INST.nextFloat()*0.6f)      );
        }
        needSave = true;
    }


    public ISolarOrbitObj getObj(GalName name) {
        ArrayList<String> inverted = new ArrayList<>();
        while (name != null) {
            inverted.add(0, name.getSelfName());
            name = name.getParent();
        }
        ISolarOrbitObj orbObj = getRealSystem(inverted.remove(0));
        while (inverted.size() > 0) {
            if(orbObj==null)return null;
            orbObj = orbObj.getSatellite(inverted.remove(0));
        }
        return orbObj;
    }


    //TODO
    public void createPlanetGenSettings(TsonMap settings, Race race, boolean isSatellite){
        //Random random = random();

        settings.put( SNames.BIOMES, biomePlanetCreatorLib.create(race, isSatellite));

        needSave = true;
    }


    public void createOrbitGenSettings(TsonMap settings, Race race, AtomicBoolean hasPortal){
        Random random = random();
        //???
        BiomeOrbit bo = BiomeOrbit.ALL[random.nextInt(BiomeOrbit.ALL.length)];
        settings.put(SNames.BIOME_ID, Biome.getIdForBiome(bo));
        settings.put(SNames.RACE_ID, race.ID);
        int min = random().nextInt(4);
        settings.put(SNames.METEOR_START_CIRCLE, 2+min);
        settings.put(SNames.METEOR_END_CIRCLE, 4+min+random().nextInt(7));
        settings.put(SNames.METEOR_GROUP_ID, race.meteorGroupIds[random.nextInt(race.meteorGroupIds.length)]);
        settings.put(SNames.DENSITY, random.nextFloat());

        boolean hasPortal0 = hasPortal.get();

        if(random.nextInt(3)==1) settings.put(SNames.HAS_STATION, true);
        if(random.nextInt(3)==1) settings.put(SNames.HAS_PORTAL,  hasPortal0 = true);
        if(random.nextInt(3)==1) settings.put(SNames.HAS_ADDITIONAL, true);

        hasPortal.set(hasPortal0);

        needSave = true;
    }
}