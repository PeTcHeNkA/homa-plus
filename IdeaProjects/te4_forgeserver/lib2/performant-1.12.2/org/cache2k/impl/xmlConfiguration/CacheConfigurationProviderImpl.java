//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Te4hnoPlus\Desktop\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package org.cache2k.impl.xmlConfiguration;

import org.cache2k.core.spi.*;
import org.cache2k.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.cache2k.configuration.*;

public class CacheConfigurationProviderImpl implements CacheConfigurationProvider
{
    private static final String DEFAULT_CONFIGURATION_FILE = "cache2k.xml";
    private static final Map<String, String> version1SectionTypes;
    private PropertyParser propertyParser;
    private TokenizerFactory tokenizerFactory;
    private volatile Map<Class<?>, BeanPropertyMutator> type2mutator;
    private volatile Map<CacheManager, ConfigurationContext> manager2defaultConfig;
    private volatile Map<ClassLoader, ConfigurationContext> classLoader2config;
    
    public CacheConfigurationProviderImpl() {
        this.propertyParser = new StandardPropertyParser();
        this.tokenizerFactory = new FlexibleXmlTokenizerFactory();
        this.type2mutator = new HashMap<Class<?>, BeanPropertyMutator>();
        this.manager2defaultConfig = new HashMap<CacheManager, ConfigurationContext>();
        this.classLoader2config = new HashMap<ClassLoader, ConfigurationContext>();
    }
    
    public String getDefaultManagerName(final ClassLoader cl) {
        ConfigurationContext ctx = this.classLoader2config.get(cl);
        if (ctx == null) {
            ctx = this.createContext(cl, null, "cache2k.xml");
            final Map<ClassLoader, ConfigurationContext> m2 = new HashMap<ClassLoader, ConfigurationContext>(this.classLoader2config);
            m2.put(cl, ctx);
            this.classLoader2config = m2;
        }
        return ctx.getManagerConfiguration().getDefaultManagerName();
    }
    
    public Cache2kConfiguration getDefaultConfiguration(final CacheManager mgr) {
        final Cache2kConfiguration cfg = this.getManagerContext(mgr).getDefaultManagerConfiguration();
        return copyViaSerialization(mgr, cfg);
    }
    
    public <K, V> void augmentConfiguration(final CacheManager mgr, final Cache2kConfiguration<K, V> cfg) {
        final ConfigurationContext ctx = this.getManagerContext(mgr);
        if (!ctx.isConfigurationPresent()) {
            return;
        }
        final String _cacheName = cfg.getName();
        if (_cacheName == null) {
            if (ctx.getManagerConfiguration().isIgnoreAnonymousCache()) {
                return;
            }
            throw new ConfigurationException("Cache name missing, cannot apply XML configuration. Consider parameter: ignoreAnonymousCache");
        }
        else {
            final ParsedConfiguration _parsedTop = this.readManagerConfigurationWithExceptionHandling(mgr.getClassLoader(), getFileName(mgr));
            final ParsedConfiguration _section = this.extractCachesSection(_parsedTop);
            ParsedConfiguration _parsedCache = null;
            if (_section != null) {
                _parsedCache = _section.getSection(_cacheName);
            }
            if (_parsedCache != null) {
                this.apply(ctx, _parsedCache, cfg);
                return;
            }
            if (ctx.getManagerConfiguration().isIgnoreMissingCacheConfiguration()) {
                return;
            }
            final String _exceptionText = "Configuration for cache '" + _cacheName + "' is missing. Consider parameter: ignoreMissingCacheConfiguration at " + _parsedTop.getSource();
            throw new IllegalArgumentException(_exceptionText);
        }
    }
    
    public Iterable<String> getConfiguredCacheNames(final CacheManager mgr) {
        final ConfigurationContext ctx = this.getManagerContext(mgr);
        if (!ctx.isConfigurationPresent()) {
            return Collections.emptyList();
        }
        final ParsedConfiguration _parsedTop = this.readManagerConfigurationWithExceptionHandling(mgr.getClassLoader(), getFileName(mgr));
        final ParsedConfiguration _section = this.extractCachesSection(_parsedTop);
        if (_section == null) {
            return Collections.emptyList();
        }
        final List<String> _names = new ArrayList<String>();
        for (final ParsedConfiguration pc : _section.getSections()) {
            _names.add(pc.getName());
        }
        return _names;
    }
    
    private static String getFileName(final CacheManager mgr) {
        if (mgr.isDefaultManager()) {
            return "cache2k.xml";
        }
        return "cache2k-" + mgr.getName() + ".xml";
    }
    
