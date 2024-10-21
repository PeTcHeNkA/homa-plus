package net.minecraft.server.management;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.IOUtils;


public class PlayerProfileCache {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static boolean onlineMode;
    private final Map<String, ProfileEntry> usernameToProfileEntryMap = Maps.newHashMap();
    private final Map<UUID, ProfileEntry> uuidToProfileEntryMap = Maps.newHashMap();
    private final Deque<GameProfile> gameProfiles = Lists.newLinkedList();
    private final GameProfileRepository profileRepo;
    protected final Gson gson;
    private final File usercacheFile;

    private static final ParameterizedType TYPE = new ParameterizedType() {
        private final Type[] actualArgs = new Type[]{ProfileEntry.class};
        @Override
        public Type[] getActualTypeArguments() {
            return actualArgs;
        }
        @Override
        public Type getRawType() {
            return List.class;
        }
        @Override
        public Type getOwnerType() {
            return null;
        }
    };


    public PlayerProfileCache(GameProfileRepository repo, File file) {
        this.profileRepo = repo;
        this.usercacheFile = file;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(ProfileEntry.class, new Serializer());
        this.gson = builder.create();
        this.load();
    }


    private static GameProfile lookupProfile(GameProfileRepository p_187319_0_, String p_187319_1_) {
        final GameProfile[] profile = new GameProfile[1];
        ProfileLookupCallback callback = new ProfileLookupCallback() {
            public void onProfileLookupSucceeded(GameProfile p_onProfileLookupSucceeded_1_) {
                profile[0] = p_onProfileLookupSucceeded_1_;
            }

            public void onProfileLookupFailed(GameProfile p_onProfileLookupFailed_1_, Exception p_onProfileLookupFailed_2_) {
                profile[0] = null;
            }
        };
        p_187319_0_.findProfilesByNames(new String[]{p_187319_1_}, Agent.MINECRAFT, callback);
        if (!isOnlineMode() && profile[0] == null) {
            UUID lvt_4_1_ = EntityPlayer.getUUID(new GameProfile((UUID)null, p_187319_1_));
            GameProfile lvt_5_1_ = new GameProfile(lvt_4_1_, p_187319_1_);
            callback.onProfileLookupSucceeded(lvt_5_1_);
        }
        return profile[0];
    }


    public static void setOnlineMode(boolean online) {
        onlineMode = online;
    }


    private static boolean isOnlineMode() {
        return onlineMode;
    }


    public void addEntry(GameProfile profile) {
        this.addEntry(profile, null, false);
    }


