package plus;

import org.bukkit.plugin.java.JavaPlugin;
import plus.Motions.CatScene;
import plus.Motions.DialogScene;
import plus.util.Config.Config;

public class Camera extends JavaPlugin {

    //private ProtocolManager protocolManager;
    public static Camera c;


    public void onEnable() {
        c = this;
        //protocolManager = ProtocolLibrary.getProtocolManager();
        ControlCommands cm = new ControlCommands(this);
        this.getCommand("cam").setExecutor(cm);
        this.getCommand("c_test").setExecutor(new DebugCmd(this));
        this.getCommand("cam").setTabCompleter(cm);

        Config s = new Config(this, "scenes.yml");
        CatScene.setConfig(s);
        DialogScene.setConfig(s);
        Messages.load(new Config(this, "messages.yml"));

    }
}
//        try {
//            player.getEntityId();
//            PacketContainer e = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
//
//            e.getIntegers().write(0, uid);
//            e.getShorts()
//                    .write(0, (short) neqLoc[0])
//                    .write(1, (short) neqLoc[1])
//                    .write(2, (short) neqLoc[2]);
//            e.getBooleans().write(0, false);
//
//            //e.getBytes().write(0,(byte) (yawPitch[0] * 0.7111111f));
//            protocolManager.sendServerPacket(player, e);
//
//            if(yawPitch != null) {
//                e = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
//                e.getBytes().write(0, (byte) (yawPitch[0] * 0.7111111f))
//                        .write(1, (byte) (yawPitch[1] * 0.7111111f));
//                protocolManager.sendServerPacket(player, e);
//            }
//
//        } catch (InvocationTargetException err) {
//            err.printStackTrace();
//        }
//    }//    public void editCamera(Player player, int uid, int[] neqLoc, float[] yawPitch){