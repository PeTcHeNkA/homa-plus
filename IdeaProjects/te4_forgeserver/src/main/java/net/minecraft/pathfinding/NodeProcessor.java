package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.collections.PointHashMap;


public abstract class NodeProcessor extends PointHashMap{
    protected final BlockPos.MutableBlockPos pooledPos;
    protected IBlockEAccess blockaccess;
    protected EntityLiving entity;
    protected byte entitySizeX;
    protected byte entitySizeY;
    protected byte entitySizeZ;
    protected boolean canEnterDoors;
    protected boolean canOpenDoors;
    protected boolean canSwim;

    public NodeProcessor(){
        pooledPos = new BlockPos.MutableBlockPos();
    }


    public NodeProcessor(IBlockAccess sourceIn){
        World world = sourceIn.getWorld();
        if(world != null && !world.isRemote){
            pooledPos = ((WorldServer)world).navPooledPos;
        } else {
            pooledPos = new BlockPos.MutableBlockPos();
        }
    }


    public void init(IBlockAccess sourceIn, EntityLiving mob) {
        this.blockaccess = (IBlockEAccess) sourceIn;
        this.entity = mob;
        super.clearMap();
        this.entitySizeX = (byte)MathHelper.floor(mob.width  + 0.95F);
        this.entitySizeY = (byte)MathHelper.floor(mob.height + 0.95F);
        this.entitySizeZ = (byte)MathHelper.floor(mob.width  + 0.95F);
    }


    public void postProcess() {
        this.blockaccess = null;
        this.entity = null;
    }


    protected PathPoint openPoint(int x, int y, int z) {
        int hash;
        PathPoint point;
        if ((point = super.lookup(
                hash = PathPoint.makeHash(x, y, z)
        )) == null) {
            if((point = super.tryGetValueOnInsert()) == null)
                point = new PathPoint(x, y, z, hash);
            else
                point.init(x, y, z, hash);

            super.addKey(hash, point);
        }
        return point;
    }


    public abstract PathPoint getStart();


    public abstract PathPoint getPathPointToCoords(double x, double y, double z);


    public abstract int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance);


    public abstract PathNodeType getPathNodeType(IBlockEAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn);


    public abstract PathNodeType getPathNodeType(IBlockEAccess blockaccessIn, int x, int y, int z);


    public void setCanEnterDoors(boolean canEnterDoorsIn) {
        this.canEnterDoors = canEnterDoorsIn;
    }


    public void setCanOpenDoors(boolean canOpenDoorsIn) {
        this.canOpenDoors = canOpenDoorsIn;
    }


    public void setCanSwim(boolean canSwimIn) {
        this.canSwim = canSwimIn;
    }


    public boolean getCanEnterDoors() {
        return this.canEnterDoors;
    }


    public boolean getCanOpenDoors() {
        return this.canOpenDoors;
    }


    public boolean getCanSwim() {
        return this.canSwim;
    }
}