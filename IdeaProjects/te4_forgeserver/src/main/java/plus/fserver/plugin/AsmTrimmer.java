package plus.fserver.plugin;

import net.minecraft.launchwrapper.Launch;
import java.security.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static plus.tson.utl.uns.UnsafeUtils.UNSAFE;
import static plus.tson.utl.uns.UnsafeUtils.*;


public class AsmTrimmer {
    public static void runFixes() throws Throwable {
        long off = offset(SecureClassLoader.class,"pdcache");

        Map<?, ProtectionDomain> domains = (Map<?, ProtectionDomain>) UNSAFE.getObject(Launch.classLoader, off);

        if(domains instanceof ConcurrentHashMap){
            forMap(domains);
        } else {
            synchronized (domains) {
                forMap(domains);
            }
        }
    }


    private static void forMap(Map<?, ProtectionDomain> domains) throws NoSuchFieldException {
        long nameOff = offset(Permission.class,"name");

        domains.forEach((obj, pd) -> {
            PermissionCollection pc = pd.getPermissions();
            if (pc != null) {
                Enumeration<Permission> perms = pc.elements();
                while (perms.hasMoreElements()) {
                    Permission perm = perms.nextElement();
                    set(perm, nameOff, "");
                }
            }
        });
    }
}
