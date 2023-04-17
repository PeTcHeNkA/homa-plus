package plus.common.spship.entity;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.mem.Memory;
import plus.common.network.spship.SpModePacket;
import plus.common.network.entity.EnableSpFromEntity;
import plus.common.player.PlayerSpship;
import javax.annotation.Nullable;


public class OffSpShip extends LivingBlock{
    protected int modelId = -1;

    public OffSpShip(World worldIn) {
        super(worldIn);
    }


    @Override
    protected void setRotation(float yaw, float pitch) {
        super.setRotation(yaw, pitch);
        if(world.isRemote){
            mem.prevXZrot = 180-yaw;
        }
    }


    @SideOnly(Side.CLIENT)
    protected Memory buildDef(){
        return new NedMem();
    }


    public OffSpShip(EntityPlayerMP pl) {
        super(pl.world);
        setParent(pl.getEntityId());
        PlayerSpship sp = ((PlayerSpship) pl);
        modelId = sp.getSpModelId();

        setPositionAndRotation(
                pl.posX, pl.posY, pl.posZ,
                pl.rotationYaw, pl.rotationPitch
        );
        sp.setSpMode(false);
        new SpModePacket(false).sendTo(pl);
    }


    private int parent = -1;
    public void setParent(int parent){
        this.parent = parent;
    }

    public int getParent(){
        return parent;
    }


    public boolean canBeCollidedWith() {
        return !this.isDead;
    }


    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return getEntityBoundingBox();
    }


    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand){
        if(parent == player.getEntityId()){
            PlayerSpship sp = ((PlayerSpship) player);
            if(sp.isSpshipMode())return EnumActionResult.PASS;

            this.setDead();
            sp.setSpMode(true);
            new EnableSpFromEntity(getEntityId(), parent).sendTo(player);

            player.setPosition(posX+0.7, posY+1.6, posZ+0.7);

            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }


    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return true;
    }


    @Override
    public int getModelId() {
        return modelId;
    }

    @Override
    protected void entityInit() {
        setSize(2,2);
        //dataManager.register(PARENT_ID, -1);
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("d",
                this.newFloatNBTList(
                        (float) this.prevPosX,
                        (float) this.prevPosZ
                )
        );
        compound.setShort("m", (short) modelId);
        compound.setInteger("p", parent);
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        if(world.isRemote){
            setDead();
            return;
        }
        NBTTagList list = compound.getTagList("d", 5);
        this.prevPosX = list.getFloatAt(0);
        this.prevPosZ = list.getFloatAt(1);

        modelId = compound.getShort("m");
        parent = compound.getInteger("p");
    }
}