package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.event.ElcaConnectEvent;
import plus.fserver.server.chunk.io.TChunkExecutor;
import plus.tson.utl.Te4HashSet;


@Keep
public abstract class PlayerList {
    @Keep
    public static final File FILE_PLAYERBANS = new File("banned-players.json");
    @Keep
    public static final File FILE_IPBANS = new File("banned-ips.json");
    @Keep
    public static final File FILE_OPS = new File("ops.json");
    @Keep
    public static final File FILE_WHITELIST = new File("whitelist.json");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<EntityPlayerMP> playerEntityList = new CopyOnWriteArrayList<>();
    private final Map<UUID, EntityPlayerMP> uuidToPlayerMap = Maps.newHashMap();
    private final UserListBans bannedPlayers;
    private final UserListIPBans bannedIPs;
    private final UserListOps ops;
    private final UserListWhitelist whiteListedPlayers;
    private final Map<UUID, StatisticsManagerServer> playerStatFiles;
    private final Map<UUID, PlayerAdvancements> advancements;
    private IPlayerFileData playerDataManager;
    private boolean whiteListEnforced;
    @Keep
    protected int maxPlayers;
    private int viewDistance;
    private GameType gameType;
    private boolean commandsAllowedForAll;
    private int playerPingIndex;

    public PlayerList(MinecraftServer server) {
        this.bannedPlayers = new UserListBans(FILE_PLAYERBANS);
        this.bannedIPs = new UserListIPBans(FILE_IPBANS);
        this.ops = new UserListOps(FILE_OPS);
        this.whiteListedPlayers = new UserListWhitelist(FILE_WHITELIST);
        this.playerStatFiles = new ConcurrentHashMap<>();
        this.advancements = new ConcurrentHashMap<>();
        this.server = server;
        this.bannedPlayers.setLanServer(false);
        this.bannedIPs.setLanServer(false);
        this.maxPlayers = 8;
    }


    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, NetHandlerPlayServer connection) {
        GameProfile gameprofile = playerIn.getGameProfile();
        PlayerProfileCache playerprofilecache = this.server.getPlayerProfileCache();
        GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.addEntry(gameprofile);
        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
        playerIn.setWorld(this.server.getWorld(playerIn.dimension));

        World playerWorld = this.server.getWorld(playerIn.dimension);
        if (playerWorld == null)
        {
            playerIn.dimension = 0;
            playerWorld = this.server.getWorld(0);
            BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            playerIn.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }

        playerIn.setWorld(playerWorld);
        //playerIn.interactionManager.setWorld((WorldServer)playerIn.world);
        String s1 = "local";

        if (netManager.getRemoteAddress() != null)
        {
            s1 = netManager.getRemoteAddress().toString();
        }

        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", playerIn.getName(), s1, Integer.valueOf(playerIn.getEntityId()), Double.valueOf(playerIn.posX), Double.valueOf(playerIn.posY), Double.valueOf(playerIn.posZ));
        WorldServer worldserver = this.server.getWorld(playerIn.dimension);
        WorldInfo worldinfo = worldserver.getWorldInfo();
        this.setPlayerGameTypeBasedOnOther(playerIn, (EntityPlayerMP)null, worldserver);
        playerIn.connection = connection;
        net.minecraftforge.fml.common.FMLCommonHandler.instance().fireServerConnectionEvent(netManager);
        connection.sendPacket(new SPacketJoinGame(playerIn.getEntityId(), playerIn.interactionManager.getGameType(), worldinfo.isHardcoreModeEnabled(), worldserver.provider.getDimension(), worldserver.getDifficulty(), this.getMaxPlayers(), worldinfo.getTerrainType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
        connection.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServerInstance().getServerModName())));
        connection.sendPacket(new SPacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        connection.sendPacket(new SPacketPlayerAbilities(playerIn.capabilities));
        connection.sendPacket(new SPacketHeldItemChange(playerIn.inventory.currentItem));
        this.updatePermissionLevel(playerIn);
        playerIn.getStatFile().markAllDirty();
        playerIn.getRecipeBook().init(playerIn);
        this.sendScoreboard((ServerScoreboard)worldserver.getScoreboard(), playerIn);
        this.server.refreshStatusNextTick();
        TextComponentTranslation textcomponenttranslation;

        if (playerIn.getName().equalsIgnoreCase(s)) {
            textcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined", new Object[] {playerIn.getDisplayName()});
        } else {
            textcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined.renamed", new Object[] {playerIn.getDisplayName(), s});
        }

        textcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);
        this.sendMessage(textcomponenttranslation);
        this.playerLoggedIn(playerIn);
        connection.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        this.updateTimeAndWeatherForPlayer(playerIn, worldserver);

        if (!this.server.getResourcePackUrl().isEmpty()) {
            playerIn.loadResourcePack(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
        }

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            connection.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        if (nbttagcompound != null && nbttagcompound.hasKey("RootVehicle", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("RootVehicle");
            Entity entity1 = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompoundTag("Entity"), worldserver, true);

            if (entity1 != null) {
                UUID uuid = nbttagcompound1.getUniqueId("Attach");

                if (entity1.getUniqueID().equals(uuid)) {
                    playerIn.startRiding(entity1, true);
                } else {
                    for (Entity entity : entity1.getRecursivePassengers()) {
                        if (entity.getUniqueID().equals(uuid)) {
                            playerIn.startRiding(entity, true);
                            break;
                        }
                    }
                }

                if (!playerIn.isRiding()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    worldserver.removeEntityDangerously(entity1);

                    for (Entity entity2 : entity1.getRecursivePassengers()) {
                        worldserver.removeEntityDangerously(entity2);
                    }
                }
            }
        }
        playerIn.addSelfToInternalCraftingInventory();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedIn(playerIn);
    }


    protected void sendScoreboard(ServerScoreboard scoreboardIn, EntityPlayerMP playerIn) {
        NetworkManager net = playerIn.connection.netManager;
        Collection<ScorePlayerTeam> team = scoreboardIn.getTeams();
        net.exec(() -> {
            Set<ScoreObjective> set = new Te4HashSet<>();
            for (ScorePlayerTeam scoreplayerteam : team) {
                net.sendPacketSync(new SPacketTeams(scoreplayerteam, 0));
            }
            for (int i = 0; i < 19; ++i) {
                ScoreObjective scoreobjective = scoreboardIn.getObjectiveInDisplaySlot(i);
                if (scoreobjective != null && !set.contains(scoreobjective)) {
                    for (Packet<?> packet : scoreboardIn.getCreatePackets(scoreobjective)) {
                        net.sendPacketSync(packet);
                    }
                    set.add(scoreobjective);
                }
            }
        });
    }


    public void setPlayerManager(WorldServer[] worldServers) {
        this.playerDataManager = worldServers[0].getSaveHandler().getPlayerNBTManager();
        worldServers[0].getWorldBorder().addListener(new IBorderListener() {
            public void onSizeChanged(WorldBorder border, double newSize) {
                PlayerList.this.sendPacketToAllPlayersE(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_SIZE));
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time) {
                PlayerList.this.sendPacketToAllPlayersE(new SPacketWorldBorder(border, SPacketWorldBorder.Action.LERP_SIZE));
            }
            public void onCenterChanged(WorldBorder border, double x, double z) {
                PlayerList.this.sendPacketToAllPlayersE(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_CENTER));
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime) {
                PlayerList.this.sendPacketToAllPlayersE(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_TIME));
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance) {
                PlayerList.this.sendPacketToAllPlayersE(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_BLOCKS));
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
            }
        });
    }


    public int getEntityViewDistance() {
        return PlayerChunkMap.getFurthestViewableBlock(this.getViewDistance());
    }


    @Nullable
    public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn) {
        NBTTagCompound nbttagcompound = this.server.worlds[0].getWorldInfo().getPlayerNBTTagCompound();
        NBTTagCompound nbttagcompound1;

        if (playerIn.getName().equals(this.server.getServerOwner()) && nbttagcompound != null) {
            nbttagcompound1 = nbttagcompound;
            playerIn.readFromNBT(nbttagcompound);
            LOGGER.debug("loading single player");
            net.minecraftforge.event.ForgeEventFactory.firePlayerLoadingEvent(playerIn, this.playerDataManager, playerIn.getUniqueID().toString());
        } else {
            nbttagcompound1 = this.playerDataManager.readPlayerData(playerIn);
        }
        return nbttagcompound1;
    }


    public NBTTagCompound getPlayerNBT(EntityPlayerMP player) {
        // Hacky method to allow loading the NBT for a player prior to login
        NBTTagCompound nbttagcompound = this.server.worlds[0].getWorldInfo().getPlayerNBTTagCompound();
        if (player.getName().equals(this.server.getServerOwner()) && nbttagcompound != null) {
            return nbttagcompound;
        } else {
            return ((net.minecraft.world.storage.SaveHandler)this.playerDataManager).getPlayerNBT(player);
        }
    }


    protected void writePlayerData(EntityPlayerMP playerIn) {
        if (playerIn.connection == null) return;
        this.playerDataManager.writePlayerData(playerIn);
        StatisticsManagerServer statisticsmanagerserver = this.playerStatFiles.get(playerIn.getUniqueID());
        if (statisticsmanagerserver != null) {
            statisticsmanagerserver.saveStatFile();
        }
        PlayerAdvancements playeradvancements = this.advancements.get(playerIn.getUniqueID());

        if (playeradvancements != null) {
            playeradvancements.save();
        }
    }


    public void serverUpdateMovingPlayer(EntityPlayerMP playerIn) {
        playerIn.getServerWorld().getPlayerChunkMap().updateMovingPlayer(playerIn);
    }


    public void playerLoggedOut(EntityPlayerMP playerIn) {
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerLoggedOut(playerIn);
        WorldServer worldserver = playerIn.getServerWorld();
        playerIn.addStat(StatList.LEAVE_GAME);
        this.writePlayerData(playerIn);

        if (playerIn.isRiding()) {
            Entity entity = playerIn.getLowestRidingEntity();

            if (entity.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
                LOGGER.debug("Removing player mount");
                playerIn.dismountRidingEntity();
                worldserver.removeEntityDangerously(entity);

                for (Entity entity1 : entity.getRecursivePassengers()) {
                    worldserver.removeEntityDangerously(entity1);
                }
                worldserver.getChunk(playerIn.chunkCoordX, playerIn.chunkCoordZ).markDirty();
            }
        }
        TChunkExecutor.adjustPoolSize(this.getCurrentPlayerCount());

        worldserver.removeEntity(playerIn);
        worldserver.getPlayerChunkMap().removePlayer(playerIn);
        playerIn.getAdvancements().dispose();
        this.playerEntityList.remove(playerIn);
        UUID uuid = playerIn.getUniqueID();
        EntityPlayerMP entityplayermp = this.uuidToPlayerMap.get(uuid);

        if (entityplayermp == playerIn) {
            this.uuidToPlayerMap.remove(uuid);
            this.playerStatFiles.remove(uuid);
            this.advancements.remove(uuid);
        }
        this.sendPacketToAllPlayersE(new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, playerIn));
    }
