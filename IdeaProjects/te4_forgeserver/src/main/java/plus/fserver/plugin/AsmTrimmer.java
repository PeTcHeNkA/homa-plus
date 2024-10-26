package plus.fserver.plugin;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.stats.StatList;
import plus.fserver.server.Reflector;
import plus.fserver.utls.collections.IdentityTe4HashSet;
import java.security.*;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static plus.tson.utl.uns.UnsafeUtils.UNSAFE;
import static plus.tson.utl.uns.UnsafeUtils.*;
import static plus.fserver.server.Reflector.cleanReflectCache;


public class AsmTrimmer {
    public static int runFixes() {
        IdentityTe4HashSet<ClassLoader> loaders = new IdentityTe4HashSet<>(8);
        long off        = offset(SecureClassLoader.class,"pdcache");
        long nameOff    = offset(Permission.class,"name");
        long classesOff = offset(ClassLoader.class, "classes");

        AtomicInteger num = new AtomicInteger();
        SecureClassLoader loader = Launch.classLoader;
        loaders.add(loader);
        runFixes(loader, off, nameOff, num, loaders);

        try {
            loaders.forEach(curLoader -> {
                List<Class<?>> classes = get(curLoader, classesOff);
                synchronized (classes){
                    classes.forEach(Reflector::cleanReflectCache);
                }
            });
        }catch (ConcurrentModificationException ignored){}
        cleanReflectCache(Class.class);

        return num.get() + StatList.trimSize();
    }


    private static void runFixes(SecureClassLoader loader, long off, long nameOff, AtomicInteger num, IdentityTe4HashSet<ClassLoader> loaders) {
        Map<?, ProtectionDomain> domains = (Map<?, ProtectionDomain>) UNSAFE.getObject(loader, off);

        if(domains instanceof ConcurrentHashMap){
            forMap(domains, nameOff, num);
        } else {
            synchronized (domains) {
                forMap(domains, nameOff, num);
            }
        }

        l1: try {
            ClassLoader parent = loader.getParent();
            while (parent != null){
                if(parent instanceof SecureClassLoader){
                    if(loaders.add(parent)) {
                        runFixes((SecureClassLoader) parent, off, nameOff, num, loaders);
                    }
                    break l1;
                } else loaders.add(parent);
                parent = parent.getParent();
            }
        } catch (SecurityException e){
            throw new RuntimeException(e);
        }
        ClassLoader parent = loader.getClass().getClassLoader();
        while (parent != null) {
            if (parent instanceof SecureClassLoader) {
                if (loaders.add(parent)) {
                    runFixes((SecureClassLoader) parent, off, nameOff, num, loaders);
                    return;
                }
            } else loaders.add(parent);
            ClassLoader nextLoader = parent.getClass().getClassLoader();
            if(nextLoader != parent)parent = nextLoader;
        }
    }


    private static void forMap(Map<?, ProtectionDomain> domains, long nameOff, AtomicInteger num) {
        domains.forEach((obj, pd) -> {
            PermissionCollection pc = pd.getPermissions();
            if (pc != null) {
                Enumeration<Permission> perms = pc.elements();
                while (perms.hasMoreElements()) {
                    Permission perm = perms.nextElement();
                    num.addAndGet(perm.getName().length()+12); //96 = summ of refs size
                    set(perm, nameOff, "");
                }
            }
        });
    }
}
