package plus.space.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import plus.space.common.net.CallBackMsg;
import plus.space.common.GalName;
import plus.space.common.SNames;
import plus.space.server.world.SolarTeleporter;
import plus.space.server.world.SolarWorld;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class SolarMapServerProvider implements IMessageHandler<CallBackMsg,
        CallBackMsg> {
    private static final HashMap<String,String> cache = new HashMap<>();
    private static SolarRepository repo;
    int checkSize = -1;


    public static void init(SimpleNetworkWrapper network, int id){
        network.registerMessage(SolarMapServerProvider.class, CallBackMsg.class, id, Side.SERVER);
    }


    public static void init(SolarRepository repository){
        repo = repository;
    }


    private void scanModel(){
        int check = repo.starLib.countUsages() + repo.meteorBeltLib.countUsages()+
                repo.stationLib.countUsages()+ repo.planetLib.countUsages();
        if(check!=checkSize){
            cache.clear();
        }
    }


    private String getSystemsFromRepo(){
        TsonList list = new TsonList(repo.systems.size());
        Map<String, ISolarSystem> map = repo.systems;
        for(String name:map.keySet().toArray(new String[0])){
            ISolarSystem system = map.get(name);
            TsonMap systemModel = new TsonMap();
            systemModel.put( SNames.NAME,    system.getName()      );
            systemModel.put( SNames.POS_X,   system.getX()         );
            systemModel.put( SNames.POS_Y,   system.getY()         );
            systemModel.put( SNames.POS_Z,   system.getZ()         );
            systemModel.put( SNames.TEXTURE, system.getTextureId() );
            systemModel.put( SNames.SIZE,    system.getSize()      );
            if(system instanceof SolarSystem){
                systemModel.put( SNames.INT_1, system.getSatellites().size());
            }
            list.add(systemModel);
        }
        return list.toString();
    }


    private String getPlanetsFromRepo(GalName name){
        //repo.getSystem().getP
        if(name.getParent()==null){
            ISolarSystem system = repo.getRealSystem(name.getSelfName());
            if(system==null)return null;
            TsonMap model = new TsonMap();
            addSettalitesToModel(model, system.getSatellites());
            return model.toString();
        } else {
            SolarPlanet planet = (SolarPlanet) repo.getObj(name);
            if (planet == null) return null;
            TsonMap model = spToModelPlus(planet);
            return model.toString();
        }
    }


    private void addSettalitesToModel(TsonMap model, Collection<SolarPlanet> stls){
        if(stls.size()>0){
            TsonList list = new TsonList();
            model.put(SNames.ORBIT, list);
            for(SolarPlanet planet1:stls){
                list.add(spToModel(planet1));
            }
        }
    }
//
//
//    private TsonMap starToModelPlus(SolarPlanet planet){/
//        TsonMap model = new TsonMap();
//        addSettalitesToModel(model, planet.getSatellites());
//        return model;
//    }


    private TsonMap spToModelPlus(SolarPlanet planet){
        TsonMap model = spToModel(planet);
        addSettalitesToModel(model, planet.getSatellites());
        return model;
    }


    private TsonMap spToModel(SolarPlanet planet){
        TsonMap model = new TsonMap();
        model.put(SNames.NAME,     planet.getName()      );
        model.put(SNames.DISTANCE, planet.getOrbit()     );
        model.put(SNames.TEXTURE,  planet.getTextureId() );
        model.put(SNames.SIZE,     planet.size           );

        if(planet.hasOxygen())
            model.put(SNames.BOOL_1, true);
        if(planet.planet.type != SolarRepository.Type.PLANET){
            model.put(SNames.TYPE, planet.planet.type.ID);
        } else {
            int stsize = planet.getSatellites().size();
            if(stsize>0) model.put(SNames.INT_1, stsize);
        }
        return model;
    }


    public String getPlanetFromServer(GalName name) {
        String planet = cache.get(name.getSelfName());
        if(planet==null)cache.put(name.getSelfName(), planet = getSystemsFromRepo());
        return planet;
    }


    public String getSystemsFromServer() {
        String systems = cache.get(SNames.SYSTEMS);
        if(systems==null)cache.put(SNames.SYSTEMS, systems = getSystemsFromRepo());
        return systems;
    }


    public int getActualSizeFromServer() {
        return checkSize;
    }


    @Override
    public CallBackMsg onMessage(CallBackMsg msg, MessageContext ctx) {
        try {
            switch (msg.getMtd()) {
                case 0: return msg.reply(getSystemsFromRepo());
                case 1: return msg.reply(getPlanetsFromRepo(GalName.ofStr(msg.getData())));
                case 2: return msg.reply(Integer.toString(getActualSizeFromServer()));
                case 4: return msg.reply(Integer.toString(flyTo(GalName.ofStr(msg.getData()), ctx.getServerHandler().player)));
            }
        } catch (Exception e){}
        return null;
    }


    //TODO
    private int flyTo(GalName name, EntityPlayerMP player){
        SolarWorld worldFrom = null;
        ISolarOrbitObj to = repo.getObj(name);
        if(player.world instanceof SolarWorld){
            //FROM
            worldFrom = (SolarWorld) player.world;
        }
        player.changeDimension(to.getDimensionId(), new SolarTeleporter(worldFrom));
        return 1;
    }
}





