package org.cache2k.core.util;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.atomic.*;


public abstract class Log {
    private static Map<String, Log> loggers;
    private static LogFactory logFactory;
    
    public static Log getLog(final Class<?> _class) {
        return getLog(_class.getName());
    }
    
    public static synchronized Log getLog(final String s) {
        Log l = Log.loggers.get(s);
        if (l != null) {
            return l;
        }
        if (Log.logFactory == null) {
            initializeLogFactory();
        }
        l = Log.logFactory.getLog(s);
        Log.loggers.put(s, l);
        return l;
    }
    
    private static void log(final String s) {
        getLog(Log.class.getName()).debug(s);
    }
    
    private static void initializeLogFactory() {
        final ServiceLoader<LogFactory> loader = ServiceLoader.load(LogFactory.class);
        final Iterator<LogFactory> iterator = loader.iterator();
        if (iterator.hasNext()) {
            final LogFactory lf = Log.logFactory = iterator.next();
            log("New instance, using: " + Log.logFactory.getClass().getName());
            return;
        }
        try {
            final org.apache.commons.logging.LogFactory cl = org.apache.commons.logging.LogFactory.getFactory();
            Log.logFactory = new LogFactory() {
                @Override
                public Log getLog(final String s) {
                    return new CommonsLogger(cl.getInstance(s));
                }
            };
            log("New instance, using commons logging");
        } catch (NoClassDefFoundError noClassDefFoundError2) {
            Log.logFactory = new LogFactory() {
                @Override
                public Log getLog(final String s) {
                    return new JdkLogger(Logger.getLogger(s));
                }
            };
            log("New instance, using JDK logging");
        }
    }
    
    public static synchronized void registerSuppression(final String s, final Log l) {
        Log.loggers.put(s, l);
    }
    
    public static synchronized void deregisterSuppression(final String s) {
        Log.loggers.remove(s);
    }
    
    public abstract boolean isDebugEnabled();
    
    public abstract boolean isInfoEnabled();
    
    public abstract void debug(final String p0);
    
    public abstract void debug(final String p0, final Throwable p1);
    
    public abstract void info(final String p0);
    
    public abstract void info(final String p0, final Throwable p1);
    
    public abstract void warn(final String p0);
    
    public abstract void warn(final String p0, final Throwable p1);
    
    static {
        Log.loggers = new HashMap<String, Log>();
    }
    
    private static class CommonsLogger extends Log
    {
        org.apache.commons.logging.Log cLog;
        
        private CommonsLogger(final org.apache.commons.logging.Log cLog) {
            this.cLog = cLog;
        }
        
        @Override
        public boolean isDebugEnabled() {
            return this.cLog.isDebugEnabled();
        }
        
        @Override
        public boolean isInfoEnabled() {
            return this.cLog.isInfoEnabled();
        }
        
        @Override
        public void debug(final String s) {
            this.cLog.debug((Object)s);
        }
        
        @Override
        public void debug(final String s, final Throwable ex) {
            this.cLog.debug((Object)s, ex);
        }
        
        @Override
        public void info(final String s, final Throwable ex) {
            this.cLog.info((Object)s);
        }
        
        @Override
        public void info(final String s) {
            this.cLog.info((Object)s);
        }
        
        @Override
        public void warn(final String s) {
            this.cLog.warn((Object)s);
        }
        
        @Override
        public void warn(final String s, final Throwable ex) {
            this.cLog.warn((Object)s, ex);
        }
    }
    
//    private static class Slf4jLogger extends Log
//    {
//        org.slf4j.Logger logger;
//
//        private Slf4jLogger(final org.slf4j.Logger _logger) {
//            this.logger = _logger;
//        }
//
//        @Override
//        public boolean isDebugEnabled() {
//            return this.logger.isDebugEnabled();
//        }
//
//        @Override
//        public boolean isInfoEnabled() {
//            return this.logger.isInfoEnabled();
//        }
//
//        @Override
//        public void debug(final String s) {
//            this.logger.debug(s);
//        }
//
//        @Override
//        public void debug(final String s, final Throwable ex) {
//            this.logger.debug(s, ex);
//        }
//
//        @Override
//        public void info(final String s, final Throwable ex) {
//            this.logger.info(s);
//        }
//
//        @Override
//        public void info(final String s) {
//            this.logger.info(s);
//        }
//
//        @Override
//        public void warn(final String s) {
//            this.logger.warn(s);
//        }
//
//        @Override
//        public void warn(final String s, final Throwable ex) {
//            this.logger.warn(s, ex);
//        }
//    }
    
    private static class JdkLogger extends Log
    {
        Logger logger;
        
        private JdkLogger(final Logger logger) {
            this.logger = logger;
        }
        
        @Override
        public boolean isDebugEnabled() {
            return this.logger.isLoggable(Level.FINE);
        }
        
        @Override
        public boolean isInfoEnabled() {
            return this.logger.isLoggable(Level.INFO);
        }
        
        @Override
        public void debug(final String s) {
            this.logger.logp(Level.FINE, null, null, s);
        }
        
        @Override
        public void debug(final String s, final Throwable ex) {
            this.logger.logp(Level.FINE, s, null, null, ex);
        }
        
        @Override
        public void info(final String s) {
            this.logger.logp(Level.INFO, null, null, s);
        }
        
        @Override
        public void info(final String s, final Throwable ex) {
            this.logger.logp(Level.INFO, null, null, s, ex);
        }
        
        @Override
        public void warn(final String s) {
            this.logger.logp(Level.WARNING, null, null, s);
        }
        
        @Override
        public void warn(final String s, final Throwable ex) {
            this.logger.logp(Level.WARNING, null, null, s, ex);
        }
    }
    
    public static class SuppressionCounter extends Log
    {
        AtomicInteger debugCount;
        AtomicInteger infoCount;
        AtomicInteger warnCount;
        
        public SuppressionCounter() {
            this.debugCount = new AtomicInteger();
            this.infoCount = new AtomicInteger();
            this.warnCount = new AtomicInteger();
        }
        
        @Override
        public boolean isDebugEnabled() {
            return true;
        }
        
        @Override
        public boolean isInfoEnabled() {
            return true;
        }
        
        @Override
        public void debug(final String s) {
            this.debugCount.incrementAndGet();
        }
        
        @Override
        public void debug(final String s, final Throwable ex) {
            this.debugCount.incrementAndGet();
        }
        
        @Override
        public void info(final String s, final Throwable ex) {
            this.infoCount.incrementAndGet();
        }
        
        @Override
        public void info(final String s) {
            this.infoCount.incrementAndGet();
        }
        
        @Override
        public void warn(final String s) {
            this.warnCount.incrementAndGet();
        }
        
        @Override
        public void warn(final String s, final Throwable ex) {
            this.warnCount.incrementAndGet();
        }
        
        public int getDebugCount() {
            return this.debugCount.get();
        }
        
        public int getInfoCount() {
            return this.infoCount.get();
        }
        
        public int getWarnCount() {
            return this.warnCount.get();
        }
    }
}
