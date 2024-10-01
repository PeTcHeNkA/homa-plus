package net.minecraftforge.event.world;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class BlockEvent extends Event {
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("forge.debugBlockEvent", "false"));

    private World world;
    private BlockPos pos;
    private IBlockState state;

    public BlockEvent(){}

    public BlockEvent(World world, BlockPos pos, IBlockState state) {
        this.pos = pos;
        this.world = world;
        this.state = state;
    }


    public void initBlockEvent(World world, BlockPos pos, IBlockState state){
        this.pos = pos;
        this.world = world;
        this.state = state;
    }


    public World getWorld() {
        return world;
    }


    public BlockPos getPos() {
        return pos;
    }


    public IBlockState getState() {
        return state;
    }


    public static class HarvestDropsEvent extends BlockEvent {
        private final int fortuneLevel;
        private final List<ItemStack> drops;
        private final boolean isSilkTouching;
        private float dropChance; // Change to e.g. 1.0f, if you manipulate the list and want to guarantee it always drops
        private final EntityPlayer harvester; // May be null for non-player harvesting such as explosions or machines

        public HarvestDropsEvent(World world, BlockPos pos, IBlockState state, int fortuneLevel, float dropChance, List<ItemStack> drops, EntityPlayer harvester, boolean isSilkTouching)
        {
            super(world, pos, state);
            this.fortuneLevel = fortuneLevel;
            this.setDropChance(dropChance);
            this.drops = drops;
            this.isSilkTouching = isSilkTouching;
            this.harvester = harvester;
        }

        public int getFortuneLevel() { return fortuneLevel; }
        public List<ItemStack> getDrops() { return drops; }
        public boolean isSilkTouching() { return isSilkTouching; }
        public float getDropChance() { return dropChance; }
        public void setDropChance(float dropChance) { this.dropChance = dropChance; }
        public EntityPlayer getHarvester() { return harvester; }
    }


    @Cancelable
    public static class BreakEvent extends BlockEvent {
        /** Reference to the Player who broke the block. If no player is available, use a EntityFakePlayer */
        private final EntityPlayer player;
        private int exp;

        public BreakEvent(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
            super(world, pos, state);
            this.player = player;

            if (state == null || !ForgeHooks.canHarvestBlock(state.getBlock(), player, world, pos) || // Handle empty block or player unable to break block scenario
                    (state.getBlock().canSilkHarvest(world, pos, world.getBlockState(pos), player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand()) > 0)) // If the block is being silk harvested, the exp dropped is 0
            {
                this.exp = 0;
            } else {
                int bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
                this.exp = state.getBlock().getExpDrop(state, world, pos, bonusLevel);
            }
        }

        public EntityPlayer getPlayer() {
            return player;
        }


        public int getExpToDrop() {
            return this.isCanceled() ? 0 : exp;
        }


        public void setExpToDrop(int exp) {
            this.exp = exp;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }
    }


    @Cancelable
    public static class EntityPlaceEvent extends BlockEvent {
        private Entity entity;
        private BlockSnapshot blockSnapshot;
        private IBlockState placedBlock;
        private IBlockState placedAgainst;

        public EntityPlaceEvent(){
            super(null,null,null);
        }

        public void initEntityPlaceEvent(@Nonnull BlockSnapshot blockSnapshot, @Nonnull IBlockState placedAgainst, @Nullable Entity entity){
            initBlockEvent(blockSnapshot.getWorld(), blockSnapshot.getPos(), !(entity instanceof EntityPlayer) ? blockSnapshot.getReplacedBlock() : blockSnapshot.getCurrentBlock());
            setCanceled(false);

            this.entity = entity;
            this.blockSnapshot = blockSnapshot;
            this.placedBlock = !(entity instanceof EntityPlayer) ? blockSnapshot.getReplacedBlock() : blockSnapshot.getCurrentBlock();
            this.placedAgainst = placedAgainst;
        }


        public EntityPlaceEvent(@Nonnull BlockSnapshot blockSnapshot, @Nonnull IBlockState placedAgainst, @Nullable Entity entity) {
            super(blockSnapshot.getWorld(), blockSnapshot.getPos(), !(entity instanceof EntityPlayer) ? blockSnapshot.getReplacedBlock() : blockSnapshot.getCurrentBlock());
            this.entity = entity;
            this.blockSnapshot = blockSnapshot;
            this.placedBlock = !(entity instanceof EntityPlayer) ? blockSnapshot.getReplacedBlock() : blockSnapshot.getCurrentBlock();
            this.placedAgainst = placedAgainst;

            if (DEBUG) {
                System.out.printf("Created EntityPlaceEvent - [PlacedBlock: %s ][PlacedAgainst: %s ][Entity: %s ]\n", getPlacedBlock(), placedAgainst, entity);
            }
        }


        @Nullable
        public Entity getEntity() {
            return entity;
        }


        public BlockSnapshot getBlockSnapshot() {
            return blockSnapshot;
        }


        public IBlockState getPlacedBlock() {
            return placedBlock;
        }


        public IBlockState getPlacedAgainst() {
            return placedAgainst;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }
    }


    @Cancelable
    @Deprecated // Remove in 1.13
    public static class PlaceEvent extends EntityPlaceEvent {
        private final EntityPlayer player;
        private final EnumHand hand;

        public PlaceEvent(@Nonnull BlockSnapshot blockSnapshot, @Nonnull IBlockState placedAgainst, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
            super(blockSnapshot, placedAgainst, player);
            this.player = player;
            this.hand = hand;
            if (DEBUG) {
                System.out.printf("Created PlaceEvent - [PlacedBlock: %s ][PlacedAgainst: %s ][ItemStack: %s ][Player: %s ][Hand: %s]\n", getPlacedBlock(), placedAgainst, player.getHeldItem(hand), player, hand);
            }
        }


        public EntityPlayer getPlayer() {
            return player;
        }


        @Nonnull
        @Deprecated
        public ItemStack getItemInHand() {
            return player.getHeldItem(hand);
        }


        public EnumHand getHand() {
            return hand;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }
    }


    @Cancelable
    public static class EntityMultiPlaceEvent extends EntityPlaceEvent {
        private final List<BlockSnapshot> blockSnapshots;

        public EntityMultiPlaceEvent(@Nonnull List<BlockSnapshot> blockSnapshots, @Nonnull IBlockState placedAgainst, @Nullable Entity entity) {
            super(blockSnapshots.get(0), placedAgainst, entity);
            this.blockSnapshots = ImmutableList.copyOf(blockSnapshots);
            if (DEBUG) {
                System.out.printf("Created EntityMultiPlaceEvent - [PlacedAgainst: %s ][Entity: %s ]\n", placedAgainst, entity);
            }
        }


        public List<BlockSnapshot> getReplacedBlockSnapshots() {
            return blockSnapshots;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    @Cancelable
    public static class MultiPlaceEvent extends PlaceEvent {
        private final List<BlockSnapshot> blockSnapshots;

        public MultiPlaceEvent(@Nonnull List<BlockSnapshot> blockSnapshots, @Nonnull IBlockState placedAgainst, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
            super(blockSnapshots.get(0), placedAgainst, player, hand);
            this.blockSnapshots = ImmutableList.copyOf(blockSnapshots);
            if (DEBUG) {
                System.out.printf("Created MultiPlaceEvent - [PlacedAgainst: %s ][ItemInHand: %s ][Player: %s ]\n", placedAgainst, player.getHeldItem(hand), player);
            }
        }


        public List<BlockSnapshot> getReplacedBlockSnapshots() {
            return blockSnapshots;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    /**
     * Fired when a physics update occurs on a block. This event acts as
     * a way for mods to detect physics updates, in the same way a BUD switch
     * does. This event is only called on the server.
     */
    @Cancelable
    public static class NeighborNotifyEvent extends BlockEvent {
        private EnumSet<EnumFacing> notifiedSides;
        private boolean forceRedstoneUpdate;

        public NeighborNotifyEvent(World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides,
                                   boolean forceRedstoneUpdate) {
            super(world, pos, state);
            this.notifiedSides = notifiedSides;
            this.forceRedstoneUpdate = forceRedstoneUpdate;
        }

        public NeighborNotifyEvent(){
            super(null,null,null);
            notifiedSides = EnumSet.noneOf(EnumFacing.class);
        }


        public void initNeighborNotifyEvent(World world, BlockPos pos, IBlockState state, boolean forceRedstoneUpdate, EnumFacing... faces){
            notifiedSides.clear();
            initBlockEvent(world, pos, state);
            this.forceRedstoneUpdate = forceRedstoneUpdate;
        }


        public void initNeighborNotifyEvent(World world, BlockPos pos, IBlockState state, boolean forceRedstoneUpdate, EnumSet<EnumFacing> notifiedSides){
            this.notifiedSides = notifiedSides;
            initBlockEvent(world, pos, state);
            this.forceRedstoneUpdate = forceRedstoneUpdate;
            setCanceled(false);
        }


        public EnumSet<EnumFacing> getNotifiedSides() {
            return notifiedSides;
        }


        public boolean getForceRedstoneUpdate() {
            return forceRedstoneUpdate;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    @HasResult
    public static class CreateFluidSourceEvent extends BlockEvent {
        public CreateFluidSourceEvent(World world, BlockPos pos, IBlockState state) {
            super(world, pos, state);
        }

        @Override
        public boolean hasResult() {
            return true;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    @Cancelable
    public static class FluidPlaceBlockEvent extends BlockEvent {
        private final BlockPos liquidPos;
        private IBlockState newState;
        private IBlockState origState;

        public FluidPlaceBlockEvent(World world, BlockPos pos, BlockPos liquidPos, IBlockState state) {
            super(world, pos, state);
            this.liquidPos = liquidPos;
            this.newState = state;
            this.origState = world.getBlockState(pos);
        }


        public BlockPos getLiquidPos() {
            return liquidPos;
        }


        public IBlockState getNewState() {
            return newState;
        }


        public void setNewState(IBlockState state) {
            this.newState = state;
        }


        public IBlockState getOriginalState() {
            return origState;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    /**
     * Fired when a crop block grows.  See subevents.
     *
     */
    public static class CropGrowEvent extends BlockEvent {
        public CropGrowEvent(){}

        public CropGrowEvent(World world, BlockPos pos, IBlockState state) {
            super(world, pos, state);
        }

        /**
         * Fired when any "growing age" blocks (for example cacti, chorus plants, or crops
         * in vanilla) attempt to advance to the next growth age state during a random tick.<br>
         * <br>
         * {@link Result#DEFAULT} will pass on to the vanilla growth mechanics.<br>
         * {@link Result#ALLOW} will force the plant to advance a growth stage.<br>
         * {@link Result#DENY} will prevent the plant from advancing a growth stage.<br>
         * <br>
         * This event is not {@link Cancelable}.<br>
         * <br>
         */
        @HasResult
        public static class Pre extends CropGrowEvent {
            public Pre(){}

            public Pre(World world, BlockPos pos, IBlockState state) {
                super(world, pos, state);
            }

            public CropGrowEvent initCropPre(World world, BlockPos pos, IBlockState state){
                initBlockEvent(world, pos, state);
                setResult(Result.DEFAULT);
                return this;
            }


            @Override
            public boolean hasResult() {
                return true;
            }
        }


        public static class Post extends CropGrowEvent {
            private IBlockState originalState;
            public Post(){}

            public Post(World world, BlockPos pos, IBlockState original, IBlockState state) {
                super(world, pos, state);
                originalState = original;
            }


            public CropGrowEvent initCropPre(World world, BlockPos pos, IBlockState original, IBlockState state){
                initBlockEvent(world, pos, state);
                originalState = original;
                return this;
            }


            public IBlockState getOriginalState() {
                return originalState;
            }
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    /**
     * Fired when when farmland gets trampled
     * This event is {@link Cancelable}
     */
    @Cancelable
    public static class FarmlandTrampleEvent extends BlockEvent {

        private final Entity entity;
        private final float fallDistance;

        public FarmlandTrampleEvent(World world, BlockPos pos, IBlockState state, float fallDistance, Entity entity) {
            super(world, pos, state);
            this.entity = entity;
            this.fallDistance = fallDistance;
        }


        public Entity getEntity() {
            return entity;
        }


        public float getFallDistance() {
            return fallDistance;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    @Cancelable
    public static class PortalSpawnEvent extends BlockEvent {
        private final BlockPortal.Size size;

        public PortalSpawnEvent(World world, BlockPos pos, IBlockState state, BlockPortal.Size size) {
            super(world, pos, state);
            this.size = size;
        }


        @Override
        public boolean isCancelable() {
            return true;
        }


        public BlockPortal.Size getPortalSize() {
            return size;
        }


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    //Elca-Forge event system
    //Start generated code
    private static ListenerList LISTENER_LIST;
    protected void setup() {
        super.setup();
        if (LISTENER_LIST == null)
            LISTENER_LIST = new ListenerList(super.getListenerList());
    }
    public ListenerList getListenerList() {
        return LISTENER_LIST;
    }
    //End generated code
}