    private ParsedConfiguration readManagerConfiguration(final ClassLoader cl, final String _fileName) throws Exception {
        final InputStream is = cl.getResourceAsStream(_fileName);
        if (is == null) {
            return null;
        }
        final ConfigurationTokenizer tkn = this.tokenizerFactory.createTokenizer(_fileName, is, null);
        final ParsedConfiguration cfg = ConfigurationParser.parse(tkn);
        is.close();
        final VariableExpander _expander = new StandardVariableExpander();
        _expander.expand(cfg);
        return cfg;
    }
    
    private ParsedConfiguration extractCachesSection(final ParsedConfiguration pc) {
        final ParsedConfiguration _cachesSection = pc.getSection("caches");
        if (_cachesSection == null) {
            return null;
        }
        return _cachesSection;
    }
    
    private void checkCacheConfigurationOnStartup(final ConfigurationContext ctx, final ParsedConfiguration pc) {
        final ParsedConfiguration _cachesSection = pc.getSection("caches");
        if (_cachesSection == null) {
            return;
        }
        for (final ParsedConfiguration _cacheConfig : _cachesSection.getSections()) {
            this.apply(ctx, _cacheConfig, new Cache2kConfiguration());
        }
    }
    
    private ConfigurationContext getManagerContext(final CacheManager mgr) {
        ConfigurationContext ctx = this.manager2defaultConfig.get(mgr);
        if (ctx != null) {
            return ctx;
        }
        synchronized (this) {
            ctx = this.manager2defaultConfig.get(mgr);
            if (ctx != null) {
                return ctx;
            }
            if (mgr.isDefaultManager()) {
                ctx = this.classLoader2config.get(mgr.getClassLoader());
            }
            if (ctx == null) {
                ctx = this.createContext(mgr.getClassLoader(), mgr.getName(), getFileName(mgr));
            }
            final Map<CacheManager, ConfigurationContext> m2 = new HashMap<CacheManager, ConfigurationContext>(this.manager2defaultConfig);
            m2.put(mgr, ctx);
            this.manager2defaultConfig = m2;
            return ctx;
        }
    }
    
    private ConfigurationContext createContext(final ClassLoader cl, final String _managerName, final String _fileName_fileName) {
        final ParsedConfiguration pc = this.readManagerConfigurationWithExceptionHandling(cl, _fileName_fileName);
        final ConfigurationContext ctx = new ConfigurationContext();
        ctx.setClassLoader(cl);
        final Cache2kConfiguration _defaultConfiguration = new Cache2kConfiguration();
        ctx.setDefaultManagerConfiguration((Cache2kConfiguration<?, ?>)_defaultConfiguration);
        ctx.getManagerConfiguration().setDefaultManagerName(_managerName);
        if (pc != null) {
            _defaultConfiguration.setExternalConfigurationPresent(true);
            ctx.setTemplates(this.extractTemplates(pc));
            this.apply(ctx, pc, ctx.getManagerConfiguration());
            if (ctx.getManagerConfiguration().getVersion() != null && ctx.getManagerConfiguration().getVersion().startsWith("1.")) {
                ctx.setPredefinedSectionTypes(CacheConfigurationProviderImpl.version1SectionTypes);
            }
            this.applyDefaultConfigurationIfPresent(ctx, pc, _defaultConfiguration);
            ctx.setConfigurationPresent(true);
            if (!ctx.getManagerConfiguration().isSkipCheckOnStartup()) {
                this.checkCacheConfigurationOnStartup(ctx, pc);
            }
        }
        return ctx;
    }
    
    private ParsedConfiguration readManagerConfigurationWithExceptionHandling(final ClassLoader cl, final String _fileName) {
        ParsedConfiguration pc;
        try {
            pc = this.readManagerConfiguration(cl, _fileName);
        }
        catch (CacheException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            throw new ConfigurationException("Reading configuration for manager from '" + _fileName + "'", ex2);
        }
        return pc;
    }
    
    private void applyDefaultConfigurationIfPresent(final ConfigurationContext ctx, final ParsedConfiguration _pc, final Cache2kConfiguration _defaultConfiguration) {
        ParsedConfiguration _defaults = _pc.getSection("defaults");
        if (_defaults != null) {
            _defaults = _defaults.getSection("cache");
        }
        if (_defaults != null) {
            this.apply(ctx, _defaults, _defaultConfiguration);
        }
    }
    
