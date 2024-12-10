package plus.fserver.api.offline;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import java.util.UUID;


public class OfflinePlayerMPAccess extends EntityPlayerMP {
    private OfflinePlayerMPAccess(MinecraftServer server, WorldServer world, GameProfile profile, PlayerInteractionManager manager) {
        super(server, world, profile, manager);
    }


    public static OfflinePlayerMPAccess create(MinecraftServer server, UUID uuid){
        GameProfile gm = server.getPlayerProfileCache().getProfileByUUID(uuid);
        if(gm == null) gm = new GameProfile(uuid, uuid.toString());

        WorldServer world = server.getWorld(0);

        PlayerInteractionManager pmgr = new PlayerInteractionManager(world);

        return new OfflinePlayerMPAccess(server, world, gm, pmgr);
    }


    public void lock(){
        server.getPlayerList().lock(super.getUniqueID());
    }


    public boolean load(){
        NBTTagCompound data = getServerWorld().getMinecraftServer().getPlayerList().readPlayerDataFromFile(this);
        setWorld(this.server.getWorld(dimension));
        return data != null;
    }


    @Override
    protected void loadAdvancementsOnInit() {}


    public void loadAdvancements(){
        super.loadAdvancementsOnInit();
    }


    public void save(){
        server.getPlayerList().writePlayerData(this);
    }


    public void unlock(){
        server.getPlayerList().unlock(super.getUniqueID());
    }
}