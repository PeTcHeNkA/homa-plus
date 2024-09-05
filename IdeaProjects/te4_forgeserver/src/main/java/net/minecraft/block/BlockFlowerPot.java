package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.Keep;
import plus.fserver.block.BlockFs;
import plus.fserver.utls.collections.AABBList;


public class BlockFlowerPot extends BlockContainer {
    @Keep
    public static final PropertyInteger LEGACY_DATA = PropertyInteger.create("legacy_data", 0, 15);
    @Keep
    public static final PropertyEnum<BlockFlowerPot.EnumFlowerType> CONTENTS = PropertyEnum.create("contents", BlockFlowerPot.EnumFlowerType.class);
    @Keep
    protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    public BlockFlowerPot() {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CONTENTS, BlockFlowerPot.EnumFlowerType.EMPTY).withProperty(LEGACY_DATA, 0));
    }


    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, AABBList list) {
        BlockFs.addCollisionBoxToListFx(pos, entityBox, list, getCollisionBoundingBox(state, access, pos));
    }


    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal("item.flowerPot.name");
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FLOWER_POT_AABB;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = playerIn.getHeldItem(hand);
        TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);

        if (tileentityflowerpot == null) {
            return false;
        } else {
            ItemStack itemstack1 = tileentityflowerpot.getFlowerItemStack();

            if (itemstack1.isEmpty()) {
                if (!this.canBePotted(itemstack)) {
                    return false;
                }

                tileentityflowerpot.setItemStack(itemstack);
                playerIn.addStat(StatList.FLOWER_POTTED);

                if (!playerIn.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
            } else {
                if (itemstack.isEmpty()) {
                    playerIn.setHeldItem(hand, itemstack1);
                } else if (!playerIn.addItemStackToInventory(itemstack1)) {
                    playerIn.dropItem(itemstack1, false);
                }

                tileentityflowerpot.setItemStack(ItemStack.EMPTY);
            }
            tileentityflowerpot.markDirty();
            worldIn.notifyBlockUpdate(pos, state, state, 3);
            return true;
        }
    }


    private boolean canBePotted(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());

        if (block != Blocks.YELLOW_FLOWER && block != Blocks.RED_FLOWER && block != Blocks.CACTUS && block != Blocks.BROWN_MUSHROOM &&
                block != Blocks.RED_MUSHROOM && block != Blocks.SAPLING && block != Blocks.DEADBUSH) {
            int i = stack.getMetadata();
            return block == Blocks.TALLGRASS && i == BlockTallGrass.EnumType.FERN.getMeta();
        } else
            return true;
    }


    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityFlowerPot pot = this.getTileEntity(worldIn, pos);

        if (pot != null) {
            ItemStack itemstack = pot.getFlowerItemStack();
            if (!itemstack.isEmpty()) return itemstack;
        }
        return new ItemStack(Items.FLOWER_POT);
    }


    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState downState = worldIn.getBlockState(pos.down());
        return super.canPlaceBlockAt(worldIn, pos) && (downState.isTopSolid() || downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID);
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        IBlockState downState = worldIn.getBlockState(pos.down());
        if (!downState.isTopSolid() && downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) != BlockFaceShape.SOLID) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }


    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
    }


    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        super.onBlockHarvested(worldIn, pos, state, player);

        if (player.capabilities.isCreativeMode) {
            TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);

            if (tileentityflowerpot != null) {
                tileentityflowerpot.setItemStack(ItemStack.EMPTY);
            }
        }
    }


    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.FLOWER_POT;
    }


    @Nullable
    private TileEntityFlowerPot getTileEntity(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)tileentity : null;
    }


    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return createNew(meta);
    }


    private TileEntity createNew(int meta){
        switch (meta) {
            default:
                return newTi(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY.getMeta());
            case 2:
                return newTi(Blocks.YELLOW_FLOWER, 0);
            case 3:
                return newTi(Blocks.SAPLING, BlockPlanks.EnumType.OAK.getMetadata());
            case 4:
                return newTi(Blocks.SAPLING, BlockPlanks.EnumType.SPRUCE.getMetadata());
            case 5:
                return newTi(Blocks.SAPLING, BlockPlanks.EnumType.BIRCH.getMetadata());
            case 6:
                return newTi(Blocks.SAPLING, BlockPlanks.EnumType.JUNGLE.getMetadata());
            case 7:
                return newTi(Blocks.RED_MUSHROOM, 0);
            case 8:
                return newTi(Blocks.BROWN_MUSHROOM, 0);
            case 9:
                return newTi(Blocks.CACTUS, 0);
            case 10:
                return newTi(Blocks.DEADBUSH, 0);
            case 11:
                return newTi(Blocks.TALLGRASS, BlockTallGrass.EnumType.FERN.getMeta());
            case 12:
                return newTi(Blocks.SAPLING, BlockPlanks.EnumType.ACACIA.getMetadata());
            case 13:
                return newTi(Blocks.SAPLING, BlockPlanks.EnumType.DARK_OAK.getMetadata());
        }
    }


    private static TileEntity newTi(Block block, int i){
        return new TileEntityFlowerPot(Item.getItemFromBlock(block), i);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONTENTS, LEGACY_DATA);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEGACY_DATA);
    }


    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        BlockFlowerPot.EnumFlowerType type = BlockFlowerPot.EnumFlowerType.EMPTY;
        TileEntity tileentity = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityFlowerPot) {
            TileEntityFlowerPot tileentityflowerpot = (TileEntityFlowerPot)tileentity;
            Item item = tileentityflowerpot.getFlowerPotItem();

            if (item instanceof ItemBlock) {
                int i = tileentityflowerpot.getFlowerPotData();
                Block block = Block.getBlockFromItem(item);

                if (block == Blocks.SAPLING) {
                    switch (BlockPlanks.EnumType.byMetadata(i)) {
                        case OAK:
                            type = BlockFlowerPot.EnumFlowerType.OAK_SAPLING;
                            break;
                        case SPRUCE:
                            type = BlockFlowerPot.EnumFlowerType.SPRUCE_SAPLING;
                            break;
                        case BIRCH:
                            type = BlockFlowerPot.EnumFlowerType.BIRCH_SAPLING;
                            break;
                        case JUNGLE:
                            type = BlockFlowerPot.EnumFlowerType.JUNGLE_SAPLING;
                            break;
                        case ACACIA:
                            type = BlockFlowerPot.EnumFlowerType.ACACIA_SAPLING;
                            break;
                        case DARK_OAK:
                            type = BlockFlowerPot.EnumFlowerType.DARK_OAK_SAPLING;
                            break;
                        default:
                            type = BlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                }
                else if (block == Blocks.TALLGRASS) {
                    switch (i) {
                        case 0:
                            type = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                            break;
                        case 2:
                            type = BlockFlowerPot.EnumFlowerType.FERN;
                            break;
                        default:
                            type = BlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                }
                else if (block == Blocks.YELLOW_FLOWER) {
                    type = BlockFlowerPot.EnumFlowerType.DANDELION;
                } else if (block == Blocks.RED_FLOWER) {
                    switch (BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, i)) {
                        case POPPY:
                            type = BlockFlowerPot.EnumFlowerType.POPPY;
                            break;
                        case BLUE_ORCHID:
                            type = BlockFlowerPot.EnumFlowerType.BLUE_ORCHID;
                            break;
                        case ALLIUM:
                            type = BlockFlowerPot.EnumFlowerType.ALLIUM;
                            break;
                        case HOUSTONIA:
                            type = BlockFlowerPot.EnumFlowerType.HOUSTONIA;
                            break;
                        case RED_TULIP:
                            type = BlockFlowerPot.EnumFlowerType.RED_TULIP;
                            break;
                        case ORANGE_TULIP:
                            type = BlockFlowerPot.EnumFlowerType.ORANGE_TULIP;
                            break;
                        case WHITE_TULIP:
                            type = BlockFlowerPot.EnumFlowerType.WHITE_TULIP;
                            break;
                        case PINK_TULIP:
                            type = BlockFlowerPot.EnumFlowerType.PINK_TULIP;
                            break;
                        case OXEYE_DAISY:
                            type = BlockFlowerPot.EnumFlowerType.OXEYE_DAISY;
                            break;
                        default:
                            type = BlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                }
                else if (block == Blocks.RED_MUSHROOM) {
                    type = BlockFlowerPot.EnumFlowerType.MUSHROOM_RED;
                }
                else if (block == Blocks.BROWN_MUSHROOM) {
                    type = BlockFlowerPot.EnumFlowerType.MUSHROOM_BROWN;
                }
                else if (block == Blocks.DEADBUSH) {
                    type = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                }
                else if (block == Blocks.CACTUS) {
                    type = BlockFlowerPot.EnumFlowerType.CACTUS;
                }
            }
        }
        return state.withProperty(CONTENTS, type);
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }


    /*============================FORGE START=====================================*/
    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        TileEntityFlowerPot te = world.getTileEntity(pos) instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)world.getTileEntity(pos) : null;
        if (te != null && te.getFlowerPotItem() != null)
            drops.add(new ItemStack(te.getFlowerPotItem(), 1, te.getFlowerPotData()));
    }


    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }


    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

    /*===========================FORGE END==========================================*/

    @Keep
    public static enum EnumFlowerType implements IStringSerializable {
        EMPTY("empty"),
        POPPY("rose"),
        BLUE_ORCHID("blue_orchid"),
        ALLIUM("allium"),
        HOUSTONIA("houstonia"),
        RED_TULIP("red_tulip"),
        ORANGE_TULIP("orange_tulip"),
        WHITE_TULIP("white_tulip"),
        PINK_TULIP("pink_tulip"),
        OXEYE_DAISY("oxeye_daisy"),
        DANDELION("dandelion"),
        OAK_SAPLING("oak_sapling"),
        SPRUCE_SAPLING("spruce_sapling"),
        BIRCH_SAPLING("birch_sapling"),
        JUNGLE_SAPLING("jungle_sapling"),
        ACACIA_SAPLING("acacia_sapling"),
        DARK_OAK_SAPLING("dark_oak_sapling"),
        MUSHROOM_RED("mushroom_red"),
        MUSHROOM_BROWN("mushroom_brown"),
        DEAD_BUSH("dead_bush"),
        FERN("fern"),
        CACTUS("cactus");

        private final String name;

        EnumFlowerType(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return this.name;
        }


        @Override
        public String getName() {
            return this.name;
        }
    }
}
