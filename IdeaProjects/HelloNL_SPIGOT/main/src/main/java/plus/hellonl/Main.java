package plus.hellonl;

import org.bukkit.plugin.java.JavaPlugin;
import plus.nlaunch.NLaunch;
import plus.tson.TsonClass;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Consumer;


public final class Main extends JavaPlugin {
    private TsonMap map;
    private NLaunch launch;

    @Override
    public void onEnable() {
        if(map == null) reloadConfig();

        getLogger().info("Detected os: "+osName());

        //extract("nlaunch."+libExtension());
        //extract("Protected-1.0.jar.lar");
        getLogger().info("Files extracted");
        initNLaunch();
        getLogger().info("NLaunch initialized");
        load().accept(map);
        getLogger().info("Completed");

        //String.class
        //System.out.println("A");
    }


    private static String osName() {
        String var0 = System.getProperty("os.name").toLowerCase().replace(' ', '_');
        if (var0.startsWith("win")) {
            return "win";
        } else {
            return var0.startsWith("mac") ? "darwin" : var0;
        }
    }



    private static String libExtension() {
        if (!osName().contains("os_x") && !osName().contains("darwin")) {
            return osName().contains("win") ? "dll" : "so";
        } else {
            return "dylib";
        }
    }


    private void extract(String name){
        try {
            InputStream stream = getResource(name);
            if(stream == null) throw new NullPointerException(String.format("Resource '%s' not found", name));
            if(!getDataFolder().exists()) getDataFolder().mkdirs();
            File file = new File(getDataFolder(), name);
            if(file.exists()) file.delete();
            Files.copy(stream, file.toPath());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void initNLaunch(){
        System.load(new File(getDataFolder(), "nlaunch."+libExtension()).getAbsolutePath());
        getLogger().info("Native lib loaded");
        launch = new NLaunch(getClassLoader());
        getLogger().info("Classloader created");
        String name = new File(getDataFolder(), "Protected-1.0_B.jar.lar").getAbsolutePath();
        NLaunch.addFile(name);
        getLogger().info("File added: [" + name+"]");
        String name2 = new File(getDataFolder(), "Protected-1.0_M.jar.lar").getAbsolutePath();
        NLaunch.addFile(name2);
        getLogger().info("File added: [" + name2+"]");
    }


    private Consumer<TsonMap> load(){
        try {
            Class clazz = Class.forName("plus.pro.Main", true, launch);
            if (clazz == null) throw new RuntimeException("Class not found");
            getLogger().info("Class loaded: " + clazz.getName());
            return (Consumer<TsonMap>) new TsonClass(clazz).createInst();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onDisable() {
        getLogger().info("Stopped.");
    }


    @Override
    public void reloadConfig() {
        map = new TsonFile(new File(getDataFolder(), "config.tson"));
    }
}
