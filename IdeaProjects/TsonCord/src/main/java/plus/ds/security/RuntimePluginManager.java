package plus.ds.security;

import plus.ds.IrisSystem;
import plus.ds.dyn.ButtonListener;
import plus.ds.dyn.SimpleMsgListener;
import plus.ds.msg.ComplexMsg;
import plus.tson.TsonClass;
import plus.tson.TsonMap;
import plus.tson.security.ClassManager;
import java.lang.reflect.InvocationTargetException;
import java.util.IdentityHashMap;
import static plus.ds.plsys.DiscordPluginManager.getShortClass;


public class RuntimePluginManager implements ClassManager {
    private final CConctructor cConctructor;
    private final IrisSystem.GuildProgram program;

    private RuntimePluginManager(CConctructor cConctructor, IrisSystem.GuildProgram program) {
        this.cConctructor = cConctructor;
        this.program = program;
    }

    public interface CConctructor{
        Object newInstance(IrisSystem.GuildProgram program, Class<?> clazz, Object... args);
    }

    public static final class ConstructorLib implements CConctructor{
        private final IdentityHashMap<Class<?>, CConctructor> constructors = new IdentityHashMap<>();
        private final CConctructor defConstructor = (program, clazz, args) -> {
            try {
                return TsonClass.createInst(clazz, args);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
        public ConstructorLib(){
            add(ComplexMsg.class,        (program, clazz, args) -> new ComplexMsg(program, (TsonMap) args[0]));
            add(SimpleMsgListener.class, (program, clazz, args) -> new SimpleMsgListener(program, (TsonMap) args[0]));
            add(ButtonListener.class,    (program, clazz, args) -> new SimpleMsgListener(program, (TsonMap) args[0]));
        }
        private void add(Class<?> clazz, CConctructor c){
            constructors.put(clazz, c);
        }
        @Override
        public Object newInstance(IrisSystem.GuildProgram program, Class<?> clazz, Object... args){
            return constructors.getOrDefault(clazz, defConstructor).newInstance(program, clazz, args);
        }
        public RuntimePluginManager managerById(IrisSystem.GuildProgram program){
            return new RuntimePluginManager(this, program);
        }
    }


    private void checkIllegalShortClazz(String clazz){
        String shortName = clazz.toLowerCase();
        switch (shortName){
            case "script-cfg":
                throw new IllegalArgumentException();
        }
    }


    @Override
    public Class<?> forName(String clazz) {
        Class<?> result = getShortClass(clazz);
        if(result != null)return result;
        return ClassManager.super.forName(clazz);
    }


    @Override
    public Object newInstance(Class<?> clazz, Object... args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return cConctructor.newInstance(program, clazz, args);
    }


    @Override
    public String getAllowedPath() {
        return "plus.ds.plugin.";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuntimePluginManager that = (RuntimePluginManager) o;
        if (program != that.program) return false;
        return cConctructor.equals(that.cConctructor);
    }


    @Override
    public int hashCode() {
        return (int) (program.guildId ^ (program.guildId >>> 32));
    }
}