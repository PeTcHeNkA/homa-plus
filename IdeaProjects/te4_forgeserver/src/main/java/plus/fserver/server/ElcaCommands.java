package plus.fserver.server;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.server.command.TextComponentHelper;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ServerCommand;
import plus.fserver.plugin.SSPatchLoader;
import plus.fserver.utls.EntityLodFunc;
import plus.tson.TsonFile;
import plus.tson.utl.ByteStrBuilder;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;


public class ElcaCommands extends ServerCommand {
    private static final String primary  = "&b";
    private static final String critical = "&4";
    private static final String danger   = "&c";
    private static final String warning  = "&e";
    private static final String normal   = "&a";

    private final List<String> completions = ImmutableList.copyOf(
        new String[]{"version", "info", "tps", "mobs"}
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
        return completions;
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

    private static class MobInfo{
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
    }


    public void mobs(MinecraftServer server, ICommandSender sender, int dim){
        WorldServer worldServer = DimensionManager.getWorld(dim);
        List<Entity> list = new ArrayList<>(worldServer.loadedEntityList);
        IdentityHashMap<Class<?>, MobInfo> mobs = new IdentityHashMap<>();

        for (Entity entity:list){
            if(entity == null)continue;
            Class<?> clazz = entity.getClass();
            MobInfo info = mobs.get(clazz);
            if(info==null){
                mobs.put(clazz, info = new MobInfo((Class<? extends Entity>) clazz));
            }
            info.add(entity.lodLvl());
        }

        List<MobInfo> result = new ArrayList<>(mobs.values());
        result.sort((o1, o2) -> {
            int n1 = o1.count;
            int n2 = o2.count;
            return Integer.compare(n2, n1);
        });

        int maxName = 0;
        for (MobInfo info:result){
            int curLen = info.name.length();
            if(curLen > maxName)maxName = curLen;
        }
        int numL = String.valueOf(result.size()).length();
        int num = 0;

        for (MobInfo info:result){
            reply(sender, primary+"%s&8|&a%s %s &e(%s, N:%s, M: %s, L: %s)", formatNum(++num, numL),
                    info.getName(maxName+1), info.count, info.lod0, info.lod1, info.lod2, info.lod3
            );
        }
    }


    private String formatNum(int num, int max){
        String str = String.valueOf(num);
        while (str.length() < max)str = str + ' ';
        return str;
    }


    public void info(MinecraftServer server, ICommandSender sender){
        reply(sender, warning+"Loaded worlds: "+primary+server.worlds.length);
        int curMobs = 0;
        int lod0 = 0, lod1 = 0, lod2 = 0, lod3 = 0;
        int curChunks = 0;
        for (Integer dimId : DimensionManager.getIDs()) {
            WorldServer world = DimensionManager.getWorld(dimId);
            curMobs = world.loadedEntityList.size();

            for (Entity entity:world.loadedEntityList){
                if(entity == null)continue;
                int lodLvl = entity.lodLvl();
                if(lodLvl == EntityLodFunc.NEAR_MASK) lod0++;
                if(lodLvl == EntityLodFunc.LOW1_MASK) lod1++;
                if(lodLvl == EntityLodFunc.LOW2_MASK) lod2++;
                if(lodLvl == EntityLodFunc.LOW3_MASK) lod3++;
            }

            curChunks = world.getChunkProvider().getLoadedChunkCount();
            double worldTickTime = mean(server.worldTickTimes.get(dimId)) * 1.0E-6D;
            double worldTPS = Math.min(1000.0/worldTickTime, 20);
            reply(sender, primary+"[%s]: tt: %s, tps %s, chunk: %s, mob: %s (%s, N:%s, M: %s, L: %s)",
                    getDimensionPrefix(dimId),
                    formatTickTime(worldTickTime),
                    formatTps(worldTPS),
                    curChunks, curMobs, lod0, lod1, lod2, lod3
            );
        }
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
            double meanTickTime = mean(server.tickTimeArray) * 1.0E-6D;
            double meanTPS = Math.min(1000.0/meanTickTime, 20);
            sender.sendMessage(TextComponentHelper.createComponentTranslation(sender, "commands.forge.tps.summary","Overall", TIME_FORMATTER.format(meanTickTime), TIME_FORMATTER.format(meanTPS)));
        } else {
            double worldTickTime = mean(server.worldTickTimes.get(dim)) * 1.0E-6D;
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


    private static long mean(long[] values) {
        long sum = 0L;
        for (long v : values) {
            sum += v;
        }
        return sum / values.length;
    }
}