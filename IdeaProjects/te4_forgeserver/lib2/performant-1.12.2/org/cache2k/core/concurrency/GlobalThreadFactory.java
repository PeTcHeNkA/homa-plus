//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.core.concurrency;

import java.util.concurrent.*;

public class GlobalThreadFactory implements ThreadFactory
{
    private static final ConcurrentMap<String, String> NAMES_RUNNING;
    private String prefix;
    
    public GlobalThreadFactory(final String _threadNamePrefix) {
        this.prefix = _threadNamePrefix;
    }
    
    protected String generateName(final int id) {
        return this.prefix + '-' + id;
    }
    
    @Override
    public Thread newThread(final Runnable r) {
        int id = 1;
        String _name;
        while (true) {
            _name = this.generateName(id);
            if (GlobalThreadFactory.NAMES_RUNNING.putIfAbsent(_name, _name) == null) {
                break;
            }
            ++id;
        }
        final String _finalName = _name;
        final Runnable _myRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    r.run();
                }
                finally {
                    GlobalThreadFactory.NAMES_RUNNING.remove(_finalName);
                }
            }
        };
        final Thread thr = new Thread(_myRunnable);
        thr.setName(_name);
        thr.setDaemon(true);
        return thr;
    }
    
    static {
        NAMES_RUNNING = new ConcurrentHashMap<String, String>();
    }
}
