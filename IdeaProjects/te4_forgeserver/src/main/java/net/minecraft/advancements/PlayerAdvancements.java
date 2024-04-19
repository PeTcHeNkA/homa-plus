package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketAdvancementInfo;
import net.minecraft.network.play.server.SPacketSelectAdvancementsTab;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PlayerAdvancements {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).setPrettyPrinting().create();
    private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> MAP_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {};
    private final MinecraftServer server;
    private final File progressFile;
    private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<>(8);
    private final Set<Advancement> visible           = new LinkedHashSet<>(4);
    private final Set<Advancement> visibilityChanged = new LinkedHashSet<>(4);
    private final Set<Advancement> progressChanged   = new LinkedHashSet<>(4);
    private EntityPlayerMP player;
    @Nullable
    private Advancement lastSelectedTab;
    private boolean isFirstPacket = true;

    public PlayerAdvancements(MinecraftServer server, File file, EntityPlayerMP player) {
        this.server = server;
        this.progressFile = file;
        this.player = player;
        this.load();
    }

    public void setPlayer(EntityPlayerMP player) {
        this.player = player;
    }


    public void dispose() {
        for (ICriterionTrigger<?> trigger : CriteriaTriggers.getAll()) {
            trigger.removeAllListeners(this);
        }
    }


    public void reload() {
        this.dispose();
        this.progress.clear();
        this.visible.clear();
        this.visibilityChanged.clear();
        this.progressChanged.clear();
        this.isFirstPacket = true;
        this.lastSelectedTab = null;
        this.load();
    }


    private void registerListeners() {
        for (Advancement advancement : this.server.getAdvancementManager().getAdvancements()) {
            this.registerListeners(advancement);
        }
    }


    private void ensureAllVisible() {
        List<Advancement> list = new ArrayList<>();

        for (Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            if (entry.getValue().isDone()) {
                list.add(entry.getKey());
                this.progressChanged.add(entry.getKey());
            }
        }
        for (Advancement advancement : list) {
            this.ensureVisibility(advancement);
        }
    }


    private void checkForAutomaticTriggers() {
        for (Advancement advancement : this.server.getAdvancementManager().getAdvancements()) {
            if (advancement.getCriteria().isEmpty()) {
                this.grantCriterion(advancement, "");
                advancement.getRewards().apply(this.player);
            }
        }
    }


    private void load() {
        if (this.progressFile.isFile()) {
            try {
                String s = Files.toString(this.progressFile, StandardCharsets.UTF_8);
                Map<ResourceLocation, AdvancementProgress> map = JsonUtils.gsonDeserialize(GSON, s, MAP_TOKEN.getType());

                if (map == null) {
                    throw new JsonParseException("Found null for advancements");
                }

                Stream<Entry<ResourceLocation, AdvancementProgress>> stream = map.entrySet().stream().sorted(Comparator.comparing(Entry::getValue));

                for (Entry<ResourceLocation, AdvancementProgress> entry : stream.collect(Collectors.toList())) {
                    Advancement advancement = this.server.getAdvancementManager().getAdvancement(entry.getKey());

                    if (advancement == null) {
                        LOGGER.warn("Ignored advancement '" + entry.getKey() + "' in progress file " + this.progressFile + " - it doesn't exist anymore?");
                    } else {
                        this.startProgress(advancement, entry.getValue());
                    }
                }
            } catch (JsonParseException jsonparseexception) {
                LOGGER.error("Couldn't parse player advancements in " + this.progressFile, jsonparseexception);
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't access player advancements in " + this.progressFile, ioexception);
            }
        }
        this.checkForAutomaticTriggers();
        this.ensureAllVisible();
        this.registerListeners();
    }


    public void save() {
        Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();

        for (Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            AdvancementProgress advancementprogress = entry.getValue();

            if (advancementprogress.hasProgress()) {
                map.put(entry.getKey().getId(), advancementprogress);
            }
        }
        if(server != null){
            server.ticker().runAsync(() -> saveToFile(map));
        } else {
            saveToFile(map);
        }
    }


    private void saveToFile(Map<ResourceLocation, AdvancementProgress> map){
        if (this.progressFile.getParentFile() != null) {
            this.progressFile.getParentFile().mkdirs();
        }

        try {
            Files.write(GSON.toJson(map), this.progressFile, StandardCharsets.UTF_8);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save player advancements to " + this.progressFile, ioexception);
        }
    }


    public boolean grantCriterion(Advancement advancement, String str) {
        // Forge: don't grant advancements for fake players
        if (this.player instanceof net.minecraftforge.common.util.FakePlayer) return false;

        boolean flag = false;
        AdvancementProgress advancementprogress = this.getProgress(advancement);
        boolean flag1 = advancementprogress.isDone();

        if (advancementprogress.grantCriterion(str)) {
            this.unregisterListeners(advancement);
            this.progressChanged.add(advancement);
            flag = true;

            if (!flag1 && advancementprogress.isDone()) {
                advancement.getRewards().apply(this.player);

                AdvancementEvent event = new AdvancementEvent(player, advancement);
                MinecraftForge.EVENT_BUS.post(event);

                if(event.getResult() != Event.Result.DENY) {
                    if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && this.player.world.getGameRules().getBoolean("announceAdvancements")) {
                        this.server.getPlayerList().sendMessage(new TextComponentTranslation("chat.type.advancement." + advancement.getDisplay().getFrame().getName(), this.player.getDisplayName(), advancement.getDisplayText()));
                    }
                }
            }
        }

        if (advancementprogress.isDone()) {
            this.ensureVisibility(advancement);
        }

        return flag;
    }


    public boolean revokeCriterion(Advancement advancement, String str) {
        boolean flag = false;
        AdvancementProgress advancementprogress = this.getProgress(advancement);

        if (advancementprogress.revokeCriterion(str)) {
            this.registerListeners(advancement);
            this.progressChanged.add(advancement);
            flag = true;
        }

        if (!advancementprogress.hasProgress())
        {
            this.ensureVisibility(advancement);
        }
        return flag;
    }


    private void registerListeners(Advancement advancement) {
        AdvancementProgress advancementprogress = this.getProgress(advancement);

        if (!advancementprogress.isDone()) {
            for (Entry<String, Criterion> entry : advancement.getCriteria().entrySet()) {
                CriterionProgress criterionprogress = advancementprogress.getCriterionProgress(entry.getKey());

                if (criterionprogress != null && !criterionprogress.isObtained()) {
                    ICriterionInstance icriterioninstance = entry.getValue().getCriterionInstance();

                    if (icriterioninstance != null) {
                        ICriterionTrigger<ICriterionInstance> trigger = CriteriaTriggers.<ICriterionInstance>get(icriterioninstance.getId());

                        if (trigger != null) {
                            trigger.addListener(this,
                                    new ICriterionTrigger.Listener(icriterioninstance, advancement, entry.getKey()));
                        }
                    }
                }
            }
        }
    }


    private void unregisterListeners(Advancement advancement) {
        AdvancementProgress advancementprogress = this.getProgress(advancement);

        for (Entry<String, Criterion> entry : advancement.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = advancementprogress.getCriterionProgress(entry.getKey());

            if (criterionprogress != null && (criterionprogress.isObtained() || advancementprogress.isDone())) {
                ICriterionInstance icriterioninstance = entry.getValue().getCriterionInstance();

                if (icriterioninstance != null) {
                    ICriterionTrigger<ICriterionInstance> trigger = CriteriaTriggers.<ICriterionInstance>get(icriterioninstance.getId());

                    if (trigger != null) {
                        trigger.removeListener(this,
                                new ICriterionTrigger.Listener(icriterioninstance, advancement, entry.getKey()));
                    }
                }
            }
        }
    }


    public void flushDirty(EntityPlayerMP player) {
        if (!this.visibilityChanged.isEmpty() || !this.progressChanged.isEmpty()) {
            Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();
            Set<Advancement> set = Sets.<Advancement>newLinkedHashSet();
            Set<ResourceLocation> set1 = Sets.<ResourceLocation>newLinkedHashSet();

            for (Advancement advancement : this.progressChanged) {
                if (this.visible.contains(advancement)) {
                    map.put(advancement.getId(), this.progress.get(advancement));
                }
            }

            for (Advancement advancement1 : this.visibilityChanged) {
                if (this.visible.contains(advancement1)) {
                    set.add(advancement1);
                } else {
                    set1.add(advancement1.getId());
                }
            }

            if (!map.isEmpty() || !set.isEmpty() || !set1.isEmpty()) {
                player.connection.netManager.sendPacketE(new SPacketAdvancementInfo(this.isFirstPacket, set, set1, map));
                this.visibilityChanged.clear();
                this.progressChanged.clear();
            }
        }
        this.isFirstPacket = false;
    }


    public void setSelectedTab(@Nullable Advancement adv) {
        Advancement advancement = this.lastSelectedTab;

        if (adv != null && adv.getParent() == null && adv.getDisplay() != null) {
            this.lastSelectedTab = adv;
        } else {
            this.lastSelectedTab = null;
        }

        if (advancement != this.lastSelectedTab) {
            this.player.connection.netManager.sendPacketE(new SPacketSelectAdvancementsTab(this.lastSelectedTab == null ? null : this.lastSelectedTab.getId()));
        }
    }


    public AdvancementProgress getProgress(Advancement advancementIn) {
        AdvancementProgress advancementprogress = this.progress.get(advancementIn);
        if (advancementprogress == null) {
            advancementprogress = new AdvancementProgress();
            this.startProgress(advancementIn, advancementprogress);
        }
        return advancementprogress;
    }


    private void startProgress(Advancement adv1, AdvancementProgress progress) {
        progress.update(adv1.getCriteria(), adv1.getRequirements());
        this.progress.put(adv1, progress);
    }


    private void ensureVisibility(Advancement advancement1) {
        boolean flag = this.shouldBeVisible(advancement1);
        boolean flag1 = this.visible.contains(advancement1);

        if (flag && !flag1) {
            this.visible.add(advancement1);
            this.visibilityChanged.add(advancement1);

            if (this.progress.containsKey(advancement1)) {
                this.progressChanged.add(advancement1);
            }
        } else if (!flag && flag1) {
            this.visible.remove(advancement1);
            this.visibilityChanged.add(advancement1);
        }

        if (flag != flag1 && advancement1.getParent() != null) {
            this.ensureVisibility(advancement1.getParent());
        }

        for (Advancement advancement : advancement1.getChildren()) {
            this.ensureVisibility(advancement);
        }
    }


    private boolean shouldBeVisible(Advancement advancement) {
        for (int i = 0; advancement != null && i <= 2; ++i) {
            if (i == 0 && this.hasCompletedChildrenOrSelf(advancement)) {
                return true;
            }
            if (advancement.getDisplay() == null) {
                return false;
            }

            AdvancementProgress advancementprogress = this.getProgress(advancement);

            if (advancementprogress.isDone()) {
                return true;
            }

            if (advancement.getDisplay().isHidden()) {
                return false;
            }
            advancement = advancement.getParent();
        }
        return false;
    }


    private boolean hasCompletedChildrenOrSelf(Advancement other) {
        AdvancementProgress advancementprogress = this.getProgress(other);

        if (advancementprogress.isDone()) {
            return true;
        } else {
            for (Advancement advancement : other.getChildren()) {
                if (this.hasCompletedChildrenOrSelf(advancement)) {
                    return true;
                }
            }
            return false;
        }
    }
}