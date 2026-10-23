package plus.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.SpMain;
import plus.asyutl.buf.FastBuffer;
import plus.common.mashine.block.SpMehModule;
import plus.common.mashine.inv.IEnergyDependent;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IPowerContainer;
import plus.common.mashine.inv.InvTileEntity;
import plus.common.mashine.block.scan.BlockScanner;
import plus.common.mashine.block.scan.BlockWay;

import java.util.ArrayList;


public abstract class SpBasicTE extends BlockContainer{
    public static final BlockScanner SCANNER = new BlockScanner() {
        @Override
        protected void onSuccess(World world, BlockPos pos, BlockWay.BlockWayResults results) {
            FastBuffer<IEnergyDependent> machines = new FastBuffer<>();
            ArrayList<IEnergyFolder> generators = new ArrayList<>(5);
            ArrayList<IEnergyFolder> cells = new ArrayList<>(5);

            for(BlockPos pos0:results){
                TileEntity entity = world.getTileEntity(pos0);
                if (entity instanceof IPowerContainer) {
                    ((IPowerContainer) entity).setPower(results.getStructSize());
                }
                if(entity instanceof IEnergyFolder){
                    IEnergyFolder e0 = (IEnergyFolder) entity;
                    if(e0 instanceof IEnergyDependent){
                        cells.add(e0);
                    } else {
                        generators.add(e0);
                    }
                } else {
                    if(entity instanceof IEnergyDependent){
                        machines.put((IEnergyDependent) entity);
                    }
                }
            }


            IEnergyFolder powerNet;
            if(generators.size()>0) {
                powerNet = new IEnergyFolder.Combined(generators);
            } else {
                powerNet = IEnergyFolder.EMPTY;
            }

            if(cells.size()!=0){
                for(IEnergyFolder f:cells){
                    IEnergyDependent ed = (IEnergyDependent) f;
                    ed.setEnergyFolder(powerNet);
                }
                powerNet = new IEnergyFolder.Combined(cells);
            }

            while (machines.hasNext()){
                machines.flush().setEnergyFolder(powerNet);
            }
        }


        @Override
        protected boolean isResultBlock(Block block) {
            return block instanceof SpBasicTE;
        }


        @Override
        protected boolean isProvideBlock(Block block) {
            return block == SpMehModule.SP_MEH_MODULE;
        }
    };


    protected SpBasicTE(String regName) {
        super(Material.IRON);
        setRegistryName(regName);
        setTranslationKey(regName);
        setCreativeTab(SpMain.SP_TAB);
        setSoundType(SoundType.METAL);
        SpMain.proxy.addToRegObj(this);
    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            SCANNER.doScan(world, pos);
        }
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof InvTileEntity) {
                NonNullList<ItemStack> items = ((InvTileEntity) tileentity).stacks;
                for(int i=0;i<items.size()-1;i++){
                    spawnAsEntity(world, pos, items.get(i));
                }
            }
            world.removeTileEntity(pos);
            scanOnBreak(world, pos);
        }
    }


    public static void scanOnBreak(World world, BlockPos pos){
        BlockWay way = SCANNER.scan(new BlockWay(), world, pos.up());
        way = SCANNER.joinScan(way, world, pos, pos.down());
        way = SCANNER.joinScan(way, world, pos, pos.east());
        way = SCANNER.joinScan(way, world, pos, pos.west());
        way = SCANNER.joinScan(way, world, pos, pos.north());
        way = SCANNER.joinScan(way, world, pos, pos.south());
        SCANNER.completeScan(way, world, pos);
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof IInventory) {
                playerIn.displayGUIChest((IInventory) tileentity);
            }
        }
        return true;
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}