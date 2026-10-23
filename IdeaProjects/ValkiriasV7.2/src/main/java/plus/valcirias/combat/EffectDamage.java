package plus.valcirias.combat;


import com.github.magiccheese1.damageindicator.DamageIndicator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EffectDamage {
    public static void summon(Player player, Location target, double damage) {
        try {
            DamageIndicator.Summon(player, target, damage);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
//
//    public static final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
//    public EffectDamage(){
//
//    }
////    PacketContainer p1 = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
////
////        p1.getIntegers().write(0, uid)
////                .write(1, 1);
////        p1.getDoubles().write(0, loc.getX())
////            .write(1, loc.getY())
////            .write(2, loc.getZ());
//    public void Summon(Player player, double damage, boolean crit){
//        Runnable task = () -> {
//            try {
//                Location loc = player.getLocation();
//                loc.setX(FastRandom.r.nextDouble(-1, 1)+loc.getX());
//                loc.setZ(FastRandom.r.nextDouble(-1, 1)+loc.getZ());
//                loc.setY(FastRandom.r.nextDouble(0.3, 1.4)+loc.getY());
//
//                int uid = FastRandom.r.nextInt(256000);
//                PacketContainer p1 = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
//
//                p1.getIntegers().write(0, uid)
//                        .write(1, 1);
//                p1.getDoubles().write(0, loc.getX())
//                    .write(1, loc.getY())
//                    .write(2, loc.getZ());
//                //p1.getStrings().write(0, String.format("damage: %s#.##", damage));
//                pm.sendServerPacket(player, p1);
//
//
//
//
//
//
//                Thread.sleep(1500);
//                PacketContainer s = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
//                List<Integer> l = new ArrayList<>();
//                l.add(uid);
//                s.getIntLists().write(0, l);
//
//                pm.sendServerPacket(player, s);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        };
//        Thread thread = new Thread(task);
//        thread.start();
//    }
//    public void tt(){
//        //PacketContainer p2 = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//
//
//        WrappedDataWatcher watcher = new WrappedDataWatcher();
//        //watcher.setE
//        //watcher.setEntity(stand);
//
//        WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
//        WrappedDataWatcher.Serializer stringSerializer = WrappedDataWatcher.Registry.get(String.class);
//
//        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, stringSerializer), name); // Set custom name
//        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer), true); // Set custom name visible
//
//        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//        packet.getIntegers().write(0, 800);
//        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());


}
