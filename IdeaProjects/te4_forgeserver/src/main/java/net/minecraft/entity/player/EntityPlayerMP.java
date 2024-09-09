package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.CooldownTrackerServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.*;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.server.network.packet.SElcaPacketDestroyEntities;
import plus.fserver.utls.collections.Int1LinkedList;

import static plus.fserver.ElcaCV.SERVER_COMPILE;


public class EntityPlayerMP extends EntityPlayer implements IContainerListener {
    private static final Logger LOGGER = LogManager.getLogger();
    //todo maybe remove?
    private String language = "en_US";
    public NetHandlerPlayServer connection;
    public final MinecraftServer server;
    public final PlayerInteractionManager interactionManager;
    public double managedPosX;
    public double managedPosZ;
    //private final List<Integer> entityRemoveQueue = new LinkedList<>();
    private final Int1LinkedList entityRemoveQueue = new Int1LinkedList();
    private final PlayerAdvancements advancements;
    private final StatisticsManagerServer statsFile;
    private float lastHealthScore = Float.MIN_VALUE;
    private int lastFoodScore = Integer.MIN_VALUE;
    private int lastAirScore = Integer.MIN_VALUE;
    private int lastArmorScore = Integer.MIN_VALUE;
    private int lastLevelScore = Integer.MIN_VALUE;
    private int lastExperienceScore = Integer.MIN_VALUE;
    private float lastHealth = -1.0E8F;
    private int lastFoodLevel = -99999999;
    private boolean wasHungry = true;
    private int lastExperience = -99999999;
    private byte respawnInvulnerabilityTicks = 60;
    private EntityPlayer.EnumChatVisibility chatVisibility;
    //todo maybe remove?
    private boolean chatColours = true;
    private long playerLastActiveTime = System.currentTimeMillis();
    private Entity spectatingEntity;
    private boolean invulnerableDimensionChange;
    //todo maybe optional remove?
    private boolean seenCredits;
    private final RecipeBookServer recipeBook = new RecipeBookServer();
    private Vec3d levitationStartPos;
    private int levitatingSince;
    private boolean disconnected;
    private Vec3d enteredNetherPosition;
    public int currentWindowId;
    public boolean isChangingQuantityOnly;
    public int ping;
    public boolean queuedEndExit;
    public short renderRadius, prevRenderRadius;

    //not used
    public EntityPlayerMP(World world){
        this(world.getMinecraftServer(), (WorldServer) world, null, null);
    }


    public EntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
        super(worldIn, profile);
        interactionManagerIn.player = this;
        this.interactionManager = interactionManagerIn;
        BlockPos blockpos = worldIn.provider.getRandomizedSpawnPoint();

        this.server = server;
        this.statsFile = server.getPlayerList().getPlayerStatsFile(this);
        this.advancements = server.getPlayerList().getPlayerAdvancements(this);
        this.stepHeight = 1.0F;
        this.moveToBlockPosAndAngles(blockpos, 0.0F, 0.0F);

