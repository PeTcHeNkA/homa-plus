package plus.gen.legacy_templates;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import plus.gen.Te4WorldGen;
import plus.gen.settings.SettingsFolder;
import java.util.Random;


public class SmartTemplate extends McTemplate{
    public SmartTemplate(NBTTagCompound nbt) {
        super(nbt);
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settings, Random rand) {
        Te4WorldGen.buffer.put(() -> addBlocksToWorld(
                world,
                x,y,z,
                settings, rand
        ));
    }


    public void addBlocksToWorld(World worldIn, int x, int y, int z,
                                 SettingsFolder settingsf, Random r) {
        PlacementSettings settings = settingsf.get(r);
        if (this.blocks.length>0) {
            Block replacedBlock = settings.getReplacedBlock();
            boolean needReplace = replacedBlock==null;

            Rotation rotation = settings.getRotation();
            Mirror mirror = settings.getMirror();

            for (VTemplate.BlockInfo blockInfo : this.blocks) {
                BlockPos blockpos = transformedBlockPos(
                        blockInfo.x, blockInfo.y,blockInfo.z, mirror, rotation, x,y,z);

                Block block = blockInfo.blockState.getBlock();

                if (needReplace || replacedBlock != block) {
                    if(block != Blocks.STRUCTURE_BLOCK){
                        int by = blockpos.getY();
                        if(by<255 && by>0){
                            IBlockState state = blockInfo.blockState.withMirror(mirror).withRotation(rotation);
                            onPlace(worldIn, blockpos, blockInfo, state, y, by);
                            if(blockInfo.tileentitydata != null){
                                TileEntity tileEntity = worldIn.getTileEntity(blockpos);
                                if (tileEntity != null) {
                                    blockInfo.tileentitydata.setInteger("x", blockpos.getX());
                                    blockInfo.tileentitydata.setInteger("y", by);
                                    blockInfo.tileentitydata.setInteger("z", blockpos.getZ());
                                    tileEntity.readFromNBT(blockInfo.tileentitydata);
                                    tileEntity.mirror(mirror);
                                    tileEntity.rotate(rotation);
                                }
                            }
                        }
                    }
                    if (blockInfo.tileentitydata != null) {
                        TileEntity tileEntity = worldIn.getTileEntity(blockpos);

                        if (tileEntity != null) {
                            tileEntity.markDirty();
                        }
                    }
                }
            }
        }


        if (!settings.getIgnoreEntities() && !this.entities.isEmpty()) {
            this.addEntitiesToWorld(worldIn, x,y,z, settings.getMirror(),
                    settings.getRotation(), null);
        }
    }

    protected void onPlace(World world, BlockPos blockpos, VTemplate.BlockInfo blockInfo,
                           IBlockState state, int y, int curY){
        Chunk ch = world.getChunk(blockpos);
        ch.setBlockState(blockpos, state);
        if(curY == y){
            int bx = blockpos.getX();
            int bz = blockpos.getZ();
            --curY;
            while (curY>0 && mayBeReplaced(ch.getBlockState(bx,curY,bz).getBlock())){
                BlockPos pos = new BlockPos(bx,curY,bz);
                ch.setBlockState(pos,state);
                --curY;
            }
        }
    }


    protected boolean mayBeReplaced(Block b){
        return b == Blocks.AIR || b == Blocks.GRASS || b == Blocks.TALLGRASS ||
                b == Blocks.SAND || b == Blocks.DIRT || b == Blocks.GRASS_PATH;
    }
}