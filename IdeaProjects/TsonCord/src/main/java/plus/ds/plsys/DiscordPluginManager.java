package plus.ds.plsys;

import org.jetbrains.annotations.Nullable;
import plus.ds.IrisSystem;
import plus.ds.dyn.ButtonListener;
import plus.ds.dyn.InstallProgramListener;
import plus.ds.dyn.SimpleMsgListener;
import plus.ds.security.RuntimePluginManager;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.util.AbsPluginSystem;
import plus.util.IPluginProcessor;
import plus.util.PCLTask;
import plus.util.PluginClassLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.IdentityHashMap;


public class DiscordPluginManager extends AbsPluginSystem<PluginClassLoader> {
    private final IrisSystem.GuildProgram defProgram;
    private PluginClassLoader empty;
    public DiscordPluginManager(IrisSystem system) {
        this.defProgram = system.getOrCreateGuildProgram(0);
    }

    @Override
    protected IPluginProcessor<?>[] processors() {
        return new IPluginProcessor[0];
    }


    @Override
    public String getAllowedPath() {
        return defProgram.manager.getAllowedPath();
    }


    @Override
    public Class<?> forName(String clazz) {
        Class<?> result = getShortClass(clazz);
        if(result != null)return result;
        return super.forName(clazz);
    }


    @Nullable
    public static Class<?> getShortClass(String clazz){
        String clazz0 = clazz.toLowerCase();
        switch (clazz0){
            case "msg": case "simple-msg": case "sm-msg":
                return SimpleMsgListener.class;
            case "script-cfg":
                return InstallProgramListener.class;
            case "iris": case "iris-system":
                return IrisSystem.class;
            case "button": case "btn":
                return ButtonListener.class;
        }
        return null;
    }


    @Override
    public Object newInstance(Class<?> clazz, Object... args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if(clazz == IrisSystem.class)return defProgram.system;
        return defProgram.manager.newInstance(clazz, args);
    }


    @Override
    protected boolean filter(File file){
        if(file.isFile()) {
            String name = file.getName();
            if(name.endsWith(".jar"))return true;
            if(name.endsWith(".tson"))return true;
        }
        return false;
    }


    @Override
    protected PCLTask[] loadPluginsFromFile(File file) {
        if(file.getName().endsWith(".tson")){
            if(empty==null){
                try {
                    empty = newClassloader();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            super.secOnLoadConfig(empty, new TsonFile(file).load(this));
            return new PCLTask[0];
        } else {
            return super.loadPluginsFromFile(file);
        }
    }


    @Override
    protected void onLoadConfig(PluginClassLoader loader, TsonMap map) {
        super.onLoadConfig(loader, map);
        if(map!=null){
            if(map.containsKey("include")){
                TsonObj obj = map.get("include");
                File parent = null;
                if(map instanceof TsonFile){
                    parent = ((TsonFile) map).getFile().getParentFile();
                }
                if(obj.isList()){
                    for(TsonObj obj0: obj.getList()){
                        loadPluginsFromFile(
                                parent==null?new File(obj0.getStr()):new File(parent,obj0.getStr())
                        );
                    }
                } else if(obj.isString()){
                    loadPluginsFromFile(
                            parent==null?new File(obj.getStr()):new File(parent,obj.getStr())
                    );
                } else {
                    System.out.println("Unsupported type: ["+obj+"]");
                }
            }
//            if(map.containsKey("listeners")){
//                TsonObj obj = map.get("listeners");
//                if(obj.isList()){
//                    ArrayList<Object> list = new ArrayList<>();
//                    for(TsonObj obj0: obj.getList()){
//                        list.add(obj0.getField());
//
//                    }
//                    defProgram.system.addRootEventListeners(list.toArray());
//                } else {
//                    defProgram.system.addRootEventListeners(obj.getField());
//                }
//            }
        }
    }
}