        while (!worldIn.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.posY < 255.0D) {
            this.setPosition(this.posX, this.posY + 1.0D, this.posZ);
        }
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("playerGameType", 99)) {
            if (this.getServer().getForceGamemode()) {
                this.interactionManager.setGameType(this.getServer().getGameType());
            } else {
                this.interactionManager.setGameType(GameType.getByID(nbt.getInteger("playerGameType")));
            }
        }

        if (nbt.hasKey("enteredNetherPosition", 10)) {
            NBTTagCompound nbttagcompound = nbt.getCompoundTag("enteredNetherPosition");
            this.enteredNetherPosition = new Vec3d(nbttagcompound.getDouble("x"), nbttagcompound.getDouble("y"), nbttagcompound.getDouble("z"));
        }

        this.seenCredits = nbt.getBoolean("seenCredits");

        if (nbt.hasKey("recipeBook", 10)) {
            this.recipeBook.read(nbt.getCompoundTag("recipeBook"));
        }
    }


    public static void registerFixesPlayerMP(DataFixer dataFixer) {
        dataFixer.registerWalker(FixTypes.PLAYER, (fixer, compound, versionIn) -> {
            if (compound.hasKey("RootVehicle", 10)) {
                NBTTagCompound nbttagcompound = compound.getCompoundTag("RootVehicle");

                if (nbttagcompound.hasKey("Entity", 10)) {
                    nbttagcompound.setTag("Entity", fixer.process(FixTypes.ENTITY, nbttagcompound.getCompoundTag("Entity"), versionIn));
                }
            }
            return compound;
        });
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("playerGameType", this.interactionManager.getGameType().getID());
        nbt.setBoolean("seenCredits", this.seenCredits);

        if (this.enteredNetherPosition != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setDouble("x", this.enteredNetherPosition.x);
            nbttagcompound.setDouble("y", this.enteredNetherPosition.y);
            nbttagcompound.setDouble("z", this.enteredNetherPosition.z);
            nbt.setTag("enteredNetherPosition", nbttagcompound);
        }

        Entity entity1 = this.getLowestRidingEntity();
        Entity entity = this.getRidingEntity();

        if (entity != null && entity1 != this && entity1.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            entity1.writeToNBTOptional(nbttagcompound2);
            nbttagcompound1.setUniqueId("Attach", entity.getUniqueID());
            nbttagcompound1.setTag("Entity", nbttagcompound2);
            nbt.setTag("RootVehicle", nbttagcompound1);
        }
        nbt.setTag("recipeBook", this.recipeBook.write());
    }


    @Override
    public void addExperienceLevel(int levels) {
        super.addExperienceLevel(levels);
        this.lastExperience = -1;
    }


    @Override
    public void onEnchant(ItemStack enchantedItem, int cost) {
        super.onEnchant(enchantedItem, cost);
        this.lastExperience = -1;
    }


    public void addSelfToInternalCraftingInventory() {
        this.openContainer.addListener(this);
    }


    @Override
    public void sendEnterCombat() {
        super.sendEnterCombat();
        this.connection.netManager.sendPacketE(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTER_COMBAT));
    }


    @Override
    public void sendEndCombat() {
        super.sendEndCombat();
        this.connection.netManager.sendPacketE(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.END_COMBAT));
    }


    @Override
    protected void onInsideBlock(IBlockState state) {
        CriteriaTriggers.ENTER_BLOCK.trigger(this, state);
    }


    @Override
    protected CooldownTracker createCooldownTracker() {
        return new CooldownTrackerServer(this);
    }


    @Override
    public void onUpdate() {
        this.interactionManager.updateBlockRemoving();
        if(respawnInvulnerabilityTicks>0)--this.respawnInvulnerabilityTicks;

        if (this.hurtResistantTime > 0) {
            --this.hurtResistantTime;
        }

        this.openContainer.detectAndSendChanges();

        if ((!this.world.isRemote || SERVER_COMPILE) && this.openContainer != null && !this.openContainer.canInteractWith(this)) {
            this.closeScreen();
            this.openContainer = this.inventoryContainer;
        }

        Int1LinkedList.Node node = entityRemoveQueue.getListAndRem();
        if(node != null){
            connection.netManager.sendPacketE(new SElcaPacketDestroyEntities(node));
//            connection.netManager.exec(() -> {
//                Int1LinkedList.Node node0 = node;
//                while (node0 != null){
//                    connection.netManager.sendPacketSync(new SPacketDestroyEntities(node0.getValue()));
//                    node0 = node0.getParent();
//                }
//            });
        }

        Entity entity = this.getSpectatingEntity();

        if (entity != this) {
            if (entity.isEntityAlive()) {
                this.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                this.server.getPlayerList().serverUpdateMovingPlayer(this);

                if (this.isSneaking()) {
                    this.setSpectatingEntity(this);
                }
            } else {
                this.setSpectatingEntity(this);
            }
        }

        CriteriaTriggers.TICK.trigger(this);

        if (this.levitationStartPos != null) {
            CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.ticksExisted - this.levitatingSince);
        }

        this.advancements.flushDirty(this);
    }


    public void onUpdateEntity() {
        try {
            super.onUpdate();

            for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.inventory.getStackInSlot(i);

                if (!itemstack.isEmpty() && itemstack.getItem().isMap()) {
                    Packet<?> packet = ((ItemMapBase)itemstack.getItem()).createMapDataPacket(itemstack, this.world, this);

                    if (packet != null) {
                        this.connection.netManager.sendPacketE(packet);
                    }
                }
            }

            if (this.getHealth() != this.lastHealth || this.lastFoodLevel != this.foodStats.getFoodLevel() || this.foodStats.getSaturationLevel() == 0.0F != this.wasHungry) {
                this.connection.netManager.sendPacketE(new SPacketUpdateHealth(this.getHealth(), this.foodStats.getFoodLevel(), this.foodStats.getSaturationLevel()));
                this.lastHealth = this.getHealth();
                this.lastFoodLevel = this.foodStats.getFoodLevel();
                this.wasHungry = this.foodStats.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionAmount() != this.lastHealthScore) {
                this.lastHealthScore = this.getHealth() + this.getAbsorptionAmount();
                this.updateScorePoints(IScoreCriteria.HEALTH, MathHelper.ceil(this.lastHealthScore));
            }

            if (this.foodStats.getFoodLevel() != this.lastFoodScore) {
                this.lastFoodScore = this.foodStats.getFoodLevel();
                this.updateScorePoints(IScoreCriteria.FOOD, MathHelper.ceil((float)this.lastFoodScore));
            }

            if (this.getAir() != this.lastAirScore) {
                this.lastAirScore = this.getAir();
                this.updateScorePoints(IScoreCriteria.AIR, MathHelper.ceil((float)this.lastAirScore));
            }

            if (this.getTotalArmorValue() != this.lastArmorScore) {
                this.lastArmorScore = this.getTotalArmorValue();
                this.updateScorePoints(IScoreCriteria.ARMOR, MathHelper.ceil((float)this.lastArmorScore));
            }

            if (this.experienceTotal != this.lastExperienceScore) {
                this.lastExperienceScore = this.experienceTotal;
                this.updateScorePoints(IScoreCriteria.XP, MathHelper.ceil((float)this.lastExperienceScore));
            }

            if (this.experienceLevel != this.lastLevelScore) {
                this.lastLevelScore = this.experienceLevel;
                this.updateScorePoints(IScoreCriteria.LEVEL, MathHelper.ceil((float)this.lastLevelScore));
            }

            if (this.experienceTotal != this.lastExperience) {
                this.lastExperience = this.experienceTotal;
                this.connection.netManager.sendPacketE(new SPacketSetExperience(this.experience, this.experienceTotal, this.experienceLevel));
            }

            if (this.ticksExisted % 20 == 0) {
                CriteriaTriggers.LOCATION.trigger(this);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
            this.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }


    private void updateScorePoints(IScoreCriteria criteria, int points) {
        for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(criteria)) {
            Score score = this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective);
            score.setScorePoints(points);
        }
    }


    @Override
    public void onDeath(DamageSource cause) {
        if (net.minecraftforge.common.ForgeHooks.onLivingDeath(this, cause)) return;
        boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");
        this.connection.netManager.sendPacketE(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

        if (flag) {
            Team team = this.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().sendMessageToAllTeamMembers(this, this.getCombatTracker().getDeathMessage());
                }
                else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().sendMessageToTeamOrAllPlayers(this, this.getCombatTracker().getDeathMessage());
                }
            } else {
                this.server.getPlayerList().sendMessage(this.getCombatTracker().getDeathMessage());
            }
        }

        this.spawnShoulderEntities();

        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            captureDrops = true;
            capturedDrops.clear();
            this.destroyVanishingCursedItems();
            this.inventory.dropAllItems();

            captureDrops = false;
            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(this, cause, capturedDrops, recentlyHit > 0);
            if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
                for (net.minecraft.entity.item.EntityItem item : capturedDrops) {
                    this.world.spawnEntity(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : this.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
            Score score = this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (entitylivingbase != null) {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

            if (entitylist$entityegginfo != null) {
                this.addStat(entitylist$entityegginfo.entityKilledByStat);
            }

            entitylivingbase.awardKillScore(this, this.scoreValue, cause);
        }

        this.addStat(StatList.DEATHS);
        this.takeStat(StatList.TIME_SINCE_DEATH);
        this.extinguish();
        this.setFlag(0, false);
        this.getCombatTracker().reset();
    }


    @Override
    public void awardKillScore(Entity entity, int num, DamageSource src) {
        if (entity != this) {
            super.awardKillScore(entity, num, src);
            this.addScore(num);
            Collection<ScoreObjective> collection = this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TOTAL_KILL_COUNT);

            if (entity instanceof EntityPlayer) {
                this.addStat(StatList.PLAYER_KILLS);
                collection.addAll(this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.PLAYER_KILL_COUNT));
            } else {
                this.addStat(StatList.MOB_KILLS);
            }

            collection.addAll(this.awardTeamKillScores(entity));

            for (ScoreObjective scoreobjective : collection) {
                this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective).incrementScore();
            }
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, src);
        }
    }


    private Collection<ScoreObjective> awardTeamKillScores(Entity p_192038_1_) {
        String s = p_192038_1_ instanceof EntityPlayer ? p_192038_1_.getName() : p_192038_1_.getCachedUniqueIdString();
        ScorePlayerTeam scoreplayerteam = this.getWorldScoreboard().getPlayersTeam(this.getName());

        if (scoreplayerteam != null) {
            int i = scoreplayerteam.getColor().getColorIndex();

            if (i >= 0 && i < IScoreCriteria.KILLED_BY_TEAM.length) {
                for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.KILLED_BY_TEAM[i])) {
                    Score score = this.getWorldScoreboard().getOrCreateScore(s, scoreobjective);
                    score.incrementScore();
                }
            }
        }
        ScorePlayerTeam scoreplayerteam1 = this.getWorldScoreboard().getPlayersTeam(s);

        if (scoreplayerteam1 != null) {
            int j = scoreplayerteam1.getColor().getColorIndex();

            if (j >= 0 && j < IScoreCriteria.TEAM_KILL.length) {
                return this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.TEAM_KILL[j]);
            }
        }
        return Lists.newArrayList();
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            boolean flag = this.server.isDedicatedServer() && this.canPlayersAttack() && "fall".equals(source.damageType);

            if (!flag && this.respawnInvulnerabilityTicks > 0 && source != DamageSource.OUT_OF_WORLD) {
                return false;
            } else {
                if (source instanceof EntityDamageSource) {
                    Entity entity = source.getTrueSource();
                    if (entity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entity)) {
                        return false;
                    }
                    if (entity instanceof EntityArrow) {
                        EntityArrow entityarrow = (EntityArrow)entity;
                        if (entityarrow.shootingEntity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entityarrow.shootingEntity)) {
                            return false;
                        }
                    }
                }
                return super.attackEntityFrom(source, amount);
            }
        }
    }

    public boolean canAttackPlayer(EntityPlayer other) {
        return !this.canPlayersAttack() ? false : super.canAttackPlayer(other);
    }


    private boolean canPlayersAttack() {
        return this.server.isPVPEnabled();
    }


    @Override
    public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter) {
        if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(this, dimensionIn)) return this;
        this.invulnerableDimensionChange = true;

        if (this.dimension == 0 && dimensionIn == -1) {
            this.enteredNetherPosition = new Vec3d(this.posX, this.posY, this.posZ);
        } else if (this.dimension != -1 && dimensionIn != 0) {
            this.enteredNetherPosition = null;
        }

        if (this.dimension == 1 && dimensionIn == 1 && teleporter.isVanilla()) {
            this.world.removeEntity(this);
            if (!this.queuedEndExit) {
                this.queuedEndExit = true;
                this.connection.netManager.sendPacketE(new SPacketChangeGameState(4, this.seenCredits ? 0.0F : 1.0F));
                this.seenCredits = true;
            }
            return this;
        } else {
            if (this.dimension == 0 && dimensionIn == 1) {
                dimensionIn = 1;
            }
            this.server.getPlayerList().transferPlayerToDimension(this, dimensionIn, teleporter);
            this.connection.netManager.sendPacketE(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
            this.lastExperience = -1;
            this.lastHealth = -1.0F;
            this.lastFoodLevel = -1;
            return this;
        }
    }


    @Override
    public boolean isSpectatedByPlayer(EntityPlayerMP player) {
        if (player.isSpectator()) {
            return this.getSpectatingEntity() == this;
        } else {
            return this.isSpectator() ? false : super.isSpectatedByPlayer(player);
        }
    }



    private void sendTileEntityUpdate(TileEntity p_147097_1_) {
        if (p_147097_1_ != null) {
            SPacketUpdateTileEntity spacketupdatetileentity = p_147097_1_.getUpdatePacket();
            if (spacketupdatetileentity != null) {
                this.connection.netManager.sendPacketE(spacketupdatetileentity);
            }
        }
    }


    public void onItemPickup(Entity entityIn, int quantity) {
        super.onItemPickup(entityIn, quantity);
        this.openContainer.detectAndSendChanges();
    }


    public EntityPlayer.SleepResult trySleep(BlockPos bedLocation) {
        EntityPlayer.SleepResult entityplayer$sleepresult = super.trySleep(bedLocation);

        if (entityplayer$sleepresult == EntityPlayer.SleepResult.OK) {
            this.addStat(StatList.SLEEP_IN_BED);
            Packet<?> packet = new SPacketUseBed(this, bedLocation);
            this.getServerWorld().getEntityTracker().sendToTracking(this, packet);
            this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.connection.netManager.sendPacketE(packet);
            CriteriaTriggers.SLEPT_IN_BED.trigger(this);
        }
        return entityplayer$sleepresult;
    }


    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        if (this.isPlayerSleeping()) {
            this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(this, 2));
        }
        super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);

        if (this.connection != null) {
            this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }


    @Override
    public boolean startRiding(Entity entityIn, boolean force) {
        Entity entity = this.getRidingEntity();
        if (!super.startRiding(entityIn, force)) {
            return false;
        } else {
            Entity entity1 = this.getRidingEntity();
            if (entity1 != entity && this.connection != null) {
                this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            }
            return true;
        }
    }


    @Override
    public void dismountRidingEntity() {
        Entity entity = this.getRidingEntity();
        super.dismountRidingEntity();
        Entity entity1 = this.getRidingEntity();
        if (entity1 != entity && this.connection != null) {
            this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }


    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || this.isInvulnerableDimensionChange();
    }


    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}


    @Override
    protected void frostWalk(BlockPos pos) {
        if (!this.isSpectator()) super.frostWalk(pos);
    }


    public void handleFalling(double y, boolean onGroundIn) {
        int i = MathHelper.floor(this.posX);
        int j = MathHelper.floor(this.posY - 0.20000000298023224D);
        int k = MathHelper.floor(this.posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        IBlockState iblockstate = this.world.getBlockState(blockpos);

        if (iblockstate.getBlock().isAir(iblockstate, this.world, blockpos)) {
            BlockPos blockpos1 = blockpos.down();
            IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
            Block block = iblockstate1.getBlock();

            if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate)
            {
                blockpos = blockpos1;
                iblockstate = iblockstate1;
            }
        }
        super.updateFallState(y, onGroundIn, iblockstate, blockpos);
    }


    @Override
    public void openEditSign(TileEntitySign signTile) {
        signTile.setPlayer(this);
        this.connection.netManager.sendPacketE(new SPacketSignEditorOpen(signTile.getPos()));
    }


    public void getNextWindowId() {
        this.currentWindowId = this.currentWindowId % 100 + 1;
    }


    public void displayGui(IInteractionObject guiOwner) {
        if (guiOwner instanceof ILootContainer && ((ILootContainer)guiOwner).getLootTable() != null && this.isSpectator()) {
            this.sendStatusMessage((new TextComponentTranslation("container.spectatorCantOpen", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)), true);
        } else {
            this.getNextWindowId();
            this.connection.netManager.sendPacketE(new SPacketOpenWindow(this.currentWindowId, guiOwner.getGuiID(), guiOwner.getDisplayName()));
            this.openContainer = guiOwner.createContainer(this.inventory, this);
            this.openContainer.windowId = this.currentWindowId;
            this.openContainer.addListener(this);
            MinecraftForge.EVENT_BUS.postF(new PlayerContainerEvent.Open(this, this.openContainer));
        }
    }


    public void displayGUIChest(IInventory chestInventory) {
        if (chestInventory instanceof ILootContainer && ((ILootContainer)chestInventory).getLootTable() != null && this.isSpectator()) {
            this.sendStatusMessage((new TextComponentTranslation("container.spectatorCantOpen", new Object[0])).setStyle((new Style()).setColor(TextFormatting.RED)), true);
        } else {
            if (this.openContainer != this.inventoryContainer) {
                this.closeScreen();
            }
            if (chestInventory instanceof ILockableContainer) {
                ILockableContainer ilockablecontainer = (ILockableContainer)chestInventory;

                if (ilockablecontainer.isLocked() && !this.canOpen(ilockablecontainer.getLockCode()) && !this.isSpectator()) {
                    this.connection.netManager.addToQueueE(new SPacketChat(new TextComponentTranslation("container.isLocked", new Object[] {chestInventory.getDisplayName()}), ChatType.GAME_INFO));
                    this.connection.netManager.sendPacketE(new SPacketSoundEffect(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, this.posX, this.posY, this.posZ, 1.0F, 1.0F));
                    return;
                }
            }

            this.getNextWindowId();

            if (chestInventory instanceof IInteractionObject) {
                this.connection.netManager.sendPacketE(new SPacketOpenWindow(this.currentWindowId, ((IInteractionObject)chestInventory).getGuiID(), chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
                this.openContainer = ((IInteractionObject)chestInventory).createContainer(this.inventory, this);
            } else {
                this.connection.netManager.sendPacketE(new SPacketOpenWindow(this.currentWindowId, "minecraft:container", chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
                this.openContainer = new ContainerChest(this.inventory, chestInventory, this);
            }
            this.openContainer.windowId = this.currentWindowId;
            this.openContainer.addListener(this);
            MinecraftForge.EVENT_BUS.postF(new PlayerContainerEvent.Open(this, this.openContainer));
        }
    }


    @Override
    public void displayVillagerTradeGui(IMerchant villager) {
        this.getNextWindowId();
        this.openContainer = new ContainerMerchant(this.inventory, villager, this.world);
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.addListener(this);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.postF(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(this, this.openContainer));
        IInventory iinventory = ((ContainerMerchant)this.openContainer).getMerchantInventory();
        ITextComponent itextcomponent = villager.getDisplayName();
        this.connection.netManager.sendPacketE(new SPacketOpenWindow(this.currentWindowId, "minecraft:villager", itextcomponent, iinventory.getSizeInventory()));
        MerchantRecipeList merchantrecipelist = villager.getRecipes(this);

        if (merchantrecipelist != null) {
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(this.currentWindowId);
            merchantrecipelist.writeToBuf(packetbuffer);
            this.connection.netManager.sendPacketE(new SPacketCustomPayload("MC|TrList", packetbuffer));
        }
    }


    @Override
    public void openGuiHorseInventory(AbstractHorse horse, IInventory inventoryIn) {
        if (this.openContainer != this.inventoryContainer) {
            this.closeScreen();
        }
        this.getNextWindowId();
        this.connection.netManager.sendPacketE(new SPacketOpenWindow(this.currentWindowId, "EntityHorse", inventoryIn.getDisplayName(), inventoryIn.getSizeInventory(), horse.getEntityId()));
        this.openContainer = new ContainerHorseInventory(this.inventory, inventoryIn, horse, this);
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.addListener(this);
    }


    @Override
    public void openBook(ItemStack stack, EnumHand hand) {
        Item item = stack.getItem();
        if (item == Items.WRITTEN_BOOK) {
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeEnumValue(hand);
            this.connection.netManager.sendPacketE(new SPacketCustomPayload("MC|BOpen", packetbuffer));
        }
    }


    @Override
    public void displayGuiCommandBlock(TileEntityCommandBlock commandBlock) {
        commandBlock.setSendToClient(true);
        this.sendTileEntityUpdate(commandBlock);
    }


    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (!(containerToSend.getSlot(slotInd) instanceof SlotCrafting)) {
            if (containerToSend == this.inventoryContainer) {
                CriteriaTriggers.INVENTORY_CHANGED.trigger(this, this.inventory);
            }
            if (!this.isChangingQuantityOnly) {
                this.connection.netManager.sendPacketE(new SPacketSetSlot(containerToSend.windowId, slotInd, stack));
            }
        }
    }


    public void sendContainerToPlayer(Container containerIn) {
        this.sendAllContents(containerIn, containerIn.getInventory());
    }


    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
        this.connection.netManager.addToQueueE(new SPacketWindowItems(containerToSend.windowId, itemsList));
        this.connection.netManager.sendPacketE(new SPacketSetSlot(-1, -1, this.inventory.getItemStack()));
    }


    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
        this.connection.netManager.sendPacketE(new SPacketWindowProperty(containerIn.windowId, varToUpdate, newValue));
    }


    @Override
    public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
        for (int i = 0; i < inventory.getFieldCount(); ++i) {
            this.connection.netManager.sendPacketE(new SPacketWindowProperty(containerIn.windowId, i, inventory.getField(i)));
        }
    }


    @Override
    public void closeScreen() {
        int id;
        NetHandlerPlayServer conn = this.connection;
        conn.netManager.sendPacketE(new SPacketCloseWindow(id = this.openContainer.windowId));
        conn.removeTransactions(id);
        this.closeContainer();
    }


    public void updateHeldItem() {
        if (!this.isChangingQuantityOnly) {
            this.connection.netManager.sendPacketE(new SPacketSetSlot(-1, -1, this.inventory.getItemStack()));
        }
    }


    public void closeContainer() {
        this.openContainer.onContainerClosed(this);
        MinecraftForge.EVENT_BUS.postF(new PlayerContainerEvent.Close(this, this.openContainer));
        this.openContainer = this.inventoryContainer;
    }


    public void setEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking) {
        if (this.isRiding()) {
            if (strafe >= -1.0F && strafe <= 1.0F) {
                this.moveStrafing = strafe;
            }
            if (forward >= -1.0F && forward <= 1.0F) {
                this.moveForward = forward;
            }
            this.isJumping = jumping;
            this.setSneaking(sneaking);
        }
    }


    @Override
    public void addStat(StatBase stat, int amount) {
        if (stat != null) {
            this.statsFile.increaseStat(this, stat, amount);
            for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria())) {
                this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective).increaseScore(amount);
            }
        }
    }


    @Override
    public void takeStat(StatBase stat) {
        if (stat != null) {
            this.statsFile.unlockAchievement(this, stat, 0);
            for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria())) {
                this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective).setScorePoints(0);
            }
        }
    }


    @Override
    public void unlockRecipes(List<IRecipe> recipes) {
        this.recipeBook.add(recipes, this);
    }


    @Override
    public void unlockRecipes(ResourceLocation[] recipes) {
        List<IRecipe> list = Lists.newArrayList();
        for (ResourceLocation resourcelocation : recipes) {
            list.add(CraftingManager.getRecipe(resourcelocation));
        }
        this.unlockRecipes(list);
    }


    @Override
    public void resetRecipes(List<IRecipe> recipes) {
        this.recipeBook.remove(recipes, this);
    }


    public void mountEntityAndWakeUp() {
        this.disconnected = true;
        this.removePassengers();
        if (this.sleeping) {
            this.wakeUpPlayer(true, false, false);
        }
    }


    public boolean hasDisconnected() {
        return this.disconnected;
    }


    public void setPlayerHealthUpdated() {
        this.lastHealth = -1.0E8F;
    }


    @Override
    public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {
        this.connection.sendPacket(new SPacketChat(chatComponent, actionBar ? ChatType.GAME_INFO : ChatType.CHAT));
    }


    @Override
    protected void onItemUseFinish() {
        if (!this.activeItemStack.isEmpty() && this.isHandActive()) {
            this.connection.netManager.sendPacketE(new SPacketEntityStatus(this, (byte)9));
            super.onItemUseFinish();
        }
    }


    public void copyFrom(EntityPlayerMP that, boolean keepEverything) {
        if (keepEverything) {
            this.inventory.copyInventory(that.inventory);
            this.setHealth(that.getHealth());
            this.foodStats = that.foodStats;
            this.experienceLevel = that.experienceLevel;
            this.experienceTotal = that.experienceTotal;
            this.experience = that.experience;
            this.setScore(that.getScore());
            this.lastPortalPos = that.lastPortalPos;
            this.lastPortalVec = that.lastPortalVec;
            this.teleportDirection = that.teleportDirection;
        } else if (this.world.getGameRules().keepInventory.getBoolean() || that.isSpectator()) {
            this.inventory.copyInventory(that.inventory);
            this.experienceLevel = that.experienceLevel;
            this.experienceTotal = that.experienceTotal;
            this.experience = that.experience;
            this.setScore(that.getScore());
        }

        this.xpSeed = that.xpSeed;
        this.enderChest = that.enderChest;
        this.getDataManager().set(PLAYER_MODEL_FLAG, that.getDataManager().get(PLAYER_MODEL_FLAG));
        this.lastExperience = -1;
        this.lastHealth = -1.0F;
        this.lastFoodLevel = -1;
        this.recipeBook.copyFrom(that.recipeBook);
        this.entityRemoveQueue.addAll(that.entityRemoveQueue);
        this.seenCredits = that.seenCredits;
        this.enteredNetherPosition = that.enteredNetherPosition;
        this.setLeftShoulderEntity(that.getLeftShoulderEntity());
        this.setRightShoulderEntity(that.getRightShoulderEntity());

        this.spawnChunkMap = that.spawnChunkMap;
        this.spawnForcedMap = that.spawnForcedMap;

        if(that.dimension != 0) {
            this.spawnPos = that.spawnPos;
            this.spawnForced = that.spawnForced;
        }

        //Copy over a section of the Entity Data from the old player.
        //Allows mods to specify data that persists after players respawn.
        NBTTagCompound old = that.getEntityData();
        if (old.hasKey(PERSISTED_NBT_TAG)) {
            getEntityData().setTag(PERSISTED_NBT_TAG, old.getCompoundTag(PERSISTED_NBT_TAG));
        }
        ForgeEventFactory.onPlayerClone(this, that, !keepEverything);
    }


    @Override
    protected void onNewPotionEffect(PotionEffect id) {
        super.onNewPotionEffect(id);
        this.connection.netManager.sendPacketE(new SPacketEntityEffect(this.getEntityId(), id));

        if (id.getPotion() == MobEffects.LEVITATION) {
            this.levitatingSince = this.ticksExisted;
            this.levitationStartPos = new Vec3d(this.posX, this.posY, this.posZ);
        }
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
    }


    @Override
    protected void onChangedPotionEffect(PotionEffect id, boolean flag) {
        super.onChangedPotionEffect(id, flag);
        this.connection.netManager.sendPacketE(new SPacketEntityEffect(this.getEntityId(), id));
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
    }


    @Override
    protected void onFinishedPotionEffect(PotionEffect effect) {
        super.onFinishedPotionEffect(effect);
        this.connection.netManager.sendPacketE(new SPacketRemoveEntityEffect(this.getEntityId(), effect.getPotion()));

        if (effect.getPotion() == MobEffects.LEVITATION) {
            this.levitationStartPos = null;
        }
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this);
    }


    @Override
    public void setPositionAndUpdate(double x, double y, double z) {
        this.connection.setPlayerLocation(x, y, z, this.rotationYaw, this.rotationPitch);
    }


    @Override
    public void onCriticalHit(Entity entityHit) {
        this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(entityHit, 4));
    }


    @Override
    public void onEnchantmentCritical(Entity entityHit) {
        this.getServerWorld().getEntityTracker().sendToTrackingAndSelf(this, new SPacketAnimation(entityHit, 5));
    }


    @Override
    public void sendPlayerAbilities() {
        if (this.connection != null) {
            this.connection.netManager.sendPacketE(new SPacketPlayerAbilities(this.capabilities));
            this.updatePotionMetadata();
        }
    }


    public WorldServer getServerWorld() {
        return (WorldServer)this.world;
    }


    @Override
    public void setGameType(GameType gameType) {
        this.interactionManager.setGameType(gameType);
        this.connection.netManager.sendPacketE(new SPacketChangeGameState(3, (float)gameType.getID()));
        if (gameType == GameType.SPECTATOR) {
            this.spawnShoulderEntities();
            this.dismountRidingEntity();
        } else {
            this.setSpectatingEntity(this);
        }
        this.sendPlayerAbilities();
        this.markPotionsDirty();
    }


    @Override
    public boolean isSpectator() {
        return this.interactionManager.getGameType() == GameType.SPECTATOR;
    }


    @Override
    public boolean isCreative() {
        return this.interactionManager.getGameType() == GameType.CREATIVE;
    }


    @Override
    public void sendMessage(ITextComponent component) {
        this.connection.sendPacket(new SPacketChat(component));
    }


    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        if ("seed".equals(commandName) && !this.server.isDedicatedServer()) {
            return true;
        } else if (!"tell".equals(commandName) && !"help".equals(commandName) && !"me".equals(commandName) && !"trigger".equals(commandName)) {
            if (this.server.getPlayerList().canSendCommands(this.getGameProfile())) {
                UserListOpsEntry userlistopsentry = this.server.getPlayerList().getOppedPlayers().getEntry(this.getGameProfile());

                if (userlistopsentry != null) {
                    return userlistopsentry.getPermissionLevel() >= permLevel;
                } else {
                    return this.server.getOpPermissionLevel() >= permLevel;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }


    public String getPlayerIP() {
        String s = this.connection.netManager.getRemoteAddress().toString();
        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));
        return s;
    }


    public void handleClientSettings(CPacketClientSettings packetIn) {
        renderRadius = PlayerChunkMap.radByFormula(packetIn.getViewRadius());
        this.language = packetIn.getLang();
        this.chatVisibility = packetIn.getChatVisibility();
        this.chatColours = packetIn.isColorsEnabled();
        this.getDataManager().set(PLAYER_MODEL_FLAG, (byte) packetIn.getModelPartFlags());
        this.getDataManager().set(MAIN_HAND, (byte) (packetIn.getMainHand() == EnumHandSide.LEFT ? 0 : 1));
    }


    public EntityPlayer.EnumChatVisibility getChatVisibility() {
        return this.chatVisibility;
    }


    public void loadResourcePack(String url, String hash) {
        this.connection.netManager.sendPacketE(new SPacketResourcePackSend(url, hash));
    }


    @Override
    public BlockPos getPosition() {
        return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
    }


    public void markPlayerActive() {
        this.playerLastActiveTime = MinecraftServer.getCurrentTimeMillis();
    }


    public StatisticsManagerServer getStatFile() {
        return this.statsFile;
    }


    public RecipeBookServer getRecipeBook() {
        return this.recipeBook;
    }


    public void removeEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayer) {
            this.connection.netManager.sendPacketE(new SPacketDestroyEntities(entityIn.getEntityId()));
        } else {
            this.entityRemoveQueue.add(entityIn.getEntityId());
        }
    }


    public void addEntity(Entity entityIn) {
        this.entityRemoveQueue.removeVal(entityIn.getEntityId());
    }


    @Override
    protected void updatePotionMetadata() {
        if (this.isSpectator()) {
            this.resetPotionEffectMetadata();
            this.setInvisible(true);
        } else {
            super.updatePotionMetadata();
        }
        this.getServerWorld().getEntityTracker().updateVisibility(this);
    }


    public Entity getSpectatingEntity() {
        return this.spectatingEntity == null ? this : this.spectatingEntity;
    }


    public void setSpectatingEntity(Entity entityToSpectate) {
        Entity entity = this.getSpectatingEntity();
        this.spectatingEntity = entityToSpectate == null ? this : entityToSpectate;
        if (entity != this.spectatingEntity) {
            this.connection.netManager.sendPacketE(new SPacketCamera(this.spectatingEntity));
            this.setPositionAndUpdate(this.spectatingEntity.posX, this.spectatingEntity.posY, this.spectatingEntity.posZ);
        }
    }


    @Override
    protected void decrementTimeUntilPortal() {
        if (this.timeUntilPortal > 0 && !this.invulnerableDimensionChange) {
            --this.timeUntilPortal;
        }
    }


    @Override
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (this.interactionManager.getGameType() == GameType.SPECTATOR) {
            this.setSpectatingEntity(targetEntity);
        } else {
            super.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }


    public long getLastActiveTime() {
        return this.playerLastActiveTime;
    }


    @Nullable
    public ITextComponent getTabListDisplayName() {
        return null;
    }


    public void swingArm(EnumHand hand) {
        super.swingArm(hand);
        this.resetCooldown();
    }


    public void addPacketToQueue(Packet<?> packet){
        connection.netManager.addToQueueE(packet);
    }


    public boolean isInvulnerableDimensionChange() {
        return this.invulnerableDimensionChange;
    }


    public void clearInvulnerableDimensionChange() {
        this.invulnerableDimensionChange = false;
    }


    public void setElytraFlying() {
        this.setFlag(7, true);
    }


    public void clearElytraFlying() {
        this.setFlag(7, true);
        this.setFlag(7, false);
    }


    public PlayerAdvancements getAdvancements() {
        return this.advancements;
    }


    @Nullable
    public Vec3d getEnteredNetherPosition() {
        return this.enteredNetherPosition;
    }


    public void sendBlockBreakProgressToTracking(BlockPos pos, int progress) {
        if(trackerEntry == null)return;
        SPacketBlockBreakAnim packet = null;
        for (EntityPlayerMP player : trackerEntry.trackingPlayers) {
            if (player != null && player != this) {
                double d0 = (double)pos.getX() - player.posX;
                double d1 = (double)pos.getY() - player.posY;
                double d2 = (double)pos.getZ() - player.posZ;

                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                    if(packet == null)packet = new SPacketBlockBreakAnim(getEntityId(), pos, progress);
                    player.connection.netManager.sendPacketE(packet);
                }
            }
        }
    }
}