package plus.fserver.utls.debug;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class DebugEntity extends Entity {
    private static int ID = 0;
    private String name;

    public DebugEntity(World world){
        this("");
    }

    public DebugEntity(){
        this("");
    }


    public DebugEntity(String name) {
        super(null);
        this.name = name;
        posX = 0; posY = 0; posZ = 0;
        setAABB(0,0,0);
    }


    public DebugEntity(double x, double y, double z){
        this("ID: "+ (ID++), x, y, z);
    }


    public DebugEntity(String name, double x, double y, double z){
        super(null);
        this.name =name;
        posX = x; posY = y; posZ = z;
        setAABB(x, y, z);
    }


    public DebugEntity track(){
        if(trackerEntry == null)
            trackerEntry = new EntityTrackerEntry(this, -1, 0, 0, false);

        trackerEntry.lastTrackedEntityPosX = posX;
        trackerEntry.lastTrackedEntityPosY = posY;
        trackerEntry.lastTrackedEntityPosZ = posZ;

        return this;
    }


    public DebugEntity setPos(double x, double y, double z){
        this.posX = x; this.posY = y; this.posZ = z;
        return setAABB(x, y, z);
    }


    public DebugEntity setAABB(double x, double y, double z){
        super.setEntityBoundingBox(new AxisAlignedBB(
                x-0.5, y-0.5, z-0.5, x+0.5, y+0.5, z+0.5
        ));
        return this;
    }

    @Override
    public String toString() {
        return String.format(name+"[x: %s, y: %s, z: %s] ", posX, posY, posZ)+getEntityBoundingBox();
    }

    @Override
    protected void entityInit() {}


    @Override
    public boolean equals(Object p_equals_1_) {
        return super.equals(p_equals_1_);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {}


    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}
}
