package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.block.BlockFs;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;


public class BlockBeacon extends BlockContainer {
    public BlockBeacon() {
        super(Material.GLASS, MapColor.DIAMOND);
        this.setHardness(3.0F);
        this.setCreativeTab(CreativeTabs.MISC);
    }


    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityBeacon();
    }


    @Override
    public boolean onBlockActivated(World p_180639_1_, BlockPos p_180639_2_, IBlockState p_180639_3_, EntityPlayer p_180639_4_, EnumHand p_180639_5_, EnumFacing p_180639_6_, float p_180639_7_, float p_180639_8_, float p_180639_9_) {
        if (p_180639_1_.isRemote) {
            return true;
        } else {
            TileEntity lvt_10_1_ = p_180639_1_.getTileEntity(p_180639_2_);
            if (lvt_10_1_ instanceof TileEntityBeacon) {
                p_180639_4_.displayGUIChest((TileEntityBeacon)lvt_10_1_);
                p_180639_4_.addStat(StatList.BEACON_INTERACTION);
            }

            return true;
        }
    }

    @Override
    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        BlockFs.addCollisionBoxToListFs(pos, entityBox, list, state.getCollisionBoundingBox(access, pos));
    }


    @Override
    public boolean isOpaqueCube(IBlockState p_149662_1_) {
        return false;
    }


    @Override
    public boolean isFullCube(IBlockState p_149686_1_) {
        return false;
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    public void onBlockPlacedBy(World p_180633_1_, BlockPos p_180633_2_, IBlockState p_180633_3_, EntityLivingBase p_180633_4_, ItemStack p_180633_5_) {
        super.onBlockPlacedBy(p_180633_1_, p_180633_2_, p_180633_3_, p_180633_4_, p_180633_5_);
        if (p_180633_5_.hasDisplayName()) {
            TileEntity lvt_6_1_ = p_180633_1_.getTileEntity(p_180633_2_);
            if (lvt_6_1_ instanceof TileEntityBeacon) {
                ((TileEntityBeacon)lvt_6_1_).setName(p_180633_5_.getDisplayName());
            }
        }

    }

    public void neighborChanged(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_, BlockPos p_189540_5_) {
        TileEntity lvt_6_1_ = p_189540_2_.getTileEntity(p_189540_3_);
        if (lvt_6_1_ instanceof TileEntityBeacon) {
            ((TileEntityBeacon)lvt_6_1_).updateBeacon();
            p_189540_2_.addBlockEvent(p_189540_3_, this, 1, 0);
        }

    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    public static void updateColorAsync(final World world, final BlockPos pos) {
        Runnable task = () -> {
            Chunk chunk = world.getChunk(pos);

            final int posX = pos.getX(), posZ = pos.getZ();

            for (int posY = pos.getY() - 1; posY >= 0; --posY) {
                if (!chunk.canSeeSky(posX, posY, posZ)) break;

                IBlockState state = chunk.getBlockState(posX, posY, posZ);
                if (state.getBlock() == Blocks.BEACON) {
                    final BlockPos result = new BlockPos(posX, posY, posZ);
                    ((WorldServer) world).addSyncTask(() -> {
                        TileEntity tile = world.getTileEntity(result);
                        if (tile instanceof TileEntityBeacon) {
                            ((TileEntityBeacon) tile).updateBeacon();
                            world.addBlockEvent(result, Blocks.BEACON, 1, 0);
                        }
                    });
                }
            }

        };
        if(world.isRemote){
            HttpUtil.DOWNLOADER_EXECUTOR.submit(task);
        } else {
            world.getMinecraftServer().ticker().runAsync(task);
        }
    }
}
