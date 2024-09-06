package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.collections.PointHashMap;


public abstract class NodeProcessor extends PointHashMap{
    protected final BlockPos.MutableBlockPos pooledPos = new BlockPos.MutableBlockPos();
    protected IBlockEAccess blockaccess;
    protected EntityLiving entity;
    //protected final PointHashMap pointMap = new PointHashMap();
    protected int entitySizeX;
    protected int entitySizeY;
    protected int entitySizeZ;
    protected boolean canEnterDoors;
    protected boolean canOpenDoors;
    protected boolean canSwim;

    public void init(IBlockAccess sourceIn, EntityLiving mob) {
        this.blockaccess = (IBlockEAccess) sourceIn;
        this.entity = mob;
        super.clearMap();
        this.entitySizeX = MathHelper.floor(mob.width + 1.0F);
        this.entitySizeY = MathHelper.floor(mob.height + 1.0F);
        this.entitySizeZ = MathHelper.floor(mob.width + 1.0F);
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