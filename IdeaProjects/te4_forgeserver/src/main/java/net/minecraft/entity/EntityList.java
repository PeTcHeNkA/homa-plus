package net.minecraft.entity;

import net.minecraft.util.*;
import javax.annotation.*;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.world.*;
import net.minecraft.nbt.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.effect.*;
import java.util.*;
import java.util.function.Function;
import net.minecraft.entity.boss.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.item.*;
import com.google.common.collect.*;
import net.minecraft.stats.*;
import plus.fserver.entity.SpawnEntityFactory;
import plus.tson.utl.Te4HashSet;
import static net.minecraft.server.MinecraftServer.LOGGER;


public class EntityList {
    public static final ResourceLocation LIGHTNING_BOLT;
    private static final ResourceLocation PLAYER;
    public static final Map<ResourceLocation, EntityEggInfo> ENTITY_EGGS;
    private static final Set<ResourceLocation> EXTRA_NAMES;

    @Nullable
    public static ResourceLocation getKey(final Entity entityIn) {
        return getKey(entityIn.getClass());
    }


    @Nullable
    public static ResourceLocation getKey(final Class<? extends Entity> entityIn) {
        final EntityEntry entry = EntityRegistry.getEntry(entityIn);
        return (entry == null) ? null : entry.getRegistryName();
    }


    @Nullable
    public static String getEntityString(final Entity entityIn) {
        final EntityEntry entry = EntityRegistry.getEntry(entityIn.getClass());
        return (entry == null) ? null : entry.getName();
    }


