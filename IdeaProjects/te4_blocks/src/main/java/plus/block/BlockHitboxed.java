package plus.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import plus.fserver.block.BlockFs;
import plus.fserver.block.BlockFx;
import plus.fserver.utls.collections.ArrayListE6;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.utl.HitBoxBuilder;
import plus.utl.NumFolder;
import plus.utl.NumX1intOrX32bool;
import java.util.List;


public class BlockHitboxed extends Block {

    private final AxisAlignedBB[] hitbox;

    public BlockHitboxed(String material, String id, String name,
                         boolean isOpaqueCube, HitBoxBuilder[] hitbox) {
        super(material, id, name, isOpaqueCube);
        this.hitbox = new AxisAlignedBB[hitbox.length];
        insertBoxes(hitbox);
        setCreativeTab(TE4BLOCKS_TAB);
    }


    public BlockHitboxed(TsonMap map){
        super(map);
        HitBoxBuilder[] hitBoxBuilder = null;
        TsonObj obj = map.get("hitbox");
        if(obj.isString()){
            hitBoxBuilder = new HitBoxBuilder[1];
            hitBoxBuilder[0] = BlockList.parse(obj.getStr());
        } else if(obj.isList()){
            hitBoxBuilder = new HitBoxBuilder[obj.getList().size()];
            int i = 0;
            for (TsonObj obj2:obj.getList()){
                hitBoxBuilder[i] = BlockList.parse(obj2.getStr());
                ++i;
            }
        }
        this.hitbox = new AxisAlignedBB[hitBoxBuilder.length];
        insertBoxes(hitBoxBuilder);
        setCreativeTab(TE4BLOCKS_TAB);
    }


    @Override
    public TsonMap toTson() {
        TsonMap map = super.toTson();
        TsonList list = map.addList("hitbox");
        for(AxisAlignedBB box:hitbox) {
            list.add(HitBoxBuilder.convertMcHitboxToString(box));
        }
        return map;
    }


    private void insertBoxes(HitBoxBuilder[] hitbox){
        for(int i=0;i<hitbox.length;i++){
            this.hitbox[i] = hitbox[i].toHitbox();
        }
    }


    @Override
    public boolean isFullCube(IBlockState p_isFullCube_1_) {
        return false;
    }


    @Override
    public void addCollisionBoxToList(
            IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
        if(hitbox.length==1){
            super.addCollisionBoxToList(
                    state, worldIn, pos, entityBox, collidingBoxes, entityIn,isActualState
            );
        } else {
            for(AxisAlignedBB box:hitbox){
                addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
            }
        }
    }


    public void addCollisionBoxToListF(IBlockState state, IBlockAccess access, BlockPos pos, AxisAlignedBB entityBox, ArrayListE6<AxisAlignedBB> list) {
        for(AxisAlignedBB box:hitbox)
            BlockFs.addCollisionBoxToListFs(pos, entityBox, list, box);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state,
                                        IBlockAccess access,
                                        BlockPos pos) {
        return hitbox[0];
    }


    @Override
    public BlockFaceShape getBlockFaceShape(
            IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing facing) {
        return BlockFaceShape.UNDEFINED;
    }


    public static abstract class Parametrized extends BlockHitboxed {
        public Parametrized(String material, String id, String name, boolean isOpaqueCube, HitBoxBuilder[] hitbox) {
            super(material, id, name, isOpaqueCube, hitbox);
        }

        abstract IProperty[] iProperties();


        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, iProperties());
        }


        @Override
        public int getMetaFromState(IBlockState state) {
            NumFolder tempFolder = new NumX1intOrX32bool();
            int i = 0;
            for(IProperty p:iProperties()){
                Comparable value = state.getValue(p);
                tempFolder.setBool(i, (boolean) value);
                i++;
            }
            return tempFolder.toInt();
        }


        @Override
        public IBlockState getStateFromMeta(int meta) {
            IBlockState state = this.getDefaultState();
            NumFolder tempFolder = new NumX1intOrX32bool(meta);
            int i = 0;
            for(IProperty p:iProperties()){
                state = state.withProperty(p, tempFolder.getBool(i));
                i++;
            }
            return state;
        }
    }
}