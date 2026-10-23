package plus.limbo;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.file.SchematicFile;
import plus.Main;
import plus.utl.Config;
import plus.utl.Location;
import java.io.File;
import java.nio.file.Paths;


public class SchemWorld {


    protected Limbo server;
    protected final String name;
    protected final LimboFactory factory;
    protected LimboListener l;
    //protected final LimboSessionHandler l2;


    public SchemWorld(String name){
        this.name = name;
        factory = (LimboFactory) Main.getServer().getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();
        Reload();
    }


    public Limbo getServer(){
        return server;
    }


    public String getName(){
        return name;
    }


    public void Reload(){
        String path = "phantom_wolrds";
        String pathSchems = path+File.separator+"schems";

        File dir = new File(path);
        if(!dir.exists() || !dir.isDirectory())dir.mkdir();
        dir = new File(pathSchems);
        if(!dir.exists() || !dir.isDirectory())dir.mkdir();

        Config c = new Config(path+ File.separator+name.replace(" ", "_")+".yml");
        this.server = factory.createLimbo(
                buildWorld(
                        c.getOrWrite("world_type", "world"),
                        pathSchems+ File.separator+c.getOrWrite("schematic", null, "file name in [phantom_wolds_schems] (example.schematic)"),
                        new Location(c.getOrWrite("schem_loc", "0;0;0")),
                        new Location(c.getOrWrite("spawn_loc", "0;10;0;90;10"))
                )
        ).setName(c.getOrWrite("world_name", name));
        l = new LimboListener(
                c.getOrWrite("disable_fall", "true").equalsIgnoreCase("true"),
                Integer.parseInt(
                        c.getOrWrite("world_gamemode", "0", "world_gamemode: [allowed only 0, 1, 2, 3]")
                ),
                EmptyListener.I
        );
    }


    public void setListener(LimboSessionHandler handler){
        this.l = new LimboListener(l.df, l.gm, handler);
    }


    public void spawnPlayer(Player player){
        server.spawnPlayer(player, l);
    }


    private Dimension ofId(String id){
        if(id==null)return Dimension.THE_END;
        switch (id){
            case "world":
                return Dimension.OVERWORLD;
            case "nether":
                return Dimension.NETHER;
            default:
                return Dimension.THE_END;
        }
    }


    private VirtualWorld buildWorld(String worldId,String pathToSchematic, Location loc, Location pasteSchem){
        return buildWorld(worldId,pathToSchematic, pasteSchem,loc.X, loc.Y, loc.Z, loc.YAW, loc.PITH);
    }


    private VirtualWorld buildWorld(String worldId,String pathToSchematic, Location pm, int x, int y, int z, float yaw, float pith){
        VirtualWorld w = factory.createVirtualWorld(ofId(worldId), x, y, z, yaw, pith);
        if(pathToSchematic != null) {
            try {
                SchematicFile file = new SchematicFile(Paths.get(pathToSchematic));
                file.toWorld(factory, w, pm.X, pm.Y, pm.Z);
            } catch (Exception e){
                System.out.printf("Schematic form file [%s] is not exist!%n", pathToSchematic);
            }
        }
        return w;
    }
}