    @Nullable
    public static String getTranslationName(@Nullable final ResourceLocation entityType) {
        final EntityEntry entry = ForgeRegistries.ENTITIES.getValue(entityType);
        return (entry == null) ? null : entry.getName();
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public static Class<? extends Entity> getClassFromID(final int entityID) {
        final EntityEntry entry = GameData.getEntityRegistry().getValue(entityID);
        return (entry == null) ? null : entry.getEntityClass();
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public static Class<? extends Entity> getClassFromName(final String p_192839_0_) {
        final EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(p_192839_0_));
        return (entry == null) ? null : entry.getEntityClass();
    }

    public static int getID(final Class<? extends Entity> p_getID_0_) {
        final EntityEntry entry = EntityRegistry.getEntry(p_getID_0_);
        return (entry == null) ? -1 : GameData.getEntityRegistry().getID(entry);
    }


    @Nullable
    public static Class<? extends Entity> getClass(final ResourceLocation p_getClass_0_) {
        final EntityEntry entry = ForgeRegistries.ENTITIES.getValue(p_getClass_0_);
        return (entry == null) ? null : entry.getEntityClass();
    }


    @Nullable
    public static Entity newEntity(@Nullable final Class<? extends Entity> clazz, final World worldIn) {
        if (clazz == null) {
            return null;
        }
        try {
            final EntityEntry entry = EntityRegistry.getEntry(clazz);
            if (entry != null) {
                return entry.newInstance(worldIn);
            }
            return SpawnEntityFactory.make(clazz).apply(worldIn);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public static Entity createEntityByID(final int entityID, final World worldIn) {
        final EntityEntry entry = GameData.getEntityRegistry().getValue(entityID);
        return (entry == null) ? null : entry.newInstance(worldIn);
    }


    @Nullable
    public static Entity createEntityByIDFromName(final ResourceLocation name, final World worldIn) {
        final EntityEntry entry = ForgeRegistries.ENTITIES.getValue(name);
        return (entry == null) ? null : entry.newInstance(worldIn);
    }


    @Nullable
    public static Entity createEntityFromNBT(final NBTTagCompound nbt, final World worldIn) {
        final ResourceLocation resourcelocation = new ResourceLocation(nbt.getString("id"));
        Entity entity = createEntityByIDFromName(resourcelocation, worldIn);
        if (entity == null) {
            LOGGER.warn("Skipping Entity with id {}", resourcelocation);
        }
        else {
            try {
                entity.readFromNBT(nbt);
            }
            catch (Exception e) {
                FMLLog.log.error("An Entity {}({}) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", (Object)nbt.getString("id"), (Object)entity.getName(), (Object)e);
                entity = null;
            }
        }
        return entity;
    }


    public static Set<ResourceLocation> getEntityNameList() {
        return Sets.union(ForgeRegistries.ENTITIES.getKeys(), EntityList.EXTRA_NAMES);
    }

    public static boolean isMatchingName(final Entity entityIn, final ResourceLocation entityName) {
        final ResourceLocation resourcelocation = getKey(entityIn.getClass());
        if (resourcelocation != null) {
            return resourcelocation.equals(entityName);
        }
        if (entityIn instanceof EntityPlayer) {
            return EntityList.PLAYER.equals(entityName);
        }
        return entityIn instanceof EntityLightningBolt && EntityList.LIGHTNING_BOLT.equals(entityName);
    }

    public static boolean isRegistered(final ResourceLocation entityName) {
        return EntityList.PLAYER.equals(entityName) || getEntityNameList().contains(entityName);
    }

    public static String getValidTypeNames() {
        final StringBuilder stringbuilder = new StringBuilder();
        for (final ResourceLocation resourcelocation : getEntityNameList()) {
            stringbuilder.append(resourcelocation).append(", ");
        }
        stringbuilder.append(EntityList.PLAYER);
        return stringbuilder.toString();
    }


    public static void init() {
        register(1 , "item"                 , EntityItem.class                , EntityItem::new                , "Item");
        register(2 , "xp_orb"               , EntityXPOrb.class               , EntityXPOrb::new               , "XPOrb");
        register(3 , "area_effect_cloud"    , EntityAreaEffectCloud.class     , EntityAreaEffectCloud::new     , "AreaEffectCloud");
        register(4 , "elder_guardian"       , EntityElderGuardian.class       , EntityElderGuardian::new       , "ElderGuardian");
        register(5 , "wither_skeleton"      , EntityWitherSkeleton.class      , EntityWitherSkeleton::new      , "WitherSkeleton");
        register(6 , "stray"                , EntityStray.class               , EntityStray::new               , "Stray");
        register(7 , "egg"                  , EntityEgg.class                 , EntityEgg::new                 , "ThrownEgg");
        register(8 , "leash_knot"           , EntityLeashKnot.class           , EntityLeashKnot::new           , "LeashKnot");
        register(9 , "painting"             , EntityPainting.class            , EntityPainting::new            , "Painting");
        register(10, "arrow"                , EntityTippedArrow.class         , EntityTippedArrow::new         , "Arrow");
        register(11, "snowball"             , EntitySnowball.class            , EntitySnowball::new            , "Snowball");
        register(12, "fireball"             , EntityLargeFireball.class       , EntityLargeFireball::new       , "Fireball");
        register(13, "small_fireball"       , EntitySmallFireball.class       , EntitySmallFireball::new       , "SmallFireball");
        register(14, "ender_pearl"          , EntityEnderPearl.class          , EntityEnderPearl::new          , "ThrownEnderpearl");
        register(15, "eye_of_ender_signal"  , EntityEnderEye.class            , EntityEnderEye::new            , "EyeOfEnderSignal");
        register(16, "potion"               , EntityPotion.class              , EntityPotion::new              , "ThrownPotion");
        register(17, "xp_bottle"            , EntityExpBottle.class           , EntityExpBottle::new           , "ThrownExpBottle");
        register(18, "item_frame"           , EntityItemFrame.class           , EntityItemFrame::new           , "ItemFrame");
        register(19, "wither_skull"         , EntityWitherSkull.class         , EntityWitherSkull::new         , "WitherSkull");
        register(20, "tnt"                  , EntityTNTPrimed.class           , EntityTNTPrimed::new           , "PrimedTnt");
        register(21, "falling_block"        , EntityFallingBlock.class        , EntityFallingBlock::new        , "FallingSand");
        register(22, "fireworks_rocket"     , EntityFireworkRocket.class      , EntityFireworkRocket::new      , "FireworksRocketEntity");
        register(23, "husk"                 , EntityHusk.class                , EntityHusk::new                , "Husk");
        register(24, "spectral_arrow"       , EntitySpectralArrow.class       , EntitySpectralArrow::new       , "SpectralArrow");
        register(25, "shulker_bullet"       , EntityShulkerBullet.class       , EntityShulkerBullet::new       , "ShulkerBullet");
        register(26, "dragon_fireball"      , EntityDragonFireball.class      , EntityDragonFireball::new      , "DragonFireball");
        register(27, "zombie_villager"      , EntityZombieVillager.class      , EntityZombieVillager::new      , "ZombieVillager");
        register(28, "skeleton_horse"       , EntitySkeletonHorse.class       , EntitySkeletonHorse::new       , "SkeletonHorse");
        register(29, "zombie_horse"         , EntityZombieHorse.class         , EntityZombieHorse::new         , "ZombieHorse");
        register(30, "armor_stand"          , EntityArmorStand.class          , EntityArmorStand::new          , "ArmorStand");
        register(31, "donkey"               , EntityDonkey.class              , EntityDonkey::new              , "Donkey");
        register(32, "mule"                 , EntityMule.class                , EntityMule::new                , "Mule");
        register(33, "evocation_fangs"      , EntityEvokerFangs.class         , EntityEvokerFangs::new         , "EvocationFangs");
        register(34, "evocation_illager"    , EntityEvoker.class              , EntityEvoker::new              , "EvocationIllager");
        register(35, "vex"                  , EntityVex.class                 , EntityVex::new                 , "Vex");
        register(36, "vindication_illager"  , EntityVindicator.class          , EntityVindicator::new          , "VindicationIllager");
        register(37, "illusion_illager"     , EntityIllusionIllager.class     , EntityIllusionIllager::new     , "IllusionIllager");
        register(40, "commandblock_minecart", EntityMinecartCommandBlock.class, EntityMinecartCommandBlock::new, EntityMinecart.Type.COMMAND_BLOCK.getName());
        register(41, "boat"                 , EntityBoat.class                , EntityBoat::new                , "Boat");
        register(42, "minecart"             , EntityMinecartEmpty.class       , EntityMinecartEmpty::new       , EntityMinecart.Type.RIDEABLE.getName());
        register(43, "chest_minecart"       , EntityMinecartChest.class       , EntityMinecartChest::new       , EntityMinecart.Type.CHEST.getName());
        register(44, "furnace_minecart"     , EntityMinecartFurnace.class     , EntityMinecartFurnace::new     , EntityMinecart.Type.FURNACE.getName());
        register(45, "tnt_minecart"         , EntityMinecartTNT.class         , EntityMinecartTNT::new         , EntityMinecart.Type.TNT.getName());
        register(46, "hopper_minecart"      , EntityMinecartHopper.class      , EntityMinecartHopper::new      , EntityMinecart.Type.HOPPER.getName());
        register(47, "spawner_minecart"     , EntityMinecartMobSpawner.class  , EntityMinecartMobSpawner::new  , EntityMinecart.Type.SPAWNER.getName());
        register(50, "creeper"              , EntityCreeper.class             , EntityCreeper::new             , "Creeper");
        register(51, "skeleton"             , EntitySkeleton.class            , EntitySkeleton::new            , "Skeleton");
        register(52, "spider"               , EntitySpider.class              , EntitySpider::new              , "Spider");
        register(53, "giant"                , EntityGiantZombie.class         , EntityGiantZombie::new         , "Giant");
        register(54, "zombie"               , EntityZombie.class              , EntityZombie::new              , "Zombie");
        register(55, "slime"                , EntitySlime.class               , EntitySlime::new               , "Slime");
        register(56, "ghast"                , EntityGhast.class               , EntityGhast::new               , "Ghast");
        register(57, "zombie_pigman"        , EntityPigZombie.class           , EntityPigZombie::new           , "PigZombie");
        register(58, "enderman"             , EntityEnderman.class            , EntityEnderman::new            , "Enderman");
        register(59, "cave_spider"          , EntityCaveSpider.class          , EntityCaveSpider::new          , "CaveSpider");
        register(60, "silverfish"           , EntitySilverfish.class          , EntitySilverfish::new          , "Silverfish");
        register(61, "blaze"                , EntityBlaze.class               , EntityBlaze::new               , "Blaze");
        register(62, "magma_cube"           , EntityMagmaCube.class           , EntityMagmaCube::new           , "LavaSlime");
        register(63, "ender_dragon"         , EntityDragon.class              , EntityDragon::new              , "EnderDragon");
        register(64, "wither"               , EntityWither.class              , EntityWither::new              , "WitherBoss");
        register(65, "bat"                  , EntityBat.class                 , EntityBat::new                 , "Bat");
        register(66, "witch"                , EntityWitch.class               , EntityWitch::new               , "Witch");
        register(67, "endermite"            , EntityEndermite.class           , EntityEndermite::new           , "Endermite");
        register(68, "guardian"             , EntityGuardian.class            , EntityGuardian::new            , "Guardian");
        register(69, "shulker"              , EntityShulker.class             , EntityShulker::new             , "Shulker");
        register(90, "pig"                  , EntityPig.class                 , EntityPig::new                 , "Pig");
        register(91, "sheep"                , EntitySheep.class               , EntitySheep::new               , "Sheep");
        register(92, "cow"                  , EntityCow.class                 , EntityCow::new                 , "Cow");
        register(93, "chicken"              , EntityChicken.class             , EntityChicken::new             , "Chicken");
        register(94, "squid"                , EntitySquid.class               , EntitySquid::new               , "Squid");
        register(95, "wolf"                 , EntityWolf.class                , EntityWolf::new                , "Wolf");
        register(96, "mooshroom"            , EntityMooshroom.class           , EntityMooshroom::new           , "MushroomCow");
        register(97, "snowman"              , EntitySnowman.class             , EntitySnowman::new             , "SnowMan");
        register(98, "ocelot"               , EntityOcelot.class              , EntityOcelot::new              , "Ozelot");
        register(99, "villager_golem"       , EntityIronGolem.class           , EntityIronGolem::new           , "VillagerGolem");
        register(100, "horse"               , EntityHorse.class               , EntityHorse::new               , "Horse");
        register(101, "rabbit"              , EntityRabbit.class              , EntityRabbit::new              , "Rabbit");
        register(102, "polar_bear"          , EntityPolarBear.class           , EntityPolarBear::new           , "PolarBear");
        register(103, "llama"               , EntityLlama.class               , EntityLlama::new               , "Llama");
        register(104, "llama_spit"          , EntityLlamaSpit.class           , EntityLlamaSpit::new           , "LlamaSpit");
        register(105, "parrot"              , EntityParrot.class              , EntityParrot::new              , "Parrot");
        register(120, "villager"            , EntityVillager.class            , EntityVillager::new            , "Villager");
        register(200, "ender_crystal"       , EntityEnderCrystal.class        , EntityEnderCrystal::new        , "EnderCrystal");

        addSpawnInfo("bat", 4996656, 986895);
        addSpawnInfo("blaze", 16167425, 16775294);
        addSpawnInfo("cave_spider", 803406, 11013646);
        addSpawnInfo("chicken", 10592673, 16711680);
        addSpawnInfo("cow", 4470310, 10592673);
        addSpawnInfo("creeper", 894731, 0);
        addSpawnInfo("donkey", 5457209, 8811878);
        addSpawnInfo("elder_guardian", 13552826, 7632531);
        addSpawnInfo("enderman", 1447446, 0);
        addSpawnInfo("endermite", 1447446, 7237230);
        addSpawnInfo("evocation_illager", 9804699, 1973274);
        addSpawnInfo("ghast", 16382457, 12369084);
        addSpawnInfo("guardian", 5931634, 15826224);
        addSpawnInfo("horse", 12623485, 15656192);
        addSpawnInfo("husk", 7958625, 15125652);
        addSpawnInfo("llama", 12623485, 10051392);
        addSpawnInfo("magma_cube", 3407872, 16579584);
        addSpawnInfo("mooshroom", 10489616, 12040119);
        addSpawnInfo("mule", 1769984, 5321501);
        addSpawnInfo("ocelot", 15720061, 5653556);
        addSpawnInfo("parrot", 894731, 16711680);
        addSpawnInfo("pig", 15771042, 14377823);
        addSpawnInfo("polar_bear", 15921906, 9803152);
        addSpawnInfo("rabbit", 10051392, 7555121);
        addSpawnInfo("sheep", 15198183, 16758197);
        addSpawnInfo("shulker", 9725844, 5060690);
        addSpawnInfo("silverfish", 7237230, 3158064);
        addSpawnInfo("skeleton", 12698049, 4802889);
        addSpawnInfo("skeleton_horse", 6842447, 15066584);
        addSpawnInfo("slime", 5349438, 8306542);
        addSpawnInfo("spider", 3419431, 11013646);
        addSpawnInfo("squid", 2243405, 7375001);
        addSpawnInfo("stray", 6387319, 14543594);
        addSpawnInfo("vex", 8032420, 15265265);
        addSpawnInfo("villager", 5651507, 12422002);
        addSpawnInfo("vindication_illager", 9804699, 2580065);
        addSpawnInfo("witch", 3407872, 5349438);
        addSpawnInfo("wither_skeleton", 1315860, 4672845);
        addSpawnInfo("wolf", 14144467, 13545366);
        addSpawnInfo("zombie", 44975, 7969893);
        addSpawnInfo("zombie_horse", 3232308, 9945732);
        addSpawnInfo("zombie_pigman", 15373203, 5009705);
        addSpawnInfo("zombie_villager", 5651507, 7969893);
        EntityList.EXTRA_NAMES.add(EntityList.LIGHTNING_BOLT);
    }


    private static void register(final int id, final String name, final Class<? extends Entity> clazz, Function<World, ? extends Entity> func, final String oldName) {
        SpawnEntityFactory.registerCtor(clazz, func);
        register(id, name, clazz, oldName);
    }


    private static void register(final int id, final String name, final Class<? extends Entity> clazz, final String oldName) {
//        try {
//            clazz.getConstructor(World.class);
//        }
//        catch (NoSuchMethodException var5) {
//            throw new RuntimeException("Invalid class " + clazz + " no constructor taking " + World.class.getName());
//        }
        if ((clazz.getModifiers() & 0x400) == 0x400) {
            throw new RuntimeException("Invalid abstract class " + clazz);
        }
        final ResourceLocation resourcelocation = new ResourceLocation(name);
        GameData.registerEntity(id, resourcelocation, clazz, oldName);
    }


    protected static EntityEggInfo addSpawnInfo(final String id, final int primaryColor, final int secondaryColor) {
        final ResourceLocation resourcelocation = new ResourceLocation(id);
        final EntityEggInfo egg = new EntityEggInfo(resourcelocation, primaryColor, secondaryColor);
        final EntityEntry entry = ForgeRegistries.ENTITIES.getValue(resourcelocation);
        if (entry != null) {
            entry.setEgg(egg);
        }
        return EntityList.ENTITY_EGGS.put(resourcelocation, egg);
    }


    static {
        LIGHTNING_BOLT = new ResourceLocation("lightning_bolt");
        PLAYER = new ResourceLocation("player");
        ENTITY_EGGS = Maps.newLinkedHashMap();
        EXTRA_NAMES = new Te4HashSet<>();
    }


    public static class EntityEggInfo {
        public final ResourceLocation spawnedID;
        public final int primaryColor;
        public final int secondaryColor;
        public final StatBase killEntityStat;
        public final StatBase entityKilledByStat;

        public EntityEggInfo(final ResourceLocation idIn, final int primaryColorIn, final int secondaryColorIn) {
            this.spawnedID = idIn;
            this.primaryColor = primaryColorIn;
            this.secondaryColor = secondaryColorIn;
            this.killEntityStat = StatList.getStatKillEntity(this);
            this.entityKilledByStat = StatList.getStatEntityKilledBy(this);
        }

        public EntityEggInfo(final ResourceLocation p_i47621_1_, final int p_i47621_2_, final int p_i47621_3_, final StatBase p_i47621_4_, final StatBase p_i47621_5_) {
            this.spawnedID = p_i47621_1_;
            this.primaryColor = p_i47621_2_;
            this.secondaryColor = p_i47621_3_;
            this.killEntityStat = p_i47621_4_;
            this.entityKilledByStat = p_i47621_5_;
        }
    }
}
