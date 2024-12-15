package plus.fserver.utls;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.AABBList;
import plus.fserver.utls.collections.EntityList;
import static net.minecraft.util.math.MathHelper.floor;


public class ShootRTX {
    private final EntityList<Entity> list = new EntityList<>();
    private final AABBList aabbList = new AABBList();
    private final BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
    private final AxisAlignedBB aabb = new AxisAlignedBB(0,0,0,0,0,0);
    private boolean hasResult = false;


    @SideOnly(Side.CLIENT)
    public static void debugShoot(MinecraftServer server, ICommandSender sender, String[] args){
        Entity entity = (Entity) sender;

        ShootRTX rtx1 = new ShootRTX(){
            @Override
            public void onNextPos(ChunkSelector acc, double x, double y, double z) {
                acc.getWorld().spawnParticle(
                        EnumParticleTypes.FLAME, x, y, z, 50, 0d, 0d, 0d, 0.05d
                );
            }

            @Override
            public void onHitBlock(ChunkSelector acc) {
                acc.setBlockState(pos(), Blocks.NETHERRACK.getDefaultState());
            }

            @Override
            public void onHitEntities(ChunkSelector acc) {
                for (Entity entity:this.entities()){
                    entity.attackEntityFrom(DamageSource.ON_FIRE, 10);
                }
            }
        };
        rtx1.calc(entity, entity.rotationYaw, entity.rotationPitch, 1d, 64d);
    }


    public ShootRTX calc(Entity shooter, float yaw, float pitch, double pow, double maxDistance){
        double strX = shooter.posX, strY = shooter.posY + shooter.getEyeHeight() - 0.10000000149011612, strZ = shooter.posZ;

        final float sX = -MathHelper.sin(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f);
        final float sY = -MathHelper.sin(pitch * 0.017453292f);
        final float sZ =  MathHelper.cos(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f);

        return calc(shooter, shooter.world.localSelector, strX, strY, strZ,
                sX * pow, sY * pow, sZ * pow, maxDistance
        );
    }


    public ShootRTX calc(@Nullable Entity exclude, ChunkSelector acc, double startX, double startY, double startZ, double stepX, double stepY, double stepZ, double maxDistance){
        double maxSq = maxDistance * maxDistance;
        double curX = startX+stepX, curY = startY+stepY, curZ = startZ+stepZ;

        int sx = floor(startX), sy = floor(startY), sz = floor(startZ);

        Chunk chunk = acc.getLoadedE(sx>>4, sz>>4);

        if(chunk == null){
            hasResult = false;
            return this;
        }
        EntityList<Entity> list = this.list;
        AABBList aabbList = this.aabbList;
        BlockPos.MutableBlockPos mut = this.mut;
        IBlockState state = chunk.getBlockState(sx, sy, sz);

        AxisAlignedBB aabb = this.aabb; aabb.init(
                startX, startY, startZ,
                curX, curY, curZ
        );

        while (disSq(startX, startY, startZ, curX, curY, curZ) < maxSq){
            int nx = floor(curX), ny = floor(curY), nz = floor(curZ);

            if(nx != sx || ny != sy || nz != sz){
                mut.setPosB(sx = nx, sy = ny, sz = nz);

                chunk = acc.getLoadedE(nx >> 4, nz >> 4);
                if(chunk == null){
                    hasResult = false;
                    return this;
                }
                state = chunk.getBlockState(nx, ny, nz);
                onNextBlockPos(acc, mut);
            }

            if(getEntities(chunk, aabb, list, exclude)){
                hasResult = true;
                onHitEntities(acc);
                return this;
            }

            if(hasBlockCollisions(acc, state, mut, aabb, aabbList)){
                hasResult = true;
                onHitBlock(acc);
                return this;
            }
            onNextPos(acc, curX, curY, curZ);

            curX += stepX; curY += stepY; curZ += stepZ;
            aabb.offsetMut(stepX, stepY, stepZ);
        }
        hasResult = false;
        return this;
    }


    public void onNextPos(ChunkSelector acc, double x, double y, double z){}


    public void onNextBlockPos(ChunkSelector acc, BlockPos pos){}


    public void onHitBlock(ChunkSelector acc){}


    public void onHitEntities(ChunkSelector acc){}


    public BlockPos pos(){
        return mut;
    }


    public AxisAlignedBB getAABB(){
        return aabb;
    }


    public boolean hasResult(){
        return hasResult;
    }


    public EntityList<Entity> entities(){
        return list;
    }


    public ShootRTX clear(){
        list.clear();
        aabbList.clear();
        return this;
    }


    private static double disSq(double x, double y, double z, double x2, double y2, double z2){
        final double d0 = x2 - x;
        final double d2 = y2 - y;
        final double d3 = z2 - z;
        return d0 * d0 + d2 * d2 + d3 * d3;
    }


    public static boolean hasBlockCollisions(ChunkSelector acc, IBlockState state, BlockPos pos, AxisAlignedBB aabb, AABBList list){
        if(state == BlockIDIndexes.AIR_STATE)return false;
        if(state.isFullCube())return true;
        list.fclear();
        state.addCollisionBoxToListF(acc, pos, aabb, list);
        return !list.isEmptyC();
    }


    public static boolean getEntities(Chunk chunk, AxisAlignedBB aabb, EntityList<Entity> list, Entity exclude){
        list.fclear();
        chunk.getEntitiesWithinAABBForEntityLimitedC(exclude, aabb, list,null,3);
        return !list.isEmptyC();
    }
}