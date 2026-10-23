package plus.block;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.utl.HitBoxBuilder;


public class BlockDirectionalHitboxed extends BlockDirectional {
    private final AxisAlignedBB H_0;
    private final AxisAlignedBB H_90;
    private final AxisAlignedBB H_180;
    private final AxisAlignedBB H_270;
    private final AxisAlignedBB H_UP;
    private final AxisAlignedBB H_DN;

    public BlockDirectionalHitboxed(String material, String id, String name,
                                    boolean isOpaqueCube, HitBoxBuilder[] hitbox) {
        super(material, id, name, isOpaqueCube);
        AxisAlignedBB[] fboxes = parse(hitbox);
        H_0 = fboxes[0];
        H_90 = fboxes[1];
        H_180 = fboxes[2];
        H_270 = fboxes[3];
        H_UP = fboxes[4];
        H_DN = fboxes[5];
    }


    public BlockDirectionalHitboxed(TsonMap map){
        super(map);
        TsonObj obj = map.get("hitbox");
        HitBoxBuilder[] hitBoxBuilder = null;
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
        AxisAlignedBB[] fboxes = parse(hitBoxBuilder);
        H_0 = fboxes[0];
        H_90 = fboxes[1];
        H_180 = fboxes[2];
        H_270 = fboxes[3];
        H_UP = fboxes[4];
        H_DN = fboxes[5];
    }


    @Override
    public TsonMap toTson() {
        TsonMap map = super.toTson();
        TsonList list = map.addList("hitbox");
        list.add(HitBoxBuilder.convertMcHitboxToString(H_0));
        list.add(HitBoxBuilder.convertMcHitboxToString(H_90));
        list.add(HitBoxBuilder.convertMcHitboxToString(H_180));
        list.add(HitBoxBuilder.convertMcHitboxToString(H_270));
        list.add(HitBoxBuilder.convertMcHitboxToString(H_UP));
        list.add(HitBoxBuilder.convertMcHitboxToString(H_DN));
        return map;
    }


    protected static AxisAlignedBB[] parse(HitBoxBuilder[] hitbox){
        AxisAlignedBB H_0l;
        AxisAlignedBB H_90l;
        AxisAlignedBB H_180l;
        AxisAlignedBB H_270l;
        AxisAlignedBB H_UPl;
        AxisAlignedBB H_DNl;
        if (hitbox.length == 3 || hitbox.length == 1) {
            HitBoxBuilder hb = hitbox[0];
            H_0l = hb.toHitbox();
            H_90l = hb.rotate90().toHitbox();
            H_180l = hb.rotate90().toHitbox();
            H_270l = hb.rotate90().toHitbox();
            if (hitbox.length == 3) {
                H_UPl = hitbox[1].toHitbox();
                H_DNl = hitbox[2].toHitbox();
            } else {
                H_UPl = hb.rotate90().toHitbox();
                H_DNl = hb.invertY().toHitbox();
            }
        } else {
            H_0l = hitbox[0].toHitbox();
            H_90l = hitbox[1].toHitbox();
            H_180l = hitbox[2].toHitbox();
            H_270l = hitbox[3].toHitbox();
            if (hitbox.length > 4) {
                H_UPl = hitbox[4].toHitbox();
                if (hitbox.length > 5) {
                    H_DNl = hitbox[5].toHitbox();
                } else {
                    H_DNl = H_UPl;
                }
            } else {
                H_UPl = H_0l;
                H_DNl = H_180l;
            }
        }
        return new AxisAlignedBB[]{H_0l,H_90l,H_180l,H_270l,H_UPl,H_DNl};
    }


    @Override
    public boolean isFullCube(IBlockState p_isFullCube_1_) {
        return false;
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state,
                                        IBlockAccess access,
                                        BlockPos pos) {
        switch (state.getValue(FACING)) {
            default:
                return H_UP;
            case DOWN:
                return H_DN;
            case NORTH:
                return H_0;
            case EAST:
                return H_90;
            case SOUTH:
                return H_180;
            case WEST:
                return H_270;
        }
    }


    @Override
    public BlockFaceShape getBlockFaceShape(
            IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing facing) {
        return BlockFaceShape.UNDEFINED;
    }
}
