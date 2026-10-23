//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraftforge.fml.common;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.ModContainer.Disableable;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.patcher.ClassPatchManager;
import net.minecraftforge.fml.common.versioning.*;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.FormattedMessage;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FMLModContainer implements ModContainer {
    private Object modInstance;
    private final File source;
    private ModMetadata modMetadata;
    private final String className;
    private final Map<String, Object> descriptor;
    private boolean enabled = true;
    private String internalVersion;
    private boolean overridesMetadata;
    private EventBus eventBus;
    private LoadController controller;
    private DefaultArtifactVersion processedVersion;
    private String annotationDependencies;
    private VersionRange minecraftAccepted;
    private Certificate certificate;
    private ILanguageAdapter languageAdapter;
    private ModContainer.Disableable disableability;
    private final ListMultimap<Class<? extends FMLEvent>, Method> eventMethods;
    private Map<String, String> customModProperties;
    private final ModCandidate candidate;
    private URL updateJSONUrl;
    private int classVersion;

    public FMLModContainer(String className, ModCandidate container, Map<String, Object> modDescriptor) {
        this.className = className;
        this.source = container.getModContainer();
        this.candidate = container;
        this.descriptor = modDescriptor;
        this.eventMethods = ArrayListMultimap.create();
        String modLanguage = (String) modDescriptor.get("modLanguage");
        String languageAdapterType = (String)modDescriptor.get("modLanguageAdapter");
        if (Strings.isNullOrEmpty(languageAdapterType)) {
            this.languageAdapter = (ILanguageAdapter)("scala".equals(modLanguage) ? new ILanguageAdapter.ScalaAdapter() : new ILanguageAdapter.JavaAdapter());
        } else {
            this.languageAdapter = null;
            FMLLog.log.trace("Using custom language adapter {} for {} (modid: {})", languageAdapterType, this.className, this.getModId());
        }

        //ClassPatchManager
        //

        this.sanityCheckModId();
    }

    private void sanityCheckModId() {
        String modid = (String)this.descriptor.get("modid");
        if (Strings.isNullOrEmpty(modid)) {
            modid = (String)this.descriptor.get("value");
            if (Strings.isNullOrEmpty(modid)) {
                throw new IllegalArgumentException("The modId is null or empty");
            }

            FMLLog.bigWarning("The mod {} has been identified as a probable 1.13 or greater mod. Things will not work well.", new Object[]{modid});
            this.descriptor.put("acceptedMinecraftVersions", "[1.13,)");
            this.descriptor.put("modid", modid);
        }

        if (modid.length() > 64) {
            throw new IllegalArgumentException(String.format("The modId %s is longer than the maximum of 64 characters.", modid));
        } else if (!modid.equals(modid.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException(String.format("The modId %s must be all lowercase.", modid));
        }
    }

    private ILanguageAdapter getLanguageAdapter() {
        if (this.languageAdapter == null) {
            try {
                this.languageAdapter = (ILanguageAdapter)Class.forName((String)this.descriptor.get("modLanguageAdapter"), true, Loader.instance().getModClassLoader()).newInstance();
            } catch (Exception var2) {
                FMLLog.log.error("Error constructing custom mod language adapter referenced by {} (modid: {})", this.className, this.getModId(), var2);
                throw new RuntimeException(var2);
            }
        }

        return this.languageAdapter;
    }

    public String getModId() {
        return (String)this.descriptor.get("modid");
    }

    public String getName() {
        return this.modMetadata.name;
    }

    public String getVersion() {
        return this.internalVersion;
    }

    public File getSource() {
        return this.source;
    }

    public ModMetadata getMetadata() {
        return this.modMetadata;
    }

    public void bindMetadata(MetadataCollection mc) {
        this.modMetadata = mc.getMetadataForId(this.getModId(), this.descriptor);
        if (this.descriptor.containsKey("useMetadata")) {
            this.overridesMetadata = !(Boolean)this.descriptor.get("useMetadata");
        }

        if (!this.overridesMetadata && this.modMetadata.useDependencyInformation) {
            FMLLog.log.trace("Using mcmod dependency info for {}: {} {} {}", this.getModId(), this.modMetadata.requiredMods, this.modMetadata.dependencies, this.modMetadata.dependants);
        } else {
            this.annotationDependencies = (String)this.descriptor.get("dependencies");
            DependencyParser dependencyParser = new DependencyParser(this.getModId(), FMLCommonHandler.instance().getSide());
            DependencyParser.DependencyInfo info = dependencyParser.parseDependencies(this.annotationDependencies);
            info.dependants.addAll(Loader.instance().getInjectedBefore(this.getModId()));
            info.dependencies.addAll(Loader.instance().getInjectedAfter(this.getModId()));
            this.modMetadata.requiredMods = info.requirements;
            this.modMetadata.dependencies = info.dependencies;
            this.modMetadata.dependants = info.dependants;
            FMLLog.log.trace("Parsed dependency info for {}: Requirements: {} After:{} Before:{}", this.getModId(), info.requirements, info.dependencies, info.dependants);
        }

        if (Strings.isNullOrEmpty(this.modMetadata.name)) {
            FMLLog.log.info("Mod {} is missing the required element 'name'. Substituting {}", this.getModId(), this.getModId());
            this.modMetadata.name = this.getModId();
        }

        this.internalVersion = (String)this.descriptor.get("version");
        if (Strings.isNullOrEmpty(this.internalVersion)) {
            Properties versionProps = this.searchForVersionProperties();
            if (versionProps != null) {
                this.internalVersion = versionProps.getProperty(this.getModId() + ".version");
                FMLLog.log.debug("Found version {} for mod {} in version.properties, using", this.internalVersion, this.getModId());
            }
        }

        if (Strings.isNullOrEmpty(this.internalVersion) && !Strings.isNullOrEmpty(this.modMetadata.version)) {
            FMLLog.log.warn("Mod {} is missing the required element 'version' and a version.properties file could not be found. Falling back to metadata version {}", this.getModId(), this.modMetadata.version);
            this.internalVersion = this.modMetadata.version;
        }

        if (Strings.isNullOrEmpty(this.internalVersion)) {
            FMLLog.log.warn("Mod {} is missing the required element 'version' and no fallback can be found. Substituting '1.0'.", this.getModId());
            this.modMetadata.version = this.internalVersion = "1.0";
        }

        String mcVersionString = (String)this.descriptor.get("acceptedMinecraftVersions");
        if ("[1.12]".equals(mcVersionString)) {
            mcVersionString = "[1.12,1.12.2]";
        }

        if ("[1.12.1]".equals(mcVersionString) || "[1.12,1.12.1]".equals(mcVersionString)) {
            mcVersionString = "[1.12,1.12.2]";
        }

        if (!Strings.isNullOrEmpty(mcVersionString)) {
            this.minecraftAccepted = VersionParser.parseRange(mcVersionString);
        } else {
            this.minecraftAccepted = Loader.instance().getMinecraftModContainer().getStaticVersionRange();
        }

        String jsonURL = (String)this.descriptor.get("updateJSON");
        if (!Strings.isNullOrEmpty(jsonURL)) {
            try {
                this.updateJSONUrl = new URL(jsonURL);
            } catch (MalformedURLException var5) {
                FMLLog.log.debug("Specified json URL for mod '{}' is invalid: {}", this.getModId(), jsonURL);
            }
        }

    }

//    @Nullable
    public Properties searchForVersionProperties() {
        try {
            FMLLog.log.debug("Attempting to load the file version.properties from {} to locate a version number for mod {}", this.getSource().getName(), this.getModId());
            Properties version = null;
            if (this.getSource().isFile()) {
                ZipFile source = new ZipFile(this.getSource());
                ZipEntry versionFile = source.getEntry("version.properties");
                if (versionFile != null) {
                    version = new Properties();
                    InputStream sourceInputStream = source.getInputStream(versionFile);

                    try {
                        version.load(sourceInputStream);
                    } finally {
                        IOUtils.closeQuietly(sourceInputStream);
                    }
                }

                source.close();
            } else if (this.getSource().isDirectory()) {
                File propsFile = new File(this.getSource(), "version.properties");
                if (propsFile.exists() && propsFile.isFile()) {
                    version = new Properties();
                    FileInputStream fis = new FileInputStream(propsFile);
                    Throwable var26 = null;

                    try {
                        version.load(fis);
                    } catch (Throwable var20) {
                        var26 = var20;
                        throw var20;
                    } finally {
                        if (fis != null) {
                            if (var26 != null) {
                                try {
                                    fis.close();
                                } catch (Throwable var19) {
                                    var26.addSuppressed(var19);
                                }
                            } else {
                                fis.close();
                            }
                        }

                    }
                }
            }

            return version;
        } catch (IOException var23) {
            FMLLog.log.trace("Failed to find a usable version.properties file for mod {}", this.getModId());
            return null;
        }
    }

    public void setEnabledState(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<ArtifactVersion> getRequirements() {
        return this.modMetadata.requiredMods;
    }

    public List<ArtifactVersion> getDependencies() {
        return this.modMetadata.dependencies;
    }

    public List<ArtifactVersion> getDependants() {
        return this.modMetadata.dependants;
    }

    public String getSortingRules() {
        return !this.overridesMetadata && this.modMetadata.useDependencyInformation ? this.modMetadata.printableSortingRules() : Strings.nullToEmpty(this.annotationDependencies);
    }

    public boolean matches(Object mod) {
        return mod == this.modInstance;
    }

    public Object getMod() {
        return this.modInstance;
    }

    public boolean registerBus(EventBus bus, LoadController controller) {
        if (this.enabled) {
            FMLLog.log.debug("Enabling mod {}", this.getModId());
            this.eventBus = bus;
            this.controller = controller;
            this.eventBus.register(this);
            return true;
        } else {
            return false;
        }
    }

//    @Nullable
    private Method gatherAnnotations(Class<?> clazz) {
        Method factoryMethod = null;
        Method[] var3 = clazz.getDeclaredMethods();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Method m = var3[var5];
            Annotation[] var7 = m.getAnnotations();
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                Annotation a = var7[var9];
                if (a.annotationType().equals(Mod.EventHandler.class)) {
                    if (m.getParameterTypes().length == 1 && FMLEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        m.setAccessible(true);
                        Class<? extends FMLEvent> parameterType = (Class<? extends FMLEvent>) m.getParameterTypes()[0];
                        this.eventMethods.put(parameterType, m);
                    } else {
                        FMLLog.log.error("The mod {} appears to have an invalid event annotation {}. This annotation can only apply to methods with recognized event arguments - it will not be called", this.getModId(), a.annotationType().getSimpleName());
                    }
                } else if (a.annotationType().equals(Mod.InstanceFactory.class)) {
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0 && factoryMethod == null) {
                        m.setAccessible(true);
                        factoryMethod = m;
                    } else if (Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0) {
                        if (factoryMethod != null) {
                            FMLLog.log.error("The InstanceFactory annotation can only be used once, the application to {}({}) will be ignored for mod {}", m.getName(), Arrays.asList(m.getParameterTypes()), this.getModId());
                        }
                    } else {
                        FMLLog.log.error("The InstanceFactory annotation can only apply to a static method, taking zero arguments - it will be ignored on {}({}) for mod {}", m.getName(), Arrays.asList(m.getParameterTypes()), this.getModId());
                    }
                }
            }
        }

        return factoryMethod;
    }

    private void processFieldAnnotations(ASMDataTable asmDataTable) throws IllegalAccessException {
        SetMultimap<String, ASMDataTable.ASMData> annotations = asmDataTable.getAnnotationsFor(this);
        this.parseSimpleFieldAnnotation(annotations, Mod.Instance.class.getName(), ModContainer::getMod);
        this.parseSimpleFieldAnnotation(annotations, Mod.Metadata.class.getName(), ModContainer::getMetadata);
    }

    private void parseSimpleFieldAnnotation(SetMultimap<String, ASMDataTable.ASMData> annotations, String annotationClassName, Function<ModContainer, Object> retriever) throws IllegalAccessException {
        Set<ASMDataTable.ASMData> mods = annotations.get(Mod.class.getName());
        String[] annName = annotationClassName.split("\\.");
        String annotationName = annName[annName.length - 1];
        Iterator var7 = annotations.get(annotationClassName).iterator();

        while(true) {
            while(var7.hasNext()) {
                ASMDataTable.ASMData targets = (ASMDataTable.ASMData)var7.next();
                String targetMod = (String)targets.getAnnotationInfo().get("value");
                String owner = (String)targets.getAnnotationInfo().get("owner");
                if (Strings.isNullOrEmpty(owner)) {
                    owner = ASMDataTable.getOwnerModID(mods, targets);
                    if (Strings.isNullOrEmpty(owner)) {
                        FMLLog.bigWarning("Could not determine owning mod for @{} on {} for mod {}", new Object[]{annotationClassName, targets.getClassName(), this.getModId()});
                        continue;
                    }
                }

                if (!this.getModId().equals(owner)) {
                    FMLLog.log.debug("Skipping @{} injection for {}.{} since it is not for mod {}", annotationClassName, targets.getClassName(), targets.getObjectName(), this.getModId());
                } else {
                    Field f = null;
                    Object injectedMod = null;
                    ModContainer mc = this;
                    boolean isStatic = false;
                    Class<?> clz = this.modInstance.getClass();
                    if (!Strings.isNullOrEmpty(targetMod)) {
                        if (Loader.isModLoaded(targetMod)) {
                            mc = (ModContainer)Loader.instance().getIndexedModList().get(targetMod);
                        } else {
                            mc = null;
                        }
                    }

                    if (mc != null) {
                        try {
                            clz = Class.forName(targets.getClassName(), true, Loader.instance().getModClassLoader());
                            f = clz.getDeclaredField(targets.getObjectName());
                            f.setAccessible(true);
                            isStatic = Modifier.isStatic(f.getModifiers());
                            injectedMod = retriever.apply(mc);
                        } catch (ReflectiveOperationException var17) {
                            FMLLog.log.warn("Attempting to load @{} in class {} for {} and failing", annotationName, targets.getClassName(), ((ModContainer)mc).getModId(), var17);
                        }
                    }

                    if (f != null) {
                        Object target = null;
                        if (!isStatic) {
                            target = this.modInstance;
                            if (!this.modInstance.getClass().equals(clz)) {
                                FMLLog.log.warn("Unable to inject @{} in non-static field {}.{} for {} as it is NOT the primary mod instance", annotationName, targets.getClassName(), targets.getObjectName(), ((ModContainer)mc).getModId());
                                continue;
                            }
                        }

                        f.set(target, injectedMod);
                    }
                }
            }

            return;
        }
    }

    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        ModClassLoader modClassLoader = event.getModClassLoader();

        try {
            modClassLoader.addFile(this.source);
        } catch (MalformedURLException var17) {
            FormattedMessage message = new FormattedMessage("{} Failed to add file to classloader: {}", this.getModId(), this.source);
            throw new LoaderException(message.getFormattedMessage(), var17);
        }

        modClassLoader.clearNegativeCacheFor(this.candidate.getClassList());
        MinecraftForge.preloadCrashClasses(event.getASMHarvestedData(), this.getModId(), this.candidate.getClassList());

        Class clazz;
        try {
            clazz = Class.forName(this.className, true, modClassLoader);
        } catch (ClassNotFoundException var16) {
            FormattedMessage message = new FormattedMessage("{} Failed load class: {}", this.getModId(), this.className);
            throw new LoaderException(message.getFormattedMessage(), var16);
        }

        boolean fingerprintNotPresent = true;

        Set<String> sourceFingerprints = null;
        String expectedFingerprint = null;

        try {
            ProtectionDomain domain = clazz.getProtectionDomain();
            if(domain != null) {
                CodeSource src = domain.getCodeSource();
                if(src != null) {
                    Certificate[] certificates = src.getCertificates();
                    ImmutableList<String> certList = CertificateHelper.getFingerprints(certificates);
                    sourceFingerprints = ImmutableSet.copyOf(certList);
                    expectedFingerprint = (String) this.descriptor.get("certificateFingerprint");
                    if (expectedFingerprint != null && !expectedFingerprint.isEmpty()) {
                        if (!sourceFingerprints.contains(expectedFingerprint)) {
                            Level warnLevel = this.source.isDirectory() ? Level.TRACE : Level.ERROR;
                            FMLLog.log.log(warnLevel, "The mod {} is expecting signature {} for source {}, however there is no signature matching that description", this.getModId(), expectedFingerprint, this.source.getName());
                        } else {
                            this.certificate = certificates[certList.indexOf(expectedFingerprint)];
                            fingerprintNotPresent = false;
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        List<Map<String, String>> props = (List)this.descriptor.get("customProperties");
        if (props != null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            Iterator var9 = props.iterator();

            while(var9.hasNext()) {
                Map<String, String> p = (Map)var9.next();
                builder.put(p.get("k"), p.get("v"));
            }

            this.customModProperties = builder.build();
        } else {
            this.customModProperties = EMPTY_PROPERTIES;
        }

        Boolean hasDisableableFlag = (Boolean)this.descriptor.get("canBeDeactivated");
        boolean hasReverseDepends = !event.getReverseDependencies().get(this.getModId()).isEmpty();
        if (hasDisableableFlag != null && hasDisableableFlag) {
            this.disableability = hasReverseDepends ? Disableable.DEPENDENCIES : Disableable.YES;
        } else {
            this.disableability = hasReverseDepends ? Disableable.DEPENDENCIES : Disableable.RESTART;
        }

        Method factoryMethod = this.gatherAnnotations(clazz);
        ILanguageAdapter languageAdapter = this.getLanguageAdapter();

        FormattedMessage message;
        try {
            this.modInstance = languageAdapter.getNewInstance(this, clazz, modClassLoader, factoryMethod);
        } catch (Exception var15) {
            message = new FormattedMessage("{} Failed to load new mod instance.", this.getModId());
            throw new LoaderException(message.getFormattedMessage(), var15);
        }

        NetworkRegistry.INSTANCE.register(this, clazz, (String)((String)this.descriptor.getOrDefault("acceptableRemoteVersions", (Object)null)), event.getASMHarvestedData());
        if (fingerprintNotPresent && sourceFingerprints != null) {
            this.eventBus.post(new FMLFingerprintViolationEvent(this.source.isDirectory(), this.source, ImmutableSet.copyOf(sourceFingerprints), expectedFingerprint));
        }

        ProxyInjector.inject(this, event.getASMHarvestedData(), FMLCommonHandler.instance().getSide(), languageAdapter);
        AutomaticEventSubscriber.inject(this, event.getASMHarvestedData(), FMLCommonHandler.instance().getSide());
        ConfigManager.sync(this.getModId(), Type.INSTANCE);

        try {
            this.processFieldAnnotations(event.getASMHarvestedData());
        } catch (IllegalAccessException var14) {
            message = new FormattedMessage("{} Failed to process field annotations.", this.getModId());
            throw new LoaderException(message.getFormattedMessage(), var14);
        }
    }

    @Subscribe
    public void handleModStateEvent(FMLEvent event) {
        if (this.eventMethods.containsKey(event.getClass())) {
            try {
                Iterator var2 = this.eventMethods.get(event.getClass()).iterator();

                while(var2.hasNext()) {
                    Method m = (Method)var2.next();
                    m.invoke(this.modInstance, event);
                }
            } catch (Throwable var4) {
                this.controller.errorOccurred(this, var4);
            }

        }
    }

    public ArtifactVersion getProcessedVersion() {
        if (this.processedVersion == null) {
            this.processedVersion = new DefaultArtifactVersion(this.getModId(), this.getVersion());
        }

        return this.processedVersion;
    }

    public boolean isImmutable() {
        return false;
    }

    public String getDisplayVersion() {
        return this.modMetadata.version;
    }

    public VersionRange acceptableMinecraftVersionRange() {
        return this.minecraftAccepted;
    }

    public Certificate getSigningCertificate() {
        return this.certificate;
    }

    public String toString() {
        return "FMLMod:" + this.getModId() + "{" + this.getVersion() + "}";
    }

    public Map<String, String> getCustomModProperties() {
        return this.customModProperties;
    }

    public Class<?> getCustomResourcePackClass() {
        try {
            return this.getSource().isDirectory() ? Class.forName("net.minecraftforge.fml.client.FMLFolderResourcePack", true, this.getClass().getClassLoader()) : Class.forName("net.minecraftforge.fml.client.FMLFileResourcePack", true, this.getClass().getClassLoader());
        } catch (ClassNotFoundException var2) {
            return null;
        }
    }

    public Map<String, String> getSharedModDescriptor() {
        Map<String, String> descriptor = Maps.newHashMap();
        descriptor.put("modsystem", "FML");
        descriptor.put("id", this.getModId());
        descriptor.put("version", this.getDisplayVersion());
        descriptor.put("name", this.getName());
        descriptor.put("url", this.modMetadata.url);
        descriptor.put("authors", this.modMetadata.getAuthorList());
        descriptor.put("description", this.modMetadata.description);
        return descriptor;
    }

    public ModContainer.Disableable canBeDisabled() {
        return this.disableability;
    }

    public String getGuiClassName() {
        return (String)this.descriptor.get("guiFactory");
    }

    public List<String> getOwnedPackages() {
        return this.candidate.getContainedPackages();
    }

    private boolean isTrue(Boolean value) {
        return value == null ? false : value;
    }

    public boolean shouldLoadInEnvironment() {
        boolean clientSideOnly = this.isTrue((Boolean)this.descriptor.get("clientSideOnly"));
        boolean serverSideOnly = this.isTrue((Boolean)this.descriptor.get("serverSideOnly"));
        if (clientSideOnly && serverSideOnly) {
            throw new RuntimeException("Mod annotation claims to be both client and server side only!");
        } else {
            Side side = FMLCommonHandler.instance().getSide();
            if (clientSideOnly && side != Side.CLIENT) {
                FMLLog.log.info("Disabling mod {} it is client side only.", this.getModId());
                return false;
            } else if (serverSideOnly && side != Side.SERVER) {
                FMLLog.log.info("Disabling mod {} it is server side only.", this.getModId());
                return false;
            } else {
                return true;
            }
        }
    }

    public URL getUpdateUrl() {
        return this.updateJSONUrl;
    }

    public void setClassVersion(int classVersion) {
        this.classVersion = classVersion;
    }

    public int getClassVersion() {
        return this.classVersion;
    }
}
