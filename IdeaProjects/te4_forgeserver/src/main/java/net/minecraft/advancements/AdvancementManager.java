package net.minecraft.advancements;

import javax.annotation.*;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.*;
import com.google.common.collect.*;
import java.nio.charset.*;
import net.minecraft.item.crafting.*;
import java.util.*;
import org.apache.commons.io.*;
import java.nio.file.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;
import net.minecraft.util.text.*;
import net.minecraft.util.*;
import com.google.gson.*;
import static net.minecraft.server.MinecraftServer.LOGGER;


public class AdvancementManager {
    public static final Gson GSON;
    private static final AdvancementList ADVANCEMENT_LIST;
    private static boolean needInit = true;
    private final File advancementsDir;
    private boolean hasErrored;

    public AdvancementManager(@Nullable final File advancementsDirIn) {
        this(advancementsDirIn, null);
    }

    public AdvancementManager(@Nullable final File advancementsDirIn, @Nullable WorldServer world) {
        this.advancementsDir = advancementsDirIn;
        this.reload(world);
    }


    public void ensureLoaded(WorldServer world){
        this.reload(world);
    }


    public void reload() {
        reload(null);
    }


    private void reload(@Nullable WorldServer world) {
        if(world != null){
            if(needInit) {
                world.getMinecraftServer().ticker().runAsync(() -> {
                    try {
                        syncReload(world);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            return;
        }
        needInit = false;
        syncReload(world);
    }


    private void syncReload(@Nullable WorldServer world){
        synchronized (ADVANCEMENT_LIST) {
            if(world != null){
                if(!needInit)return;
            }
            needInit = false;

            this.hasErrored = false;
            AdvancementManager.ADVANCEMENT_LIST.clear();
            final Map<ResourceLocation, Advancement.Builder> map = this.loadCustomAdvancements();
            this.loadBuiltInAdvancements(map);
            this.hasErrored |= ForgeHooks.loadAdvancements(map);
            AdvancementManager.ADVANCEMENT_LIST.loadAdvancements(map);
            for (final Advancement advancement : AdvancementManager.ADVANCEMENT_LIST.getRoots()) {
                if (advancement.getDisplay() != null) {
                    AdvancementTreeNode.layout(advancement);
                }
            }
        }
    }


    public boolean hasErrored() {
        return this.hasErrored;
    }


    private Map<ResourceLocation, Advancement.Builder> loadCustomAdvancements() {
        if (this.advancementsDir == null) {
            return Maps.newHashMap();
        }
        final Map<ResourceLocation, Advancement.Builder> map = Maps.newHashMap();
        this.advancementsDir.mkdirs();
        for (final File file1 : FileUtils.listFiles(this.advancementsDir, new String[] { "json" }, true)) {
            final String s = FilenameUtils.removeExtension(this.advancementsDir.toURI().relativize(file1.toURI()).toString());
            final String[] astring = s.split("/", 2);
            if (astring.length == 2) {
                final ResourceLocation resourcelocation = new ResourceLocation(astring[0], astring[1]);
                try {
                    final Advancement.Builder advancement$builder = JsonUtils.gsonDeserialize(AdvancementManager.GSON, FileUtils.readFileToString(file1, StandardCharsets.UTF_8), Advancement.Builder.class);
                    if (advancement$builder == null) {
                        LOGGER.error("Couldn't load custom advancement " + resourcelocation + " from " + file1 + " as it's empty or null");
                    }
                    else {
                        map.put(resourcelocation, advancement$builder);
                    }
                }
                catch (IllegalArgumentException | JsonParseException ex2) {
                    LOGGER.error("Parsing error loading custom advancement {}", resourcelocation, ex2);
                    this.hasErrored = true;
                }
                catch (IOException ioexception) {
                    LOGGER.error("Couldn't read custom advancement {} from {}", resourcelocation, file1, ioexception);
                    this.hasErrored = true;
                }
            }
        }
        return map;
    }

    private void loadBuiltInAdvancements(final Map<ResourceLocation, Advancement.Builder> map) {
        FileSystem filesystem = null;
        try {
            final URL url = AdvancementManager.class.getResource("/assets/.mcassetsroot");
            if (url != null) {
                final URI uri = url.toURI();
                Path path;
                if ("file".equals(uri.getScheme())) {
                    path = Paths.get(CraftingManager.class.getResource("/assets/minecraft/advancements").toURI());
                }
                else {
                    if (!"jar".equals(uri.getScheme())) {
                        LOGGER.error("Unsupported scheme " + uri + " trying to list all built-in advancements (NYI?)");
                        this.hasErrored = true;
                        return;
                    }
                    filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    path = filesystem.getPath("/assets/minecraft/advancements", new String[0]);
                }
                Iterator<Path> iter = Files.walk(path).iterator();
                while (iter.hasNext()) {
                    Path path2 = iter.next();
                    if(path2 == null)break;
                    if ("json".equals(FilenameUtils.getExtension(path2.toString()))) {
                        final Path path3 = path.relativize(path2);
                        final String s = FilenameUtils.removeExtension(path3.toString()).replaceAll("\\\\", "/");
                        final ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);
                        if (map.containsKey(resourcelocation)) {
                            continue;
                        }
                        BufferedReader bufferedreader = null;
                        try {
                            bufferedreader = Files.newBufferedReader(path2);
                            final Advancement.Builder advancement$builder = JsonUtils.fromJson(AdvancementManager.GSON, bufferedreader, Advancement.Builder.class);
                            map.put(resourcelocation, advancement$builder);
                        }
                        catch (JsonParseException jsonparseexception) {
                            LOGGER.error("Parsing error loading built-in advancement " + resourcelocation, (Throwable)jsonparseexception);
                            this.hasErrored = true;
                        }
                        catch (IOException ioexception) {
                            LOGGER.error("Couldn't read advancement " + resourcelocation + " from " + path2, (Throwable)ioexception);
                            this.hasErrored = true;
                        }
                        finally {
                            IOUtils.closeQuietly(bufferedreader);
                        }
                    }
                }
                return;
            }
            LOGGER.error("Couldn't find .mcassetsroot");
            this.hasErrored = true;
        }
        catch (IOException ex) {}
        catch (URISyntaxException urisyntaxexception) {
            LOGGER.error("Couldn't get a list of all built-in advancement files", urisyntaxexception);
            this.hasErrored = true;
        }
        finally {
            IOUtils.closeQuietly(filesystem);
        }
    }

    @Nullable
    public Advancement getAdvancement(final ResourceLocation id) {
        return AdvancementManager.ADVANCEMENT_LIST.getAdvancement(id);
    }

    public Iterable<Advancement> getAdvancements() {
        return AdvancementManager.ADVANCEMENT_LIST.getAdvancements();
    }


    static {
        GSON = new GsonBuilder().registerTypeHierarchyAdapter(Advancement.Builder.class, new JsonDeserializer<Advancement.Builder>() {
            public Advancement.Builder deserialize(final JsonElement p_deserialize_1_, final Type p_deserialize_2_, final JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
                final JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "advancement");
                return Advancement.Builder.deserialize(jsonobject, p_deserialize_3_);
            }
        }).registerTypeAdapter(AdvancementRewards.class, new AdvancementRewards.Deserializer()).registerTypeHierarchyAdapter((Class)ITextComponent.class, (Object)new ITextComponent.Serializer()).registerTypeHierarchyAdapter((Class)Style.class, (Object)new Style.Serializer()).registerTypeAdapterFactory((TypeAdapterFactory)new EnumTypeAdapterFactory()).create();
        ADVANCEMENT_LIST = new AdvancementList();
    }
}