//
//
//    @Deprecated
//    public String allowUserToConnect(SocketAddress address, GameProfile profile) {
//        return allowUserToConnect(new ElcaConnectEvent(this, address, profile), address, profile);
//    }


    @Nullable
    public String allowUserToConnect(ElcaConnectEvent event, SocketAddress address, GameProfile profile) {
        if (this.bannedPlayers.isBanned(profile)) {
            UserListBansEntry userlistbansentry = this.bannedPlayers.getEntry(profile);
            String s1 = "You are banned from this server!\nReason: " + userlistbansentry.getBanReason();

            if (userlistbansentry.getBanEndDate() != null) {
                s1 = s1 + "\nYour ban will be removed on " + DATE_FORMAT.format(userlistbansentry.getBanEndDate());
            }
            event.setStatus(ElcaConnectEvent.Status.REJECT_BANNED);
            event.reject(s1);
        } else if (!this.canJoin(profile)) {
            event.setStatus(ElcaConnectEvent.Status.REJECT_NON_WHITELIST);
            event.reject(ElcaConfig.REJECT_NON_WHITELIST);
        } else if (this.bannedIPs.isBanned(address)) {
            UserListIPBansEntry userlistipbansentry = this.bannedIPs.getBanEntry(address);
            String s = "Your IP address is banned from this server!\nReason: " + userlistipbansentry.getBanReason();

            if (userlistipbansentry.getBanEndDate() != null) {
                s = s + "\nYour ban will be removed on " + DATE_FORMAT.format(userlistipbansentry.getBanEndDate());
            }
            event.setStatus(ElcaConnectEvent.Status.REJECT_BANNED_IP);
            event.reject(s);
        } else {
            String msg = this.playerEntityList.size() >= this.maxPlayers && !this.bypassesPlayerLimit(profile) ? ElcaConfig.REJECT_SERVER_FULL : null;
            if(msg != null){
                event.setStatus(ElcaConnectEvent.Status.REJECT_SERVER_FULL);
                event.reject(msg);
            }
        }
        MinecraftForge.EVENT_BUS.postF(event);
        return event.getMessage();
    }


    public EntityPlayerMP createPlayerForUser(GameProfile profile) {
        UUID uuid = EntityPlayer.getUUID(profile);
        List<EntityPlayerMP> list = Lists.newArrayList();

        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = this.playerEntityList.get(i);

            if (entityplayermp.getUniqueID().equals(uuid)) {
                list.add(entityplayermp);
            }
        }

        EntityPlayerMP player = this.uuidToPlayerMap.get(profile.getId());

        if (player != null && !list.contains(player)) {
            list.add(player);
        }

        for (EntityPlayerMP entityplayermp1 : list) {
            entityplayermp1.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.duplicate_login"));
        }

        PlayerInteractionManager playerinteractionmanager;

        WorldServer world = this.server.getWorld(0);

        if (this.server.isDemo()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(world);
        } else {
            playerinteractionmanager = new PlayerInteractionManager(world);
        }
        return new EntityPlayerMP(this.server, world, profile, playerinteractionmanager);
    }


    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd) {
        World world = server.getWorld(dimension);
        if (world == null) {
            dimension = playerIn.getSpawnDimension();
        }
        else if (!world.provider.canRespawnHere()) {
            dimension = world.provider.getRespawnDimension(playerIn);
        }
        if (server.getWorld(dimension) == null) dimension = 0;

        playerIn.getServerWorld().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerWorld().getEntityTracker().untrack(playerIn);
        playerIn.getServerWorld().getPlayerChunkMap().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.server.getWorld(playerIn.dimension).removeEntityDangerously(playerIn);
        BlockPos blockpos = playerIn.getBedLocation(dimension);
        boolean flag = playerIn.isSpawnForced(dimension);
        playerIn.dimension = dimension;
        PlayerInteractionManager playerinteractionmanager;

        if (this.server.isDemo()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(this.server.getWorld(playerIn.dimension));
        } else {
            playerinteractionmanager = new PlayerInteractionManager(this.server.getWorld(playerIn.dimension));
        }

        EntityPlayerMP entityplayermp = new EntityPlayerMP(this.server, this.server.getWorld(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
        entityplayermp.connection = playerIn.connection;
        entityplayermp.copyFrom(playerIn, conqueredEnd);
        entityplayermp.dimension = dimension;
        entityplayermp.setEntityId(playerIn.getEntityId());
        entityplayermp.setCommandStats(playerIn);
        entityplayermp.setPrimaryHand(playerIn.getPrimaryHand());

        for (String s : playerIn.getTags()) {
            entityplayermp.addTag(s);
        }

        WorldServer worldserver = this.server.getWorld(playerIn.dimension);
        this.setPlayerGameTypeBasedOnOther(entityplayermp, playerIn, worldserver);

        if (blockpos != null) {
            BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(this.server.getWorld(playerIn.dimension), blockpos, flag);

            if (blockpos1 != null) {
                entityplayermp.setLocationAndAngles((float)blockpos1.getX() + 0.5F, (float)blockpos1.getY() + 0.1F, (float)blockpos1.getZ() + 0.5F, 0.0F, 0.0F);
                entityplayermp.setSpawnPoint(blockpos, flag);
            } else {
                entityplayermp.connection.netManager.sendPacketE(new SPacketChangeGameState(0, 0.0F));
            }
        }

        worldserver.getChunkProvider().provideChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);

        while (!worldserver.getCollisionBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D) {
            entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
        }

        entityplayermp.connection.netManager.sendPacketE(new SPacketRespawn(entityplayermp.dimension, entityplayermp.world.getDifficulty(), entityplayermp.world.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
        BlockPos blockpos2 = worldserver.getSpawnPoint();
        entityplayermp.connection.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
        entityplayermp.connection.netManager.sendPacketE(new SPacketSpawnPosition(blockpos2));
        entityplayermp.connection.netManager.sendPacketE(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
        this.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
        this.updatePermissionLevel(entityplayermp);
        worldserver.getPlayerChunkMap().addPlayer(entityplayermp);
        worldserver.spawnEntity(entityplayermp);
        this.playerEntityList.add(entityplayermp);
        this.uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
        entityplayermp.addSelfToInternalCraftingInventory();
        entityplayermp.setHealth(entityplayermp.getHealth());
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp, conqueredEnd);
        return entityplayermp;
    }


    public void updatePermissionLevel(EntityPlayerMP player) {
        GameProfile gameprofile = player.getGameProfile();
        int i = this.canSendCommands(gameprofile) ? this.ops.getPermissionLevel(gameprofile) : 0;
        i = this.server.isSinglePlayer() && this.server.worlds[0].getWorldInfo().areCommandsAllowed() ? 4 : i;
        i = this.commandsAllowedForAll ? 4 : i;
        this.sendPlayerPermissionLevel(player, i);
    }


    public void changePlayerDimension(EntityPlayerMP player, int dimensionIn) {
        transferPlayerToDimension(player, dimensionIn, server.getWorld(dimensionIn).getDefaultTeleporterRaw());
    }

    // TODO: Remove (1.13)
    public void transferPlayerToDimension(EntityPlayerMP player, int dimensionIn, net.minecraft.world.Teleporter teleporter) {
        transferPlayerToDimension(player, dimensionIn, (net.minecraftforge.common.util.ITeleporter) teleporter);
    }


    public void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn) {
        transferEntityToWorld(entityIn, lastDimension, oldWorldIn, toWorldIn, toWorldIn.getDefaultTeleporterRaw());
    }

    // TODO: Remove (1.13)
    public void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, net.minecraft.world.Teleporter teleporter) {
        transferEntityToWorld(entityIn, lastDimension, oldWorldIn, toWorldIn, (net.minecraftforge.common.util.ITeleporter) teleporter);
    }


    public void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, net.minecraftforge.common.util.ITeleporter teleporter) {
        double moveFactor = oldWorldIn.provider.getMovementFactor() / toWorldIn.provider.getMovementFactor();
        double d0 = MathHelper.clamp(entityIn.posX * moveFactor, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
        double d1 = MathHelper.clamp(entityIn.posZ * moveFactor, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
        float f = entityIn.rotationYaw;

        if (entityIn.dimension == 1 && teleporter.isVanilla()) {
            BlockPos blockpos;

            if (lastDimension == 1) {
                blockpos = toWorldIn.getSpawnPoint();
            } else {
                blockpos = toWorldIn.getSpawnCoordinate();
            }

            d0 = blockpos.getX();
            entityIn.posY = blockpos.getY();
            d1 = blockpos.getZ();
            entityIn.setLocationAndAngles(d0, entityIn.posY, d1, 90.0F, 0.0F);
            if (entityIn.isEntityAlive()) {
                oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }

        if (lastDimension != 1 || !teleporter.isVanilla()) {
            d0 = MathHelper.clamp((int)d0, -29999872, 29999872);
            d1 = MathHelper.clamp((int)d1, -29999872, 29999872);

            if (entityIn.isEntityAlive()) {
                entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
                oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
                teleporter.placeEntity(toWorldIn, entityIn, f);
                toWorldIn.spawnEntity(entityIn);
                toWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }

        entityIn.setWorld(toWorldIn);
    }


    public void onTick() {
        if (++this.playerPingIndex > 600) {
            this.sendPacketToAllPlayersE(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_LATENCY, this.playerEntityList));
            this.playerPingIndex = 0;
        }
    }


    public void sendPacketToAllPlayers(Packet<?> packetIn) {
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            (this.playerEntityList.get(i)).connection.sendPacket(packetIn);
        }
    }


    public void sendPacketToAllPlayersE(Packet<?> packetIn) {
        if(this.playerEntityList.isEmpty())return;
        //todo use concurrent playerEntityList
        NetworkManager first = (this.playerEntityList.get(0)).connection.netManager;
        first.exec(() -> {
            first.sendPacketSync(packetIn);
            for (int i = 1; i < PlayerList.this.playerEntityList.size(); ++i) {
                (playerEntityList.get(i)).connection.netManager.sendPacketSync(packetIn);
            }
        });
    }


    public void sendPacketToAllPlayersInDimensionE(Packet<?> packetIn, int dimension) {
        if(this.playerEntityList.isEmpty())return;
        //todo use concurrent playerEntityList
        EntityPlayerMP first = this.playerEntityList.get(0);
        first.connection.netManager.exec(() -> {
            if(first.dimension == dimension) first.connection.netManager.sendPacketSync(packetIn);
            for (int i = 1; i < PlayerList.this.playerEntityList.size(); ++i) {
                EntityPlayerMP playerMP = this.playerEntityList.get(i);
                if(playerMP.dimension == dimension)
                    playerMP.connection.netManager.sendPacketSync(packetIn);
            }
        });
    }


    public void sendPacketToAllPlayersInDimension(Packet<?> packetIn, int dimension) {
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = this.playerEntityList.get(i);
            if (entityplayermp.dimension == dimension) {
                entityplayermp.connection.sendPacket(packetIn);
            }
        }
    }


    public void sendMessageToAllTeamMembers(EntityPlayer player, ITextComponent message) {
        Team team = player.getTeam();
        if (team != null) {
            for (String s : team.getMembershipCollection()) {
                EntityPlayerMP entityplayermp = this.getPlayerByUsername(s);
                if (entityplayermp != null && entityplayermp != player) {
                    entityplayermp.sendMessage(message);
                }
            }
        }
    }


    public void sendMessageToTeamOrAllPlayers(EntityPlayer player, ITextComponent message) {
        Team team = player.getTeam();
        if (team == null) {
            this.sendMessage(message);
        } else {
            for (int i = 0; i < this.playerEntityList.size(); ++i) {
                EntityPlayerMP entityplayermp = this.playerEntityList.get(i);
                if (entityplayermp.getTeam() != team) {
                    entityplayermp.sendMessage(message);
                }
            }
        }
    }


    public String getFormattedListOfPlayers(boolean includeUUIDs) {
        StringBuilder s = new StringBuilder();
        List<EntityPlayerMP> list = Lists.newArrayList(this.playerEntityList);
        for (int i = 0; i < list.size(); ++i) {
            if (i > 0) {
                s.append(", ");
            }
            s.append(list.get(i).getName());

            if (includeUUIDs) {
                s.append(" (").append(list.get(i).getCachedUniqueIdString()).append(")");
            }
        }
        return s.toString();
    }


    public String[] getOnlinePlayerNames() {
        String[] astring = new String[this.playerEntityList.size()];
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            astring[i] = this.playerEntityList.get(i).getName();
        }
        return astring;
    }


    public GameProfile[] getOnlinePlayerProfiles() {
        GameProfile[] agameprofile = new GameProfile[this.playerEntityList.size()];
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            agameprofile[i] = this.playerEntityList.get(i).getGameProfile();
        }
        return agameprofile;
    }


    public UserListBans getBannedPlayers() {
        return this.bannedPlayers;
    }


    public UserListIPBans getBannedIPs() {
        return this.bannedIPs;
    }


    public void addOp(GameProfile profile) {
        int i = this.server.getOpPermissionLevel();
        this.ops.addEntry(new UserListOpsEntry(profile, this.server.getOpPermissionLevel(), this.ops.bypassesPlayerLimit(profile)));
        this.sendPlayerPermissionLevel(this.getPlayerByUUID(profile.getId()), i);
    }


    public void removeOp(GameProfile profile) {
        this.ops.removeEntry(profile);
        this.sendPlayerPermissionLevel(this.getPlayerByUUID(profile.getId()), 0);
    }


    private void sendPlayerPermissionLevel(EntityPlayerMP player, int permLevel) {
        if (player != null && player.connection != null) {
            byte b0;
            if (permLevel <= 0) {
                b0 = 24;
            }
            else if (permLevel >= 4) {
                b0 = 28;
            } else {
                b0 = (byte)(24 + permLevel);
            }
            player.connection.netManager.sendPacketE(new SPacketEntityStatus(player, b0));
        }
    }


    public boolean canJoin(GameProfile profile) {
        return !this.whiteListEnforced || this.ops.hasEntry(profile) || this.whiteListedPlayers.hasEntry(profile);
    }


    public boolean canSendCommands(GameProfile profile) {
        return this.ops.hasEntry(profile) || this.server.isSinglePlayer() && this.server.worlds[0].getWorldInfo().areCommandsAllowed() && this.server.getServerOwner().equalsIgnoreCase(profile.getName()) || this.commandsAllowedForAll;
    }


    @Nullable
    public EntityPlayerMP getPlayerByUsername(String username) {
        for (EntityPlayerMP entityplayermp : this.playerEntityList) {
            if (entityplayermp.getName().equalsIgnoreCase(username)) {
                return entityplayermp;
            }
        }
        return null;
    }


    public void saveAllPlayerData() {
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            this.writePlayerData(this.playerEntityList.get(i));
        }
    }


    public void addWhitelistedPlayer(GameProfile profile) {
        this.whiteListedPlayers.addEntry(new UserListWhitelistEntry(profile));
    }


    public void removePlayerFromWhitelist(GameProfile profile) {
        this.whiteListedPlayers.removeEntry(profile);
    }


    public UserListWhitelist getWhitelistedPlayers() {
        return this.whiteListedPlayers;
    }


    public String[] getWhitelistedPlayerNames() {
        return this.whiteListedPlayers.getKeys();
    }


    public UserListOps getOppedPlayers() {
        return this.ops;
    }


    public String[] getOppedPlayerNames() {
        return this.ops.getKeys();
    }


    public void reloadWhitelist() {}


    public void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn) {
        WorldBorder worldborder = this.server.worlds[0].getWorldBorder();
        playerIn.connection.netManager.exec(() -> {
            NetworkManager net = playerIn.connection.netManager;
            net.sendPacketSync(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.INITIALIZE));
            net.sendPacketSync(new SPacketTimeUpdate(worldIn.getTotalWorldTime(), worldIn.getWorldTime(), worldIn.getGameRules().doDaylightCycle.getBoolean()));
            BlockPos blockpos = worldIn.getSpawnPoint();
            net.sendPacketSync(new SPacketSpawnPosition(blockpos));

            if (worldIn.isRaining()) {
                net.sendPacketSync(new SPacketChangeGameState(1, 0.0F));
                net.sendPacketSync(new SPacketChangeGameState(7, worldIn.getRainStrength(1.0F)));
                net.sendPacketSync(new SPacketChangeGameState(8, worldIn.getThunderStrength(1.0F)));
            }
        });
    }


    public void syncPlayerInventory(EntityPlayerMP playerIn) {
        playerIn.sendContainerToPlayer(playerIn.inventoryContainer);
        playerIn.setPlayerHealthUpdated();
        playerIn.connection.netManager.sendPacketE(new SPacketHeldItemChange(playerIn.inventory.currentItem));
    }


    public int getCurrentPlayerCount() {
        return this.playerEntityList.size();
    }


    public int getMaxPlayers() {
        return this.maxPlayers;
    }


    public String[] getAvailablePlayerDat() {
        return this.server.worlds[0].getSaveHandler().getPlayerNBTManager().getAvailablePlayerDat();
    }


    public void setWhiteListEnabled(boolean whitelistEnabled) {
        this.whiteListEnforced = whitelistEnabled;
    }


    public List<EntityPlayerMP> getPlayersMatchingAddress(String address) {
        List<EntityPlayerMP> list = Lists.newArrayList();

        for (EntityPlayerMP entityplayermp : this.playerEntityList) {
            if (entityplayermp.getPlayerIP().equals(address)) {
                list.add(entityplayermp);
            }
        }
        return list;
    }


    public int getViewDistance() {
        return this.viewDistance;
    }


    public MinecraftServer getServerInstance() {
        return this.server;
    }


    public NBTTagCompound getHostPlayerData() {
        return null;
    }


    @SideOnly(Side.CLIENT)
    public void setGameType(GameType gameModeIn) {
        this.gameType = gameModeIn;
    }


    private void setPlayerGameTypeBasedOnOther(EntityPlayerMP target, EntityPlayerMP source, World worldIn) {
        if (source != null) {
            target.interactionManager.setGameType(source.interactionManager.getGameType());
        } else if (this.gameType != null) {
            target.interactionManager.setGameType(this.gameType);
        }
        target.interactionManager.initializeGameType(worldIn.getWorldInfo().getGameType());
    }


    @SideOnly(Side.CLIENT)
    public void setCommandsAllowedForAll(boolean p_72387_1_) {
        this.commandsAllowedForAll = p_72387_1_;
    }


    public void removeAllPlayers() {
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            (this.playerEntityList.get(i)).connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.server_shutdown", new Object[0]));
        }
    }


    public void sendMessage(ITextComponent component, boolean isSystem) {
        this.server.sendMessage(component);
        ChatType chattype = isSystem ? ChatType.SYSTEM : ChatType.CHAT;
        this.sendPacketToAllPlayers(new SPacketChat(component, chattype));
    }


    public void sendMessage(ITextComponent component) {
        this.sendMessage(component, true);
    }


    public StatisticsManagerServer getPlayerStatsFile(EntityPlayer playerIn) {
        UUID uuid = playerIn.getUniqueID();
        StatisticsManagerServer statisticsmanagerserver = uuid == null ? null : this.playerStatFiles.get(uuid);

        if (statisticsmanagerserver == null) {
            File file1 = new File(this.server.getWorld(0).getSaveHandler().getWorldDirectory(), "stats");
            File file2 = new File(file1, uuid + ".json");

            if (!file2.exists()) {
                File file3 = new File(file1, playerIn.getName() + ".json");

                if (file3.exists() && file3.isFile()) {
                    file3.renameTo(file2);
                }
            }
            statisticsmanagerserver = new StatisticsManagerServer(this.server, file2);
            statisticsmanagerserver.readStatFile();
            this.playerStatFiles.put(uuid, statisticsmanagerserver);
        }
        return statisticsmanagerserver;
    }


    public PlayerAdvancements getPlayerAdvancements(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        PlayerAdvancements cur = this.advancements.get(uuid);
        if (cur == null) {
            File file1 = new File(this.server.getWorld(0).getSaveHandler().getWorldDirectory(), "advancements");
            File file2 = new File(file1, uuid + ".json");
            cur = new PlayerAdvancements(this.server, file2, player);
            this.advancements.put(uuid, cur);
        }
        cur.setPlayer(player);
        return cur;
    }


    public void setViewDistance(int distance) {
        this.viewDistance = distance;
        if (this.server.worlds != null) {
            for (WorldServer worldserver : this.server.worlds) {
                if (worldserver != null) {
                    worldserver.getPlayerChunkMap().setPlayerViewRadius(distance);
                    worldserver.getEntityTracker().setViewDistance(distance);
                }
            }
        }
    }


    public List<EntityPlayerMP> getPlayers() {
        return this.playerEntityList;
    }


    @Nullable
    public EntityPlayerMP getPlayerByUUID(UUID playerUUID) {
        return this.uuidToPlayerMap.get(playerUUID);
    }


    public boolean bypassesPlayerLimit(GameProfile profile) {
        return false;
    }


    public void reloadResources() {
        for (PlayerAdvancements playeradvancements : this.advancements.values()) {
            playeradvancements.reload();
        }
    }


    @SideOnly(Side.SERVER)
    public boolean isWhiteListEnabled() {
        return this.whiteListEnforced;
    }


    public void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn) {
        WorldServer worldserver = playerIn.getServerWorld();
        if (worldIn != null) {
            playerIn.connection.setLock(true);
            worldserver.addSyncTask(() -> {
                worldIn.getPlayerChunkMap().removePlayer(playerIn);
                preparePlayer0(worldserver, playerIn, worldIn);
            });
        } else {
            preparePlayer0(worldserver, playerIn, null);
        }
    }


    private void preparePlayer0(WorldServer newWorld, EntityPlayerMP playerIn, @Nullable WorldServer worldIn){
        newWorld.addSyncTask(() -> preparePlayer1(newWorld, playerIn, worldIn));
    }


    private void preparePlayer1(WorldServer newWorld, EntityPlayerMP playerIn, @Nullable WorldServer worldIn){
        playerIn.connection.setLock(false);
        newWorld.getPlayerChunkMap().addPlayer(playerIn);
        newWorld.getChunkProvider().provideChunk((int)playerIn.posX >> 4, (int)playerIn.posZ >> 4);

        if(worldIn != null){
            CriteriaTriggers.CHANGED_DIMENSION.trigger(playerIn, worldIn.provider.getDimensionType(), newWorld.provider.getDimensionType());
            if (worldIn.provider.getDimensionType() == DimensionType.NETHER && playerIn.world.provider.getDimensionType() == DimensionType.OVERWORLD && playerIn.getEnteredNetherPosition() != null) {
                CriteriaTriggers.NETHER_TRAVEL.trigger(playerIn, playerIn.getEnteredNetherPosition());
            }
        }
    }


    /**
     * @author Te4hno
     */
    public void playerLoggedIn(final EntityPlayerMP playerIn) {
        playerIn.connection.setLock(true);
        this.playerEntityList.add(playerIn);
        this.uuidToPlayerMap.put(playerIn.getUniqueID(), playerIn);

        playerIn.connection.netManager.exec(() -> {
            SPacketPlayerListItem packet = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, playerIn);

            for (int i = 0; i < playerEntityList.size(); ++i) {
                EntityPlayerMP cur = playerEntityList.get(i);
                cur.connection.netManager.sendPacketSync(packet);
                playerIn.connection.netManager.sendPacketSync(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, cur));
            }
            TChunkExecutor.adjustPoolSize(this.getCurrentPlayerCount());
        });

        WorldServer worldserver = this.server.getWorld(playerIn.dimension);
        worldserver.getChunkProvider().loadChunk(
                MathHelper.floor(playerIn.posX) >> 4, MathHelper.floor(playerIn.posZ) >> 4,
                () -> {
                    worldserver.spawnEntity(playerIn);
                    PlayerList.this.preparePlayer(playerIn, null);
                    playerIn.connection.setLock(false);
                }
        );
    }


    /**
     * @author Te4hno
     */
    public void transferPlayerToDimension(EntityPlayerMP player, int dimensionIn, ITeleporter teleporter) {
        player.connection.setLock(true);
        int oldId = player.dimension;
        WorldServer worldserver = this.server.getWorld(oldId);
        player.dimension = dimensionIn;
        WorldServer newWorld = this.server.getWorld(player.dimension);
        player.connection.netManager.sendPacketE(
                new SPacketRespawn(
                        player.dimension,
                        newWorld.getDifficulty(),
                        newWorld.getWorldInfo().getTerrainType(),
                        player.interactionManager.getGameType()
                )
        );
        this.updatePermissionLevel(player);
        worldserver.removeEntityDangerously(player);
        player.isDead = false;

        if (player.isEntityAlive()) {
            worldserver.updateEntityWithOptionalForce(player, false);
        }
        worldserver.getPlayerChunkMap().removePlayer(player);

        newWorld.addSyncTask(() -> {
            transferEntityToWorld0(player, oldId, worldserver, newWorld, teleporter);

            preparePlayer1(newWorld, player, worldserver);
            //player.interactionManager.setWorld(newWorld);
            player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

            player.connection.netManager.sendPacketE(new SPacketPlayerAbilities(player.capabilities));
            updateTimeAndWeatherForPlayer(player, newWorld);
            syncPlayerInventory(player);

            for (PotionEffect potioneffect : player.getActivePotionEffects()) {
                player.connection.netManager.sendPacketE(new SPacketEntityEffect(player.getEntityId(), potioneffect));
            }

            AttributeMap attributemap = (AttributeMap) player.getAttributeMap();
            Collection<IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
            if (!watchedAttribs.isEmpty())
                player.connection.netManager.sendPacketE(new SPacketEntityProperties(player.getEntityId(), watchedAttribs));
            FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldId, dimensionIn);
        });
    }


    private void transferEntityToWorld0(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn, net.minecraftforge.common.util.ITeleporter teleporter) {
        double moveFactor = oldWorldIn.provider.getMovementFactor() / toWorldIn.provider.getMovementFactor();
        double d0 = MathHelper.clamp(entityIn.posX * moveFactor, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
        double d1 = MathHelper.clamp(entityIn.posZ * moveFactor, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
        float f = entityIn.rotationYaw;

        if (entityIn.dimension == 1 && teleporter.isVanilla()) {
            BlockPos blockpos;
            if (lastDimension == 1) {
                blockpos = toWorldIn.getSpawnPoint();
            } else {
                blockpos = toWorldIn.getSpawnCoordinate();
            }
            d0 = blockpos.getX();
            entityIn.posY = blockpos.getY();
            d1 = blockpos.getZ();
            entityIn.setLocationAndAngles(d0, entityIn.posY, d1, 90.0F, 0.0F);
        }
        if (lastDimension != 1 || !teleporter.isVanilla()) {
            d0 = MathHelper.clamp((int)d0, -29999872, 29999872);
            d1 = MathHelper.clamp((int)d1, -29999872, 29999872);

            if (entityIn.isEntityAlive()) {
                entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
                teleporter.placeEntity(toWorldIn, entityIn, f);
                toWorldIn.spawnEntity(entityIn);
                toWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }
        entityIn.setWorld(toWorldIn);
    }


    public void saveLocalPlayers(WorldServer worldServer){
        EntityPlayer[] players = playerEntityList.toArray(new EntityPlayerMP[0]);
        worldServer.addAsyncTask(() -> {
            for (EntityPlayer player:players){
                if(player instanceof EntityPlayerMP) writePlayerData((EntityPlayerMP) player);
            }
        });
    }


    /**
     * @author Te4hno
     */
    public void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn) {
        final double radius1 = radius*radius;
        double d0, d1, d2;
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = this.playerEntityList.get(i);
            if (entityplayermp != except && entityplayermp.dimension == dimension) {
                d0 = x - entityplayermp.posX;
                d1 = y - entityplayermp.posY;
                d2 = z - entityplayermp.posZ;

                if (d0 * d0 + d1 * d1 + d2 * d2 < radius1){
                    final NetworkManager first = entityplayermp.connection.netManager;
                    final int finalI = i;
                    first.exec(() -> {
                        first.sendPacketSync(packetIn);
                        double d01, d11, d21;
                        for (int i1 = finalI; i1 < playerEntityList.size(); ++i1) {
                            EntityPlayerMP entityplayermp1 = playerEntityList.get(i1);
                            if (entityplayermp1 != except && entityplayermp1.dimension == dimension) {
                                d01 = x - entityplayermp1.posX;
                                d11 = y - entityplayermp1.posY;
                                d21 = z - entityplayermp1.posZ;

                                if (d01 * d01 + d11 * d11 + d21 * d21 < radius1){
                                    entityplayermp1.connection.netManager.sendPacketSync(packetIn);
                                }
                            }
                        }
                    });
                    break;
                }
            }
        }
    }
}
