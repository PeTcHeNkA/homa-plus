package plus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plus.utls.Coder.Coder;
import plus.utls.Coder.ProtocolLibCoder;
import plus.utls.Random.RandomInv;
import plus.utls.Te4hnoConfig;

import java.util.Random;


public class main extends JavaPlugin {

    public static Te4hnoConfig config;
    public static Te4hnoConfig messages;
    public static Timer t;
    public static Coder coder;
    public static Random r;
    public static RandomInv inv;
    public static Manager m;

    @Override
    public void onEnable() {
        coder = new ProtocolLibCoder();
        config = new Te4hnoConfig(this,"settings.yml");
        messages = new Te4hnoConfig(this,"te4hno_messages.yml");
        t = new Timer();
        r = new Random();
        inv = new RandomInv();
        m = new Manager(this);
        Bukkit.getPluginManager().registerEvents(m, this);
        getCommand("te4hno").setExecutor(new Commands());

        new Te4hnoConfig(this,"LICENSE");
        //m.SpawnBlock();

    }

    @Override
    public void onDisable() {
        t.Disable();

    }
    public static void reLoad(){
        //t.ExternalSave();
        messages = messages.reloadTo();
        config = config.reloadTo();
        m.ReSetValues();
        t.Disable();
        t = new Timer();
        inv.Import();
    }



}