    private void addEntry(GameProfile profile, @Nullable Date date, boolean onLoad) {
        UUID uuid = profile.getId();
        if (date == null) {
            Calendar lvt_4_1_ = Calendar.getInstance();
            lvt_4_1_.setTime(new Date());
            lvt_4_1_.add(2, 1);
            date = lvt_4_1_.getTime();
        }

        //String name = profile.getName().toLowerCase(Locale.ROOT);
        ProfileEntry lvt_5_1_ = new ProfileEntry(profile, date);
        if (this.uuidToProfileEntryMap.containsKey(uuid)) {
            ProfileEntry lvt_6_1_ = this.uuidToProfileEntryMap.get(uuid);
            this.usernameToProfileEntryMap.remove(lvt_6_1_.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(profile);
        }

        this.usernameToProfileEntryMap.put(profile.getName().toLowerCase(Locale.ROOT), lvt_5_1_);
        this.uuidToProfileEntryMap.put(uuid, lvt_5_1_);
        this.gameProfiles.addFirst(profile);

        if(!onLoad)this.save();
    }


    @Nullable
    public GameProfile getGameProfileForUsername(String p_152655_1_) {
        String lvt_2_1_ = p_152655_1_.toLowerCase(Locale.ROOT);
        ProfileEntry lvt_3_1_ = this.usernameToProfileEntryMap.get(lvt_2_1_);
        if (lvt_3_1_ != null && (new Date()).getTime() >= lvt_3_1_.expirationDate.getTime()) {
            this.uuidToProfileEntryMap.remove(lvt_3_1_.getGameProfile().getId());
            this.usernameToProfileEntryMap.remove(lvt_3_1_.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(lvt_3_1_.getGameProfile());
            lvt_3_1_ = null;
        }

        GameProfile lvt_4_2_;
        if (lvt_3_1_ != null) {
            lvt_4_2_ = lvt_3_1_.getGameProfile();
            this.gameProfiles.remove(lvt_4_2_);
            this.gameProfiles.addFirst(lvt_4_2_);
        } else {
            lvt_4_2_ = lookupProfile(this.profileRepo, lvt_2_1_);
            if (lvt_4_2_ != null) {
                this.addEntry(lvt_4_2_);
                lvt_3_1_ = this.usernameToProfileEntryMap.get(lvt_2_1_);
            }
        }

        this.save();
        return lvt_3_1_ == null ? null : lvt_3_1_.getGameProfile();
    }


    public String[] getUsernames() {
        return this.usernameToProfileEntryMap.keySet().toArray(new String[0]);
    }


    @Nullable
    public GameProfile getProfileByUUID(UUID p_152652_1_) {
        ProfileEntry entry = this.uuidToProfileEntryMap.get(p_152652_1_);
        return entry == null ? null : entry.getGameProfile();
    }


    @Nullable
    private ProfileEntry getByUUID(UUID uuid) {
        ProfileEntry entry = this.uuidToProfileEntryMap.get(uuid);
        if (entry != null) {
            GameProfile lvt_3_1_ = entry.getGameProfile();
            this.gameProfiles.remove(lvt_3_1_);
            this.gameProfiles.addFirst(lvt_3_1_);
        }
        return entry;
    }


    public void load() {
        BufferedReader reader = null;

        try {
            reader = Files.newReader(this.usercacheFile, StandardCharsets.UTF_8);
            List lvt_2_1_ = JsonUtils.fromJson(this.gson, reader, TYPE);
            this.usernameToProfileEntryMap.clear();
            this.uuidToProfileEntryMap.clear();
            this.gameProfiles.clear();
            if (lvt_2_1_ != null) {
                Iterator var3 = Lists.reverse(lvt_2_1_).iterator();

                while(var3.hasNext()) {
                    ProfileEntry lvt_4_1_ = (ProfileEntry)var3.next();
                    if (lvt_4_1_ != null) {
                        this.addEntry(lvt_4_1_.getGameProfile(), lvt_4_1_.getExpirationDate(), true);
                    }
                }
            }
        } catch (FileNotFoundException var9) {
        } catch (JsonParseException var10) {
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }


    public void save() {
        String str = this.gson.toJson(this.getEntriesWithLimit(1000));
        BufferedWriter lvt_2_1_ = null;

        try {
            lvt_2_1_ = Files.newWriter(this.usercacheFile, StandardCharsets.UTF_8);
            lvt_2_1_.write(str);
            return;
        } catch (FileNotFoundException ignored) {
        } catch (IOException var9) {
            return;
        } finally {
            IOUtils.closeQuietly(lvt_2_1_);
        }
    }


    private List<ProfileEntry> getEntriesWithLimit(int lim) {
        List<ProfileEntry> list = Lists.newArrayList();
        List<GameProfile> lvt_3_1_ = Lists.newArrayList(Iterators.limit(this.gameProfiles.iterator(), lim));

        for (GameProfile profile : lvt_3_1_) {
            ProfileEntry entry = this.getByUUID(profile.getId());
            if (entry != null) {
                list.add(entry);
            }
        }
        return list;
    }


    static class ProfileEntry {
        private final GameProfile gameProfile;
        private final Date expirationDate;

        private ProfileEntry(GameProfile p_i46333_2_, Date p_i46333_3_) {
            this.gameProfile = p_i46333_2_;
            this.expirationDate = p_i46333_3_;
        }

        public GameProfile getGameProfile() {
            return this.gameProfile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }
    }



    class Serializer implements JsonDeserializer<ProfileEntry>, JsonSerializer<ProfileEntry> {
        private Serializer() {}

        @Override
        public JsonElement serialize(ProfileEntry p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
            JsonObject lvt_4_1_ = new JsonObject();
            lvt_4_1_.addProperty("name", p_serialize_1_.getGameProfile().getName());
            UUID lvt_5_1_ = p_serialize_1_.getGameProfile().getId();
            lvt_4_1_.addProperty("uuid", lvt_5_1_ == null ? "" : lvt_5_1_.toString());
            lvt_4_1_.addProperty("expiresOn", PlayerProfileCache.DATE_FORMAT.format(p_serialize_1_.getExpirationDate()));
            return lvt_4_1_;
        }


        @Override
        public ProfileEntry deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            if (p_deserialize_1_.isJsonObject()) {
                JsonObject lvt_4_1_ = p_deserialize_1_.getAsJsonObject();
                JsonElement lvt_5_1_ = lvt_4_1_.get("name");
                JsonElement lvt_6_1_ = lvt_4_1_.get("uuid");
                JsonElement lvt_7_1_ = lvt_4_1_.get("expiresOn");
                if (lvt_5_1_ != null && lvt_6_1_ != null) {
                    String lvt_8_1_ = lvt_6_1_.getAsString();
                    String lvt_9_1_ = lvt_5_1_.getAsString();
                    Date lvt_10_1_ = null;
                    if (lvt_7_1_ != null) {
                        try {
                            lvt_10_1_ = PlayerProfileCache.DATE_FORMAT.parse(lvt_7_1_.getAsString());
                        } catch (ParseException var14) {
                            lvt_10_1_ = null;
                        }
                    }

                    if (lvt_9_1_ != null && lvt_8_1_ != null) {
                        UUID lvt_11_3_;
                        try {
                            lvt_11_3_ = UUID.fromString(lvt_8_1_);
                        } catch (Throwable var13) {
                            return null;
                        }

                        return new ProfileEntry(new GameProfile(lvt_11_3_, lvt_9_1_), lvt_10_1_);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}