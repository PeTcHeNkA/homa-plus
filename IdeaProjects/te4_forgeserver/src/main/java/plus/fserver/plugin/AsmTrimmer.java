package plus.fserver.plugin;

import net.minecraft.launchwrapper.Launch;
import java.security.*;
import java.util.Enumeration;
import java.util.HashMap;
import static plus.tson.utl.uns.UnsafeUtils.UNSAFE;


public class AsmTrimmer {
    public static void runFixes() throws Throwable {
        long off = UNSAFE.objectFieldOffset(SecureClassLoader.class.getDeclaredField("pdcache"));
        long nameOff = UNSAFE.objectFieldOffset(Permission.class.getDeclaredField("name"));

        HashMap<CodeSource, ProtectionDomain> domains = (HashMap<CodeSource, ProtectionDomain>) UNSAFE.getObject(Launch.classLoader, off);

        synchronized (domains) {
            domains.forEach((codeSource, pd) -> {
                PermissionCollection pc = pd.getPermissions();
                if (pc != null) {
                    Enumeration<Permission> perms = pc.elements();
                    while (perms.hasMoreElements()) {
                        Permission perm = perms.nextElement();
                        UNSAFE.putObject(perm, nameOff, "*");
                    }
                }
            });
        }
    }
}
