package plus.fserver.server;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.server.command.TextComponentHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.fserver.ElcaConfig;
import plus.fserver.api.ServerCommand;
import plus.fserver.plugin.SSPatchLoader;
import plus.fserver.utls.EntityLodFunc;
import plus.fserver.utls.collections.ArrayListE6;
import plus.fserver.utls.collections.ArrayListEV;
import plus.tson.TsonFile;
import plus.tson.utl.ByteStrBuilder;
import plus.tson.utl.Te4HashSet;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.util.*;


public class ElcaCommands extends ServerCommand {
    private static final String primary  = "&b";
    private static final String critical = "&4";
    private static final String danger   = "&c";
    private static final String warning  = "&e";
    private static final String normal   = "&a";

    private final List<String> completions = ImmutableList.copyOf(
        new String[]{"version", "info", "tps", "mobs", "spawn", "hotpoints"}
    );
    private final DecimalFormat TIME_FORMATTER = new DecimalFormat("########0.00");
    private static final ElcaCommands cmds = new ElcaCommands();
    public static void init(){}

    static {
        cmds.register();
    }

    private ElcaCommands() {
        super("elca", "/elca <args>", 4);
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!sender.canUseCommand(4, "elca"))return;
        if(args.length==0){
            version(server, sender);
            return;
        }
        String arg0 = args[0];
        if(arg0 == null || arg0.isEmpty()){
            version(server, sender);
            return;
        }
        if(arg0.equalsIgnoreCase("tps")){
            tps(server, sender, args.length>1 && "true".equals(args[1]));return;
        }
        if(arg0.equalsIgnoreCase("info")){
            info(server, sender);
        }
        if(arg0.equalsIgnoreCase("cvi")){
            chunkViTxt(server, sender);
        }
        if(arg0.equalsIgnoreCase("mobs")){
            if(args.length>1){
                int dim = Integer.parseInt(args[1].trim());
                mobs(server, sender, dim);
            } else {
                mobs(server, sender);
            }
        }
        if(arg0.equalsIgnoreCase("soft")){
            soft(server, sender, args);
        }
        if(arg0.equalsIgnoreCase("spawn")){
            spawn(sender, args);
        }
        if(arg0.equalsIgnoreCase("hotpoints")){
            hotPoints(server, sender, args);
        }
    }


    private void hotPoints(MinecraftServer server, ICommandSender sender, String[] args){
        Te4HashSet<EntityPlayerMP> playerSet = new Te4HashSet<>();
        class Part implements Comparable<Part>{
            final Chunk chunk;
            final byte shift;
            final short count;

            Part(Chunk chunk, byte shift, int count){
                this.chunk = chunk;
                this.shift = shift;
                this.count = (short) count;
            }
            @Override
            public int compareTo(@NotNull Part o) {
                return Short.compare(o.count, count);
            }
            public List<EntityPlayerMP> findCauses(){
                WorldServer world = (WorldServer) chunk.getWorld();
                PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(chunk.x, chunk.z);
                if(entry == null)return Collections.emptyList();
                return entry.getWatchingPlayers();
            }
            public void display(){
                int x = chunk.x * 16 + 8;
                int y = shift * 16 + 8;
                int z = chunk.z * 16 + 8;

                List<EntityPlayerMP> causes = findCauses();

                MobInfoList mobs = new MobInfoList();

                for (Entity entity:chunk.getEntityLists()[shift].rawValues()){
                    mobs.add(entity);
                }

                String inf = "  &ePos: x:"+x+", y:"+y+", z: "+z+ ", count:"+count;

                if(!causes.isEmpty()){
                    for (EntityPlayerMP player:causes){
                        if(!playerSet.add(player)){
                            inf += ", "+player.getName();
                        } else {
                            BlockPos pos = player.getPosition();
                            inf += ", "+player.getName()
                                    + "["+pos.getX()+", "+pos.getY()+", "+pos.getZ()+"]";
                        }
                    }
                }
                reply(sender, inf);
                mobs.display(sender, "    ");
            }
        }
        World defWorld = sender.getEntityWorld();

        class WorldInfo implements Comparable<WorldInfo>{
            final ArrayListEV<Part> parts = new ArrayListEV<>(32);
            int sumParts = 0;
            final WorldServer world;
            boolean sorted = false;

            WorldInfo(WorldServer world) {
                this.world = world;
            }

            public void compute(){
                Chunk[] arr = world.getChunkProvider().getLoadedChunks().toArray(new Chunk[0]);
                for (Chunk chunk:arr){
                    add(chunk);
                }
            }

            public void add(Chunk chunk){
                sorted = false;
                byte index = 0;
                for (ClassInheritanceMultiMap<Entity> part:chunk.getEntityLists()){
                    int count = part.rawValues().sizeC();
                    if(count > 3){
                        parts.addF(new Part(chunk, index, count));
                    }
                    ++index;
                }
            }

            private void sortIfNeed(){
                if(!sorted){
                    parts.sort(Part::compareTo);
                    sumParts = 0;
                    for (Part part:parts){
                        sumParts += part.count;
                    }
                    sorted = true;
                }
            }

            @Override
            public int compareTo(@NotNull WorldInfo o) {
                sortIfNeed();
                o.sortIfNeed();
                if(o.world == defWorld)return 1;
                return Integer.compare(o.sumParts, sumParts);
            }

            public boolean display(int lim){
                if(parts.isEmptyC())return false;
                sortIfNeed();
                boolean flag = true;
                for (Part part:parts){
                    if(lim-- == 0)break;
                    if(flag){
                        sendWorldInfo(world, sender);
                        flag = false;
                    }
                    part.display();
                }
                return !flag;
            }
        }

        boolean onlyOne = true;
        int limit = 10;
        if(args.length > 1){
            if(args[1].equals("all")){
                onlyOne = false;
                if(args.length > 2){
                    limit = Integer.parseInt(args[2]);
                }
            }
            else if(args[1].equals("one")){
                if(args.length > 2){
                    limit = Integer.parseInt(args[2]);
                }
            }
        }

        if(onlyOne){
            WorldInfo inf = new WorldInfo((WorldServer) defWorld);
            inf.compute();
            if(!inf.display(limit)){
                reply(sender, normal + "World ["+defWorld.provider.getDimension()+"] has`nt slow entity sectors");
            }
        } else {

            ArrayListE6<WorldInfo> infs = new ArrayListE6<>();

            for (Integer dimId : DimensionManager.getIDs()) {
                WorldServer world = DimensionManager.getWorld(dimId);
                WorldInfo inf = new WorldInfo(world);
                infs.addF(inf);

                inf.compute();
            }

            infs.sort(WorldInfo::compareTo);
            boolean flag = false;

            for (WorldInfo inf : infs) {
                if(inf.display(limit))flag = true;
            }

            if (!flag) {
                reply(sender, normal + "Server has`nt slow entity sectors");
            }
        }
    }


    private void spawn(ICommandSender sender, String[] args) throws NumberInvalidException {
        if(args.length < 8){
            //   /elca spawn zombie ~ ~ ~ ~50 ~ ~50 0.7
            //   /elca spawn villager_golem ~ ~ ~ ~50 ~ ~50 1.5
            reply(sender, "&e/spawn [ID] [X] [Y] [Z] [X2] [Y2] [Z2] [STEP]");
            return;
        }

        ResourceLocation loc = new ResourceLocation(args[1]);
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(loc);

        if(entry == null){
            reply(sender, "&dEntity ID ["+args[1]+"] is invalid");
            return;
        }
        Vec3d from = posOf(sender, args, 2);
        Vec3d to   = posOf(sender, args, 5);
        double step = args.length > 8? CommandBase.parseDouble(args[8]):1;

        double xMin = Math.min(from.x, to.x);
        double xMax = Math.max(from.x, to.x);
        double yMin = Math.min(from.y, to.y);
        double yMax = Math.max(from.y, to.y);
        double zMin = Math.min(from.z, to.z);
        double zMax = Math.max(from.z, to.z);

        World world = sender.getEntityWorld();
        DifficultyInstance diff = world.getDifficultyForLocation(new BlockPos(xMin, yMin, zMin));

        boolean init = EntityLiving.class.isAssignableFrom(entry.getEntityClass());

        int count = 0;

        if(yMin >= yMax){
            yMax += step*0.5;
        }

        for(double x = xMin; x <= xMax; x += step){
            for (double z = zMin; z <= zMax; z += step) {
                for (double y = yMin; y <= yMax; y += step) {
                    Entity entity = entry.newInstance(world);
                    entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
                    if(world.spawnEntity(entity)){
                        if(init) {
                            ((EntityLiving) entity).onInitialSpawn(diff, null);
                        }
                        ++count;
                    }
                }
            }
        }
        reply(sender,  normal+"Successful spawned " + count + " entities");
    }


    private Vec3d posOf(ICommandSender sender, String[] args, int index) throws NumberInvalidException {
        Vec3d vec = sender.getPositionVector();

        double x = CommandBase.parseDouble(vec.x, args[index+0],  true);
        double y = CommandBase.parseDouble(vec.y, args[index+1], false);
        double z = CommandBase.parseDouble(vec.z, args[index+2], true);

        return new Vec3d(x, y, z);
    }


    private void soft(MinecraftServer server, ICommandSender sender, String[] args){
        NetworkSystem networkSystem = server.getNetworkSystem();
        if(args.length > 1){
            if(!networkSystem.hasFirewall()){
                reply(sender, "Firewall not exists");
                return;
            }
            boolean flag = false;
            String sub = args[1];
            switch (sub){
                case "enable":
                    networkSystem.setFireWallEnabled(true);
                    networkSystem.fireWall().softSetAll(server, ElcaConfig.DEFAULT_SOFT_TIME, true);
                    break;
                case "disable":
                    networkSystem.setFireWallEnabled(false);
                    break;
                case "log":{
                    if(args.length > 2){
                        String sub2 = args[2];
                        switch (sub2){
                            case "e":
                            case "enable":
                                networkSystem.fireWall().setLog(true);
                                break;
                            case "d":
                            case "disable":
                                networkSystem.fireWall().setLog(false);
                                break;
                        }
                    }
                    reply(sender, "Invalid args. Use /elca soft log 'enable/disable'");
                    return;
                }
                case "uset":
                    flag = true;
                case "set":{
                    boolean allow;
                    InetAddress addr = null;
                    long time;
                    l1: if(args.length > 3){
                        String arg2 = args[2];
                        String arg3 = args[3];
                        switch (arg3){
                            case "a":
                            case "allow":
                                allow = true;
                                break;
                            case "d":
                            case "deny":
                                allow = false;
                                break;
                            default:
                                break l1;
                        }
                        try {
                            if(args.length > 4){
                                time = Long.parseLong(args[4]);
                            } else {
                                time = ElcaConfig.DEFAULT_SOFT_TIME;
                            }
                            l2: if(flag){
                                ArrayList<EntityPlayerMP> players = new ArrayList<>(server.getPlayerList().getPlayers());
                                for (EntityPlayerMP player : players) {
                                    if(player.getName().equalsIgnoreCase(arg2)){
                                        SocketAddress sock = player.connection.netManager.channel().remoteAddress();
                                        addr = ((InetSocketAddress)sock).getAddress();
                                        break l2;
                                    }
                                }
                                reply(sender, "Player [%s] not founded", arg2);
                            } else {
                                addr = InetAddress.getByName(arg2);
                            }
                        } catch (Exception e){
                            reply(sender, "Critical error, see console");
                            e.printStackTrace();
                            return;
                        }

                        networkSystem.fireWall().softSet(addr, allow);

                        reply(sender, "Set [%s] to [%s] on [%s]"
                                , addr, allow ? "allow" : "deny",
                                new Date(System.currentTimeMillis() + time)
                        );
                    }
                    reply(sender, "Invalid args. Use /elca soft set 'ip' 'allow/deny' 'time'");
                    return;
                }
            }
        } else {
            reply(sender, "Firewall: %s, enabled: %s",
                    networkSystem.hasFirewall(), networkSystem.fireWallEnabled()
            );
        }
    }


    private void chunkViTxt(MinecraftServer server, ICommandSender sender){
        ChunkProviderServer ch = (ChunkProviderServer) sender.getEntityWorld().getChunkProvider();
        int minX = 0, minZ = 0;
        int maxX = 0, maxZ = 0;
        for (Chunk c:ch.loadedChunks.values()){
            if(c.x < minX)minX = c.x;
            if(c.z < minZ)minZ = c.z;

            if(c.x > maxX)maxX = c.x;
            if(c.z > maxZ)maxZ = c.z;
        }
        byte[][] table = new byte[maxX-minX+1][maxZ-minZ+1];

        for (Chunk c:ch.loadedChunks.values()){
            table[c.x-minX][c.z-minZ] = (byte) (c.maybeUnload()?1:2);
        }

        ByteStrBuilder builder = new ByteStrBuilder(((table.length+1)*table[0].length)+1);
        for (byte[] line:table){
            for (byte pos:line){
                builder.append(pos==0?' ':pos==1?'_':'#');
            }
            builder.append('\n');
        }
        String name = "CVI_"+minX+"_"+minZ+".txt";
        TsonFile.write(new File(name), builder.toString());
        reply(sender, "Saved to ["+name+"]");
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if(args.length < 2) return completions;
        return Collections.emptyList();
    }


    public void version(MinecraftServer server, ICommandSender sender){
        reply(sender, String.format("&eVersion: &d%s-%s (v%s-%s)",
                SSPatchLoader.ELCA_RELEASE, SSPatchLoader.ELCA_VERSION,
                ForgeVersion.mcVersion, ForgeVersion.buildVersion
        ));
    }


    public void mobs(MinecraftServer server, ICommandSender sender){
        if(sender instanceof Entity){
            mobs(server, sender, ((Entity) sender).dimension);
        } else {
            reply(sender, danger+"It is`nt possible to specify the dimension automatically!");
        }
    }

    private static class MobInfo implements Comparable<MobInfo>{
        final String name;
        int count;
        int lod0, lod1, lod2, lod3;

        public MobInfo(Class<? extends Entity> clazz){
            EntityEntry entry0 = EntityRegistry.getEntry(clazz);
            String name = entry0==null?clazz.getSimpleName():entry0.getName();
            if(name==null) name = clazz.getSimpleName();
            this.name = name;
        }
        public void add(int lodLvl){
            count++;
            if(lodLvl== EntityLodFunc.NEAR_MASK)lod0++;
            if(lodLvl==EntityLodFunc.LOW1_MASK) lod1++;
            if(lodLvl==EntityLodFunc.LOW2_MASK) lod2++;
            if(lodLvl==EntityLodFunc.LOW3_MASK) lod3++;
        }
        private String getName(int nameLen){
            StringBuilder buffer = new StringBuilder(nameLen);
            buffer.append(name);
            int curName = name.length();
            while(curName++ < nameLen){
                buffer.append(' ');
            }
            return buffer.toString();
        }
        public void send(ICommandSender sender, int maxName, String prefix){
            reply(sender, primary+"%s&8|&a%s %s &e(%s, N:%s, M: %s, L: %s)", prefix,
                    getName(maxName+1), count, lod0, lod1, lod2, lod3
            );
        }
        @Override
        public int compareTo(@NotNull MobInfo o) {
            return Integer.compare(o.count, count);
        }
    }

    private static class MobInfoList extends IdentityHashMap<Class<?>, MobInfo>{
        private boolean dirty = true;
        private List<MobInfo> result;
        private int numLength, nameLength;
        public void add(Entity entity){
            if(entity == null || entity.isDead)return;
            dirty = true;
            Class<? extends Entity> clazz = entity.getClass();
            MobInfo info = get(clazz);
            if(info==null){
                put(clazz, info = new MobInfo(clazz));
            }
            info.add(entity.lodLvl());
        }

        private void updateResult(){
            if(!dirty)return;
            result = new ArrayList<>(values());
            int maxName = 0;
            for (MobInfo info:result){
                int curLen = info.name.length();
                if(curLen > maxName)maxName = curLen;
            }
            result.sort(MobInfo::compareTo);
            nameLength = maxName;
            numLength = String.valueOf(result.size()).length();
        }

        public List<MobInfo> result(){
            updateResult();
            return result;
        }

        public int numLength(){
            updateResult();
            return numLength;
        }

        protected int nameLength(){
            updateResult();
            return nameLength;
        }

        public void display(ICommandSender sender, String prefix){
            List<MobInfo> result = result();

            int nameLength = nameLength();
            int numLength = numLength();
            int num = 0;

            for (MobInfo info:result){
                info.send(sender, nameLength+1, prefix+formatNum(++num, numLength));
            }
        }
    }


    public void mobs(MinecraftServer server, ICommandSender sender, int dim){
        WorldServer worldServer = DimensionManager.getWorld(dim);
        List<Entity> list = new ArrayList<>(worldServer.loadedEntityList);
        MobInfoList mobs = new MobInfoList();

        for (Entity entity:list){
            mobs.add(entity);
        }

        mobs.display(sender, "");
    }


    private static String formatNum(int num, int max){
        String str = String.valueOf(num);
        while (str.length() < max)str = str + ' ';
        return str;
    }


    public void info(MinecraftServer server, ICommandSender sender){
        reply(sender, warning+"Loaded worlds: "+primary+server.worlds.length);

        for (Integer dimId : DimensionManager.getIDs()) {
            sendWorldInfo(DimensionManager.getWorld(dimId), sender);
        }
    }


    private void sendWorldInfo(WorldServer world, ICommandSender sender){
        int curMobs, curChunks;
        int lod0 = 0, lod1 = 0, lod2 = 0, lod3 = 0;
        curMobs = world.loadedEntityList.size();

        for (Entity entity:world.loadedEntityList){
            if(entity == null)continue;
            int lodLvl = entity.lodLvl();
            if(lodLvl == EntityLodFunc.NEAR_MASK) lod0++;
            if(lodLvl == EntityLodFunc.LOW1_MASK) lod1++;
            if(lodLvl == EntityLodFunc.LOW2_MASK) lod2++;
            if(lodLvl == EntityLodFunc.LOW3_MASK) lod3++;
        }

        int dimId = world.provider.getDimension();

        curChunks = world.getChunkProvider().getLoadedChunkCount();
        double worldTickTime = mean(world.getMinecraftServer().worldTickTimes.get(dimId));
        double worldTPS = Math.min(1000.0/worldTickTime, 20);
        reply(sender, primary+"[%s]: tt: %s, tps %s, chunk: %s, mob: %s (%s, N:%s, M: %s, L: %s)",
                getDimensionPrefix(dimId),
                formatTickTime(worldTickTime),
                formatTps(worldTPS),
                curChunks, curMobs, lod0, lod1, lod2, lod3
        );
    }


    private String formatTickTime(double tt){
        String time = TIME_FORMATTER.format(tt);
        if(tt>=50)return critical +time+primary;
        if(tt>=45)return danger   +time+primary;
        if(tt>=30)return warning  +time+primary;
        else      return normal   +time+primary;
    }


    private String formatTps(double wtps){
        String tps = TIME_FORMATTER.format(wtps);
        if(wtps<=15)  return critical +tps+primary;
        if(wtps<=17)  return danger   +tps+primary;
        if(wtps<=19.9)return warning  +tps+primary;
        else          return normal   +tps+primary;
    }


    public void tps(MinecraftServer server, ICommandSender sender, boolean summary) throws CommandException {
        int dim = 0;
        if (summary) {
            for (Integer dimId : DimensionManager.getIDs()) {
                double worldTickTime = mean(server.worldTickTimes.get(dimId)) * 1.0E-6D;
                double worldTPS = Math.min(1000.0/worldTickTime, 20);
                reply(sender, "[%s][%s]: %s",
                        getDimensionPrefix(dimId),
                        TIME_FORMATTER.format(worldTickTime),
                        TIME_FORMATTER.format(worldTPS)
                );
            }
            double meanTickTime = mean(server.tickTimeArray);
            double meanTPS = Math.min(1000.0/meanTickTime, 20);
            sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, "commands.forge.tps.summary","Overall", TIME_FORMATTER.format(meanTickTime), TIME_FORMATTER.format(meanTPS)));
        } else {
            double worldTickTime = mean(server.worldTickTimes.get(dim));
            double worldTPS = Math.min(1000.0/worldTickTime, 20);
            sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, "commands.forge.tps.summary", getDimensionPrefix(dim), TIME_FORMATTER.format(worldTickTime), TIME_FORMATTER.format(worldTPS)));
        }
    }


    private static String getDimensionPrefix(int dimId) {
        DimensionType providerType = DimensionManager.getProviderType(dimId);
        if (providerType == null) {
            return String.format("%2d", dimId);
        } else {
            return String.format("%s-%2d", providerType.getName(), dimId);
        }
    }


    private static double mean(long[] values) {
        long sum = 0L;
        for (long v : values) {
            sum += v;
        }
        return ((double) sum / values.length) * 1.0E-6D;
    }
}