    private static <T extends Serializable> T copyViaSerialization(final CacheManager mgr, final T obj) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            final ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            return (T)new ObjectInputStream(bin).readObject();
        }
        catch (Exception ex) {
            throw new ConfigurationException("Copying default cache configuration for manager '" + mgr.getName() + "'", ex);
        }
    }
    
    void apply(final ConfigurationContext ctx, final ParsedConfiguration _parsedCfg, final Object cfg) {
        final ParsedConfiguration _templates = ctx.getTemplates();
        final ConfigurationTokenizer.Property _include = _parsedCfg.getPropertyMap().get("include");
        if (_include != null) {
            for (final String _template : _include.getValue().split(",")) {
                ParsedConfiguration c2 = null;
                if (_templates != null) {
                    c2 = _templates.getSection(_template);
                }
                if (c2 == null) {
                    throw new ConfigurationException("Template not found '" + _template + "'", _include);
                }
                this.apply(ctx, c2, cfg);
            }
        }
        this.applyPropertyValues(_parsedCfg, cfg);
        if (!(cfg instanceof ConfigurationWithSections)) {
            return;
        }
        final ConfigurationWithSections _configurationWithSections = (ConfigurationWithSections)cfg;
        for (final ParsedConfiguration _parsedSection : _parsedCfg.getSections()) {
            String _sectionType = ctx.getPredefinedSectionTypes().get(_parsedSection.getName());
            if (_sectionType == null) {
                _sectionType = _parsedSection.getType();
            }
            if (_sectionType == null) {
                throw new ConfigurationException("type missing or unknown", _parsedSection);
            }
            Class<?> _type;
            try {
                _type = Class.forName(_sectionType);
            }
            catch (ClassNotFoundException ex) {
                throw new ConfigurationException("class not found '" + _sectionType + "'", _parsedSection);
            }
            if (!this.handleSection(ctx, _type, _configurationWithSections, _parsedSection) && !this.handleCollection(ctx, _type, cfg, _parsedSection) && !this.handleBean(ctx, _type, cfg, _parsedSection)) {
                throw new ConfigurationException("Unknown property  '" + _parsedSection.getContainer() + "'", _parsedSection);
            }
        }
    }
    
    private boolean handleBean(final ConfigurationContext ctx, final Class<?> _type, final Object cfg, final ParsedConfiguration _parsedCfg) {
        final String _containerName = _parsedCfg.getContainer();
        final BeanPropertyMutator m = this.provideMutator(cfg.getClass());
        final Class<?> _targetType = (Class<?>)m.getType(_containerName);
        if (_targetType == null) {
            return false;
        }
        if (!_targetType.isAssignableFrom(_type)) {
            throw new ConfigurationException("Type mismatch, expected: '" + _targetType.getName() + "'", _parsedCfg);
        }
        final Object _bean = this.createBeanAndApplyConfiguration(ctx, _type, _parsedCfg);
        this.mutateAndCatch(cfg, m, _containerName, _bean, _parsedCfg, _bean);
        return true;
    }
    
    private boolean handleCollection(final ConfigurationContext ctx, final Class<?> _type, final Object cfg, final ParsedConfiguration _parsedCfg) {
        final String _containerName = _parsedCfg.getContainer();
        Method m;
        try {
            m = cfg.getClass().getMethod(constructGetterName(_containerName), (Class<?>[])new Class[0]);
        }
        catch (NoSuchMethodException ex3) {
            return false;
        }
        if (!Collection.class.isAssignableFrom(m.getReturnType())) {
            return false;
        }
        Collection c;
        try {
            c = (Collection)m.invoke(cfg, new Object[0]);
        }
        catch (Exception ex) {
            throw new ConfigurationException("Cannot access collection for '" + _containerName + "' " + ex, _parsedCfg);
        }
        final Object _bean = this.createBeanAndApplyConfiguration(ctx, _type, _parsedCfg);
        try {
            c.add(_bean);
        }
        catch (IllegalArgumentException ex2) {
            throw new ConfigurationException("Rejected add '" + _containerName + "': " + ex2.getMessage(), _parsedCfg);
        }
        return true;
    }
    
    private static String constructGetterName(final String _containerName) {
        return "get" + Character.toUpperCase(_containerName.charAt(0)) + _containerName.substring(1);
    }
    
    private Object createBeanAndApplyConfiguration(final ConfigurationContext ctx, final Class<?> _type, final ParsedConfiguration _parsedCfg) {
        Object _bean;
        try {
            _bean = _type.newInstance();
        }
        catch (Exception ex) {
            throw new ConfigurationException("Cannot instantiate bean: " + ex, _parsedCfg);
        }
        final ParsedConfiguration _parameters = _parsedCfg.getSection("parameters");
        this.apply(ctx, (_parameters != null) ? _parameters : _parsedCfg, _bean);
        if (_bean instanceof ValidatingConfigurationBean) {
            try {
                ((ValidatingConfigurationBean)_bean).validate();
            }
            catch (IllegalArgumentException ex2) {
                throw new ConfigurationException("Validation error '" + _bean.getClass().getSimpleName() + "': " + ex2.getMessage(), _parsedCfg);
            }
        }
        return _bean;
    }
    
    private boolean handleSection(final ConfigurationContext ctx, final Class<?> _type, final ConfigurationWithSections cfg, final ParsedConfiguration sc) {
        final String _containerName = sc.getContainer();
        if (!"sections".equals(_containerName)) {
            return false;
        }
        ConfigurationSection _sectionBean = cfg.getSections().getSection((Class)_type);
        if (!(_sectionBean instanceof SingletonConfigurationSection)) {
            try {
                _sectionBean = (ConfigurationSection)_type.newInstance();
            }
            catch (Exception ex) {
                throw new ConfigurationException("Cannot instantiate section class: " + ex, sc);
            }
            cfg.getSections().add(_sectionBean);
        }
        this.apply(ctx, sc, _sectionBean);
        return true;
    }
    
    private void applyPropertyValues(final ParsedConfiguration cfg, final Object _bean) {
        final BeanPropertyMutator m = this.provideMutator(_bean.getClass());
        for (final ConfigurationTokenizer.Property p : cfg.getPropertyMap().values()) {
            final Class<?> _propertyType = (Class<?>)m.getType(p.getName());
            if (_propertyType == null) {
                if ("include".equals(p.getName()) || "name".equals(p.getName())) {
                    continue;
                }
                if ("type".equals(p.getName())) {
                    continue;
                }
                throw new ConfigurationException("Unknown property '" + p.getName() + "'", p);
            }
            else {
                Object obj;
                try {
                    obj = this.propertyParser.parse(_propertyType, p.getValue());
                }
                catch (Exception ex) {
                    if (ex instanceof NumberFormatException) {
                        throw new ConfigurationException("Cannot parse number: '" + p.getValue() + "'", p);
                    }
                    if (ex instanceof IllegalArgumentException) {
                        throw new ConfigurationException("Value '" + p.getValue() + "' parse error: " + ex.getMessage(), p);
                    }
                    throw new ConfigurationException("Cannot parse property: " + ex, p);
                }
                this.mutateAndCatch(_bean, m, p, obj);
            }
        }
    }
    
    private void mutateAndCatch(final Object cfg, final BeanPropertyMutator m, final ConfigurationTokenizer.Property p, final Object obj) {
        this.mutateAndCatch(cfg, m, p.getName(), p.getValue(), p, obj);
    }
    
    private void mutateAndCatch(final Object cfg, final BeanPropertyMutator m, final String _name, final Object _valueForExceptionText, final SourceLocation loc, final Object obj) {
        try {
            m.mutate(cfg, _name, obj);
        }
        catch (InvocationTargetException ex) {
            final Throwable t = ex.getTargetException();
            if (t instanceof IllegalArgumentException) {
                throw new ConfigurationException("Value '" + _valueForExceptionText + "' rejected: " + t.getMessage(), loc);
            }
            throw new ConfigurationException("Setting property: " + ex, loc);
        }
        catch (Exception ex2) {
            throw new ConfigurationException("Setting property: " + ex2, loc);
        }
    }
    
    private BeanPropertyMutator provideMutator(final Class<?> _type) {
        BeanPropertyMutator m = this.type2mutator.get(_type);
        if (m == null) {
            synchronized (this) {
                m = new BeanPropertyMutator((Class)_type);
                final Map<Class<?>, BeanPropertyMutator> m2 = new HashMap<Class<?>, BeanPropertyMutator>(this.type2mutator);
                m2.put(_type, m);
                this.type2mutator = m2;
            }
        }
        return m;
    }
    
    private ParsedConfiguration extractTemplates(final ParsedConfiguration _pc) {
        return _pc.getSection("templates");
    }
    
    static {
        version1SectionTypes = new HashMap<String, String>() {
            {
                this.put("jcache", "org.cache2k.jcache.JCacheConfiguration");
                this.put("byClassName", CustomizationSupplierByClassName.class.getName());
            }
        };
    }
}
