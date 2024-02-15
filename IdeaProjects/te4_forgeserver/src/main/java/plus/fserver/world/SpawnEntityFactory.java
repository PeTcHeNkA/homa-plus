package plus.fserver.world;

import io.netty.util.collection.IntObjectHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import plus.fserver.utls.Reflector;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

public class SpawnEntityFactory {
    private static final Map<ClassLoader, MethodHandles.Lookup> altLookups = new Reference2ReferenceArrayMap<>();
    public static final IntObjectHashMap<Function<World, ? extends Entity>> cache = new IntObjectHashMap<>();

    public static Function<World, ? extends Entity> make(Class<? extends Entity> cls){
        int hs;
        Function<World, ? extends Entity> func = cache.get(hs = cls.hashCode());
        if(func != null)return func;

        if (!Modifier.isAbstract(cls.getModifiers())) {
            try {
                MethodHandles.Lookup lookup = Reflector.LOOKUP;
                ClassLoader classLoader = cls.getClassLoader();
                if (classLoader != Launch.classLoader) {
                    lookup = altLookups.get(classLoader);
                    if (lookup == null) {
                        altLookups.put(classLoader, lookup = Reflector.getCtor(MethodHandles.Lookup.class, Class.class).newInstance(cls));
                    }
                }
                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class),
                        Reflector.resolveCtor(cls, World.class),
                        MethodType.methodType(cls, World.class));
                func = (Function<World, ? extends Entity>) callSite.getTarget().invokeExact();
                cache.put(hs, func);
                return func;
            } catch (Throwable t) {
                System.err.println("Could not establish a faster Entity constructor replacement for "+cls);
                t.printStackTrace();
            }
        } else {
            System.err.println("Could not establish a faster Entity constructor replacement for "+cls+", as the class is abstract");
        }

        return new Function<World, Entity>() {
            private final Constructor<? extends Entity> constructor =
                    ObfuscationReflectionHelper.findConstructor(cls, World.class);
            @Override
            public Entity apply(final World world) {
                try {
                    return this.constructor.newInstance(world);
                }
                catch (final IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    FMLLog.log.error("Encountered an exception while constructing entity '{}'", cls.getSimpleName(), e);
                    return null;
                }
            }
        };
    }
}