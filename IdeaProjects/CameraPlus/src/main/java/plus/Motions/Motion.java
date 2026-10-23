package plus.Motions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Camera;
import plus.Motions.parser.LinearParser;
import plus.Motions.parser.MapParser;
import plus.Motions.parser.Parser;
import plus.Motions.parser.MotionContainer;
import plus.util.Chat.ChatParser;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


public final class Motion {
    protected static final ProtocolManager pm = ProtocolLibrary.getProtocolManager();

    private final MotionContainer container;
    private final double[] startPos;
    private boolean hasAnimation = false;
    private String animation = null;
    private double speedAnimation = 0;

    private final CatScene c;


    private DarkEffect effect;


    public void addDarkEffect(int time, int power){
        effect = new DarkEffect(time, power);
    }


//    public Motion(CatScene c,String t, List<String> data, String rawStartPos, String rawLookTarget,int time, boolean Invert){
//        this(c, t, data, CordMath.parseCords(rawStartPos), CordMath.parseCords(rawLookTarget), time, Invert);
//    }


    public Motion(CatScene c,String t, List<String> data, double[] startPos, double[] lookTarget,int time, boolean Invert){
        this.c = c;
        this.startPos = startPos;

        Parser parser = null;
        if(t.equalsIgnoreCase("LINEAR")){
            parser = new LinearParser(data, startPos, lookTarget, time);
        }
        if(t.equalsIgnoreCase("MAP")){
            parser = new MapParser(data, startPos, lookTarget, time);
        }
        parser.setInvertLook(Invert);
        container = parser.parse();
    }


    public void setAnimation(String animation, double speed){
        hasAnimation = true;
        this.animation = animation;
        speedAnimation = speed;
    }


    public void addMessages(Integer index, List<String> rawMessage){
        container.Messages.put(index, ChatParser.Parse(rawMessage));
    }


    public void run(List<Player> player, int CameraUid, int speed){
        if(effect != null){
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(Player pl:player) {
                        effect.apply(pl);
                    }
                }
            }.runTaskLater(Camera.c, 0);
        }
        boolean runText = !container.Messages.isEmpty();

        try {
            teleportCameraToStart(player, CameraUid, startPos, container.Cameras.get(0));
            if(hasAnimation){
                c.character.playAnimation(animation, speedAnimation);
            }

            if(runText) {
                for (int i = 1; i < container.Cameras.size(); i++) {
                    editCamera(
                            player,
                            CameraUid,
                            container.Motions.get(i - 1),
                            container.Cameras.get(i)
                    );
                    if (container.Messages.containsKey(i)) {
                        runMessages(player, container.Messages.get(i));
                    }
                    Thread.sleep(speed);
                }
            } else {
                for (int i = 1; i < container.Cameras.size(); i++) {
                    editCamera(
                            player,
                            CameraUid,
                            container.Motions.get(i - 1),
                            container.Cameras.get(i)
                    );
                    Thread.sleep(speed);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(Player player, int CameraUid, int speed){
        if(effect != null){
            new BukkitRunnable() {
                @Override
                public void run() {
                    effect.apply(player);
                }
            }.runTaskLater(Camera.c, 0);
        }
        //if(container == null || container.isEmpty())container = parser.parse();
        boolean runText = !container.Messages.isEmpty();

        if(hasAnimation){
            c.character.playAnimation(animation, speedAnimation);
        }

        try {
            teleportCameraToStart(player, CameraUid, startPos, container.Cameras.get(0));
            if(runText) {
                for (int i = 1; i < container.Cameras.size(); i++) {
                    editCamera(
                            player,
                            CameraUid,
                            container.Motions.get(i - 1),
                            container.Cameras.get(i)
                    );
                    if (container.Messages.containsKey(i)) {
                        runMessages(player, container.Messages.get(i));
                    }
                    Thread.sleep(speed);
                }
            } else {
                for (int i = 1; i < container.Cameras.size(); i++) {
                    editCamera(
                            player,
                            CameraUid,
                            container.Motions.get(i - 1),
                            container.Cameras.get(i)
                    );
                    Thread.sleep(speed);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    protected static void runMessages(Player player, TextComponent[] msg){
        player.spigot().sendMessage(msg);
    }


    private void runMessages(List<Player> players, TextComponent[] msg){
        for(Player player:players) {
            player.spigot().sendMessage(msg);
        }
    }


    private void teleportCameraToStart(List<Player> players, int uid, double[] motion, byte[] yawPitch){
        PacketContainer p1 = buildTeleportPacket(uid, motion, yawPitch[0]);
        PacketContainer p2 = buildYawPacket(uid, yawPitch[1]);
        try {
            for(Player player:players) {
                pm.sendServerPacket(player, p1);
                pm.sendServerPacket(player, p2);
            }
        } catch (InvocationTargetException err) {
            err.printStackTrace();
        }
    }


    protected static void teleportCameraToStart(Player player, int uid, double[] motion, byte[] yawPitch){
        PacketContainer p1 = buildTeleportPacket(uid, motion, yawPitch[0]);
        PacketContainer p2 = buildYawPacket(uid, yawPitch[1]);
        try {
            pm.sendServerPacket(player, p1);
            pm.sendServerPacket(player, p2);
        } catch (InvocationTargetException err) {
            err.printStackTrace();
        }
    }


    private void editCamera(List<Player> players, int uid, short[] motion, byte[] yawPitch){
        PacketContainer p1 = buildPacketMove(uid, motion, yawPitch[0]);
        PacketContainer p2 = buildYawPacket(uid, yawPitch[1]);
        try {
            for(Player player:players) {
                pm.sendServerPacket(player, p1);
                pm.sendServerPacket(player, p2);
            }
        } catch (InvocationTargetException err) {
            err.printStackTrace();
        }
    }


    protected static void editCamera(Player player, int uid, short[] motion, byte[] yawPitch){
        PacketContainer p1 = buildPacketMove(uid, motion, yawPitch[0]);
        PacketContainer p2 = buildYawPacket(uid, yawPitch[1]);
        try {
            pm.sendServerPacket(player, p1);
            pm.sendServerPacket(player, p2);
        } catch (InvocationTargetException err) {
            err.printStackTrace();
        }
    }


    private static PacketContainer buildPacketMove(int uid, short[] motion, byte Pitch){
        PacketContainer e = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

        e.getIntegers().write(0, uid);
        e.getShorts()
                .write(0, motion[0])
                .write(1, motion[1])
                .write(2, motion[2]);

        e.getBytes().write(1, Pitch );
        e.getBooleans().write(0, true);
        return e;
    }


    private static PacketContainer buildYawPacket(int uid,byte yaw){
        PacketContainer e = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        e.getIntegers().write(0, uid);
        e.getBytes().write(0, yaw);
        return e;
    }


    private static PacketContainer buildTeleportPacket(int uid, double[] motion, byte Pith){
        PacketContainer e = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        e.getIntegers().write(0, uid);
        e.getDoubles()
                .write(0, motion[0])
                .write(1, motion[1])
                .write(2, motion[2]);

        e.getBytes().write(1, Pith );
        e.getBooleans().write(0, true);

        return e;
    }
}