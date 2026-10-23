package plus.util.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import plus.quest.motion.BasicPos;
import plus.quest.motion.Motion;
import plus.quest.motion.TpMotion;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PLManager implements PacketManager{

    public final ProtocolManager pm = ProtocolLibrary.getProtocolManager();


    private PacketContainer[] createCameraPacket(int uid, BasicPos loc){
        PacketContainer p1 = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        p1.getIntegers().write(0, uid)
                .write(1, 1);
        p1.getDoubles().write(0, loc.X)
                .write(1, loc.Y)
                .write(2, loc.Z);

        PacketContainer p2 = new PacketContainer(PacketType.Play.Server.CAMERA);
        p2.getIntegers().write(0, uid);
        return new PacketContainer[]{p1, p2};
    }
    
    
    @Override
    public void createCamera(Collection<Player> players, int uid, BasicPos loc) {
        PacketContainer[] p = createCameraPacket(uid, loc);
        for(Player pl:players){
            try {
                pm.sendServerPacket(pl, p[0]);
                pm.sendServerPacket(pl, p[1]);
            } catch (InvocationTargetException ignored) {}
        }
    }


    @Override
    public void createCamera(Player player, int uid, BasicPos loc) {
        PacketContainer[] p = createCameraPacket(uid, loc);
        try {
            pm.sendServerPacket(player, p[0]);
            pm.sendServerPacket(player, p[1]);
        } catch (InvocationTargetException ignored) {}
    }


    private PacketContainer buildPacketMove(int uid, Motion m){
        PacketContainer e = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

        e.getIntegers().write(0, uid);
        e.getShorts()
                .write(0, m.offsetX)
                .write(1, m.offsetY)
                .write(2, m.offsetZ);

        e.getBytes().write(1, m.yaw );
        e.getBooleans().write(0, true);
        return e;
    }


    private PacketContainer buildPithPacket(int uid, byte pith){
        PacketContainer e = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        e.getIntegers().write(0, uid);
        e.getBytes().write(0, pith);
        return e;
    }


    @Override
    public void editCamera(Collection<Player> players, int cameraUid, Motion m) {
        PacketContainer p1 = buildPacketMove(cameraUid, m);
        PacketContainer p2 = buildPithPacket(cameraUid, m.pith);
        for(Player pl:players){
            try {
                pm.sendServerPacket(pl, p1);
                pm.sendServerPacket(pl, p2);
            } catch (InvocationTargetException ignored) {}
        }
    }


    @Override
    public void editCamera(Player player, int cameraUid, Motion m) {
        try {
            pm.sendServerPacket(player, buildPacketMove(cameraUid, m));
            pm.sendServerPacket(player, buildPithPacket(cameraUid, m.pith));
        } catch (InvocationTargetException ignored) {}
    }


    private PacketContainer buildTeleportPacket(int uid, BasicPos m){
        PacketContainer e = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        e.getIntegers().write(0, uid);
        e.getDoubles()
                .write(0, m.X)
                .write(1, m.Y)
                .write(2, m.Z);
        if(m instanceof TpMotion mt) e.getBytes().write(1, mt.yaw);
        e.getBooleans().write(0, true);
        return e;
    }


    @Override
    public void teleportCamera(Player player, int cameraUid, BasicPos m) {
        try {
            pm.sendServerPacket(player, buildTeleportPacket(cameraUid, m));
            if(m instanceof TpMotion tm) pm.sendServerPacket(player, buildPithPacket(cameraUid, tm.pith));
        } catch (InvocationTargetException ignored) {}
    }


    @Override
    public void teleportCamera(Collection<Player> players, int cameraUid, BasicPos m) {
        PacketContainer p1 = buildTeleportPacket(cameraUid, m);
        if(m instanceof TpMotion mt) {
            PacketContainer p2 = buildPithPacket(cameraUid, mt.pith);
            for (Player pl : players) {
                try {
                    pm.sendServerPacket(pl, p1);
                    pm.sendServerPacket(pl, p2);
                } catch (InvocationTargetException ignored) {}
            }
        } else {
            for (Player pl : players) {
                try {
                    pm.sendServerPacket(pl, p1);
                } catch (InvocationTargetException ignored) {}
            }
        }
    }


    @Override
    public void setCamera(Player player, int uid) {
        try {
            pm.sendServerPacket(player, buildSetCameraPacket(uid));
        } catch (InvocationTargetException ignored) {}
    }


    @Override
    public void setCamera(Collection<Player> players, int uid) {
        PacketContainer p1 = buildSetCameraPacket(uid);
        for(Player pl:players){
            try {
                pm.sendServerPacket(pl, p1);
            } catch (InvocationTargetException ignored) {}
        }
    }


    private PacketContainer buildSetCameraPacket(int entityUID){
        PacketContainer cam = new PacketContainer(PacketType.Play.Server.CAMERA);
        cam.getIntegers().write(0, entityUID);
        return cam;
    }


    private PacketContainer buildRemoveCameraPacket(int uid){
        PacketContainer s = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        List<Integer> l = new ArrayList<>();
        l.add(uid);
        s.getIntLists().write(0, l);
        return s;
    }


    @Override
    public void removeCamera(Player player, int uid) {
        try {
            pm.sendServerPacket(player, buildSetCameraPacket(player.getEntityId()));
            pm.sendServerPacket(player, buildRemoveCameraPacket(uid));
        } catch (InvocationTargetException ignored) {}
    }


    @Override
    public void removeCamera(Collection<Player> players, int uid) {
        PacketContainer p1 = buildRemoveCameraPacket(uid);
        for(Player player:players) {
            try {
                pm.sendServerPacket(player, buildSetCameraPacket(player.getEntityId()));
                pm.sendServerPacket(player, p1);
            } catch (InvocationTargetException ignored) {}
        }
    }
}