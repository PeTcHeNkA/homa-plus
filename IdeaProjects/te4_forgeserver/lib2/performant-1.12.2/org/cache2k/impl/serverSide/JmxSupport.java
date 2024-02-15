//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.serverSide;

import org.cache2k.core.spi.*;
import org.cache2k.core.util.*;
import org.cache2k.configuration.*;
import javax.management.*;
import org.cache2k.*;
import org.cache2k.core.*;
import java.lang.management.*;

public class JmxSupport implements CacheLifeCycleListener, CacheManagerLifeCycleListener
{
    private static final String REGISTERED_FLAG;
    private static final boolean MANAGEMENT_UNAVAILABLE;
    private Log log;
    
    public JmxSupport() {
        this.log = Log.getLog((Class)JmxSupport.class);
    }
    
    public void cacheCreated(final Cache c, final Cache2kConfiguration cfg) {
        if (JmxSupport.MANAGEMENT_UNAVAILABLE || !cfg.isEnableJmx()) {
            return;
        }
        final MBeanServer mbs = getPlatformMBeanServer();
        final String _name = this.standardName(c.getCacheManager(), c);
        try {
            mbs.registerMBean(c.getStatistics(), new ObjectName(_name));
        }
        catch (InstanceAlreadyExistsException ignore) {
            this.log.debug("register failure, cache: " + c.getName(), (Throwable)ignore);
        }
        catch (Exception e) {
            throw new CacheException("register JMX bean, ObjectName: " + _name, (Throwable)e);
        }
    }
    
    public void cacheDestroyed(final Cache c) {
        if (JmxSupport.MANAGEMENT_UNAVAILABLE) {
            return;
        }
        final MBeanServer mbs = getPlatformMBeanServer();
        final String _name = this.standardName(c.getCacheManager(), c);
        try {
            mbs.unregisterMBean(new ObjectName(_name));
        }
        catch (InstanceNotFoundException ex) {}
        catch (Exception e) {
            throw new CacheException("unregister JMX bean, ObjectName: " + _name, (Throwable)e);
        }
    }
    
    public void managerCreated(final CacheManager m) {
        if (JmxSupport.MANAGEMENT_UNAVAILABLE) {
            return;
        }
        final MBeanServer mbs = getPlatformMBeanServer();
        final ManagerMXBeanImpl _mBean = new ManagerMXBeanImpl((CacheManagerImpl)m);
        final String _name = managerName(m);
        try {
            mbs.registerMBean(_mBean, new ObjectName(_name));
            m.getProperties().put(JmxSupport.REGISTERED_FLAG, true);
            this.log.debug("Manager created and registered as: " + _name);
        }
        catch (InstanceAlreadyExistsException ignore) {
            this.log.debug("register failure, manager: " + m.getName(), (Throwable)ignore);
        }
        catch (Exception e) {
            throw new CacheException("register JMX bean, ObjectName: " + _name, (Throwable)e);
        }
    }
    
    public void managerDestroyed(final CacheManager m) {
        if (JmxSupport.MANAGEMENT_UNAVAILABLE) {
            return;
        }
        if (!m.getProperties().containsKey(JmxSupport.REGISTERED_FLAG)) {
            return;
        }
        final MBeanServer mbs = getPlatformMBeanServer();
        final String _name = managerName(m);
        try {
            mbs.unregisterMBean(new ObjectName(_name));
        }
        catch (Exception e) {
            throw new CacheException("Error unregister JMX bean, ObjectName: " + _name, (Throwable)e);
        }
    }
    
    private static MBeanServer getPlatformMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
    
    private static String managerName(final CacheManager cm) {
        return "org.cache2k:type=CacheManager,name=" + sanitizeNameAsJmxValue(cm.getName());
    }
    
    private String standardName(final CacheManager cm, final Cache c) {
        return "org.cache2k:type=Cache,manager=" + sanitizeNameAsJmxValue(cm.getName()) + ",name=" + sanitizeNameAsJmxValue(c.getName());
    }
    
    private static String sanitizeNameAsJmxValue(final String s) {
        if (s.indexOf(44) >= 0) {
            return '\"' + s + '\"';
        }
        return s;
    }
    
    static {
        REGISTERED_FLAG = JmxSupport.class.getName() + ".registered";
        boolean v;
        try {
            v = (getPlatformMBeanServer() == null);
        }
        catch (NoClassDefFoundError err) {
            v = true;
        }
        MANAGEMENT_UNAVAILABLE = v;
    }
}
