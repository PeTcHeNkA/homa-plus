package plus.space.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.net.INetMapProvider;
import plus.space.client.net.SolarMapClientProvider;
import plus.space.common.GalName;
import plus.space.common.SNames;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.utl.Tuple;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.function.Consumer;


@SideOnly(Side.CLIENT)
public class SolarMap {
    public final INetMapProvider provider;
    Star[] stars;
    int checkSize = Integer.MIN_VALUE;
    private boolean updated = false;
    private long lastUpdateTime = Long.MIN_VALUE;

    public static SolarMap INST;

    public static void init(){
        INST = new SolarMap(new SolarMapClientProvider.Provider());
    }

    public SolarMap(INetMapProvider provider) {
        this.provider = provider;
    }


    public void getSystems(Consumer<Star[]> callBack){
        if(lastUpdateTime+900_000<System.currentTimeMillis()){
            provider.getActualSizeFromServer(integer -> {
                int check = integer;
                if(check != checkSize){
                    checkSize = check;
                    loadSystems(() -> {
                        lastUpdateTime = System.currentTimeMillis();
                        callBack.accept(stars);
                    });
                }
            });
        } else {
            callBack.accept(stars);
        }
    }


    public void getPlanets(GalName name, Consumer<Tuple<MapObj, Planet[]>> callBack){
        ArrayList<String> inverted = new ArrayList<>();
        while (name != null){
            inverted.add(0,name.getSelfName());
            name = name.getParent();
        }
        MapObj[] planet = {getStar(inverted.remove(0))};

        if (inverted.size()>0){
            planet[0].getPlanets(new Consumer<Planet[]>() {
                @Override
                public void accept(Planet[] planets) {
                    if(inverted.size()==0){
                        callBack.accept(new Tuple<>(planet[0], planets));
                    } else {
                        MapObj prev = planet[0];
                        String name = inverted.remove(0);
                        planet[0] = prev.getPlanet(name);
                        if(planet[0] == null){
                            System.out.println("OBJECT: "+prev +" not contains ["+name+"]");
                            callBack.accept(new Tuple<>(prev, new Planet[]{}));
                            return;
                        }
                        planet[0].getPlanets(this);
                    }
                }
            });
        } else {
            planet[0].getPlanets(planets -> callBack.accept(new Tuple<>(planet[0], planets)));
        }
    }


    public Star getStar(String name){
        for(Star star:stars) if(star.name.getSelfName().equals(name))return star;
        return null;
    }


    public void getStar(String name, Consumer<Star> callback){
        getSystems(stars -> {
            for(Star star:stars) if(star.name.getSelfName().equals(name)){
                callback.accept(star);
                return;
            }
            callback.accept(null);
        });
    }


    public void markUsed(){
        updated = false;
    }


    void loadSystems(Runnable callBack){
        provider.getSystemsFromServer(list -> {
            stars = new Star[list.size()];
            for(int i=0;i<stars.length;i++){
                stars[i] = new Star(list.getMap(i));
            }
            updated = true;
            callBack.run();
        });
    }


//    public long getTimeStamp(){
//        return System.currentTimeMillis();
//    }


    private Planet[] readPlanets(GalName name, TsonList list){
        Planet[] planets = new Planet[list.size()];
        for(int i=0;i< planets.length;i++)
            planets[i] = new Planet(name, list.getMap(i));
        return planets;
    }


    public static abstract class MapObj{
        public final GalName name;
        public final int textureId, int1;
        public final float scale;
        public final boolean bool1;

        private MapObj(GalName parent, TsonMap map){
            name = new GalName(map.getStr(SNames.NAME), parent);
            textureId = map.getInt(SNames.TEXTURE);
            scale = map.getFloat(SNames.SIZE);
            int1 = map.getOrDefaultInt(SNames.INT_1, 0);
            bool1 = map.getOrDefaultBool(SNames.BOOL_1, false);
        }
        public abstract void getPlanets(Consumer<Planet[]> callBack);
        public abstract Planet getPlanet(String name);
        public abstract int getCountPlanets();
        public abstract int type();
    }


    public final class Star extends MapObj{
        public final int xPos, yPos, zPos;
        Planet[] planets;

        Star(TsonMap map) {
            super(null, map);
            xPos = map.getInt(SNames.POS_X);
            yPos = map.getInt(SNames.POS_Y);
            zPos = map.getInt(SNames.POS_Z);
        }

        //star
        public void getPlanets(Consumer<Planet[]> callBack){
            if(planets==null) {
                planets = new Planet[0];
                provider.getPlanetFromServer(name, list ->
                        callBack.accept(planets = readPlanets(name, list))
                );
                updated = true;
            } else {
                callBack.accept(planets);
            }
        }


        public Planet getPlanet(String name){
            for(Planet planet:planets) if(planet.name.getSelfName().equals(name))return planet;
            return null;
        }


        @Override
        public int getCountPlanets() {
            return planets==null?int1:planets.length;
        }

        @Override
        public int type() {
            return -1;
        }
    }


    public final class Planet extends MapObj{
        public final int orbit;
        public final int type;
        public final boolean bool2;
        Planet[] satellites;

        Planet(GalName parent, TsonMap map) {
            super(parent, map);
            orbit = map.getInt(SNames.DISTANCE);
            type = map.getOrDefaultInt(SNames.TYPE, 0);
            bool2 = map.getOrDefaultBool(SNames.BOOL_2, false);
            map.ifContainsList(SNames.ORBIT,(list -> satellites = readPlanets(name, list)));
        }


        public void getPlanets(Consumer<Planet[]> callBack){
            if(satellites==null) {
                satellites = new Planet[0];
                provider.getPlanetFromServer(name, list ->
                        callBack.accept(satellites = readPlanets(name, list))
                );
                updated = true;
            } else {
                callBack.accept(satellites);
            }
        }


        public Planet getPlanet(String name){
            for(Planet planet:satellites) if(planet.name.getSelfName().equals(name))return planet;
            return null;
        }


        @Override
        public int getCountPlanets() {
            return satellites==null?int1:satellites.length;
        }


        @Override
        public int type() {
            return type;
        }


        @Override
        public String toString() {
            return "(N:"+name+",O:"+orbit+",T:"+type+",S:"+Arrays.toString(satellites)+")";
        }
    }
}