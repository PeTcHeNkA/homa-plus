//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.serverSide;

import org.cache2k.jmx.*;
import org.cache2k.*;
import org.cache2k.core.*;
import java.util.*;

public class ManagerMXBeanImpl implements CacheManagerMXBean
{
    private final CacheManagerImpl manager;
    
    public ManagerMXBeanImpl(final CacheManagerImpl manager) {
        this.manager = manager;
    }
    
    @Override
    public String getHealthStatus() {
        final List<HealthInfoElement> li = new ArrayList<HealthInfoElement>();
        for (final Cache c : this.manager.getActiveCaches()) {
            final InternalCache ic = (InternalCache)c;
            li.addAll(ic.getInfo().getHealth());
        }
        this.sortHealthInfoList(li);
        return constructHealthString(li);
    }
    
    void sortHealthInfoList(final List<HealthInfoElement> li) {
        Collections.sort(li, new Comparator<HealthInfoElement>() {
            @Override
            public int compare(final HealthInfoElement o1, final HealthInfoElement o2) {
                if (!o1.getLevel().equals(o2.getLevel())) {
                    return "FAILURE".equals(o1.getLevel()) ? -1 : 1;
                }
                return o1.getCache().getName().compareTo(o2.getCache().getName());
            }
        });
    }
    
    static String constructHealthString(final List<HealthInfoElement> _sortedList) {
        if (_sortedList.isEmpty()) {
            return "ok";
        }
        boolean _comma = false;
        final StringBuilder sb = new StringBuilder();
        for (final HealthInfoElement hi : _sortedList) {
            if (_comma) {
                sb.append("; ");
            }
            sb.append(hi.getLevel()).append(": ").append("[").append(hi.getCache().getName() + "] ").append(hi.getMessage());
            _comma = true;
        }
        return sb.toString();
    }
    
    @Override
    public void clear() {
        this.manager.clear();
    }
    
    @Override
    public String getVersion() {
        return this.manager.getVersion();
    }
    
    @Override
    public String getBuildNumber() {
        return "not used";
    }
}
