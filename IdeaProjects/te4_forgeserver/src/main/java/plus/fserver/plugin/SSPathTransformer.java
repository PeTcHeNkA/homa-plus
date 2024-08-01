package plus.fserver.plugin;

import net.minecraft.launchwrapper.IClassTransformer;
import plus.fserver.utls.CSVReader;


public class SSPathTransformer extends TweakEngine implements IClassTransformer{
    private boolean shouldInit = true;

    public SSPathTransformer(){
        this(true);
    }


    public SSPathTransformer(boolean shouldInid){
        super("plus/fserver/plugin/SSPathTransformer.class");
        if(shouldInid)init();
    }


    public SSPathTransformer init(){
        if(shouldInit) {
            addPatch();
            addOverrides();
            preLoadClasses();
            shouldInit = false;
        }
        return this;
    }


    private static class PathInfo {
        private boolean safe = false;
        private boolean preload = false;
        private boolean useTransformers = true;
        private String clazz;

        public PathInfo read(Object[] args){
            clazz = args[0].toString();
            try {
                if (args.length > 1) {
                    safe = (boolean) args[1];
                } else {
                    safe = clazz.startsWith("net.minecraftforge");
                }
                if (args.length > 2) {
                    preload = (boolean) args[2];
                }
                if (args.length > 3) {
                    useTransformers = (boolean) args[3];
                }
            } catch (Exception e){
                safe = true;
                e.printStackTrace();
            }
            return this;
        }
    }


    private void addPatch(){
        byte[] table = getFastSrc("elcapatch.csv");
        for (PathInfo inf:new CSVReader<>(table, new PathInfo()::read))
            add(inf.clazz, inf.safe, inf.preload, inf.useTransformers);
    }


    private void addOverrides(){
        add("net.minecraft.server.MinecraftServer", true);
        add("net.minecraft.server.MinecraftServer$1", true);
        add("net.minecraft.server.MinecraftServer$2", true);
        add("net.minecraft.server.dedicated.DedicatedServer", true);
        add("net.minecraft.server.dedicated.DedicatedServer$1", true);
        add("net.minecraft.server.dedicated.DedicatedServer$2", true);
    }
}
