package plus.common.amm;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.entity.SpModelManager;
import plus.common.player.PlayerSpship;
import plus.common.spship.SpShip;
import plus.common.spship.entity.LivingBlock;
import plus.common.spship.modules.SpGun;
import plus.utls.MathUtils;

import java.util.List;


public class SpBullet extends LivingBlock {
    private static final DataParameter<Integer> SPSHIP_ID =
            EntityDataManager.createKey(SpBullet.class, DataSerializers.VARINT);

    protected EntityLivingBase shooter;
    protected float physDmg, enrgDmg;
    private final int mark;

    public SpBullet(World worldIn) {
        super(worldIn);
        this.setSize(1.0F, 1.0F);
        mark = 0;
    }

    public SpBullet(World worldIn, int modelId, float physDmg, float enrgDmg, int mark) {
        super(worldIn);
        setModelId(modelId);
        this.physDmg = physDmg;
        this.enrgDmg = enrgDmg;
        this.mark = mark;
    }


    public int getModelId() {
        return dataManager.get(SPSHIP_ID);
    }


    public void setModelId(int val){
        dataManager.set(SPSHIP_ID, val);
    }


    private static final float boost = 0.5f;
    public SpBullet shoot(SpShip spShip, EntityLivingBase shooter, float yaw, float pith, float power, float inaccuracy) {
        this.shooter = shooter;

        float cosPith = MathHelper.cos(pith * 0.017453292F);
        float vX = -MathHelper.sin(yaw * 0.017453292F) * cosPith;
        float vY = -MathHelper.sin(pith * 0.017453292F);
        float vZ = MathHelper.cos(yaw * 0.017453292F) * cosPith;
        this.calcMotions(
                vX,
                vY,
                vZ,
                power*((float)(1+Math.sqrt(spShip.speedSq())/2)), inaccuracy
        );

        this.setLocationAndAngles(
                shooter.posX + motionX*boost,
                shooter.posY + motionY*boost,
                shooter.posZ + motionZ*boost,
                MathUtils.wrapDegrees360(yaw),
                pith
        );
        return this;
    }


    protected void calcMotions(double x, double y, double z, float power, float inaccuracy) {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / f;
        y = y / f;
        z = z / f;
        if(inaccuracy != 0) {
            x = x + this.rand.nextGaussian() * 0.007499999832361937 * inaccuracy;
            y = y + this.rand.nextGaussian() * 0.007499999832361937 * inaccuracy;
            z = z + this.rand.nextGaussian() * 0.007499999832361937 * inaccuracy;
        }
        x = x * power;
        y = y * power;
        z = z * power;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }


    @Override
    protected void entityInit() {
        dataManager.register(SPSHIP_ID, -1);
        this.setSize(1.0F, 1.0F);
    }



    private short tick = 0;

    private boolean isLive(){
        return tick++<62;
    }


    private void calcImpact(){
        if(world.isRemote)return;
        BlockPos pos = new BlockPos(
                MathHelper.floor(posX),
                MathHelper.floor(posY),
                MathHelper.floor(posZ)
        );
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this,
                this.getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1));

        double d6 = 0;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        Vec3d vec3d1 = new Vec3d(posX+motionX, posY+motionY, posZ+motionZ);

        Entity entity = null;
        for (int i = 0; i < list.size(); ++i) {
            Entity entity1 = list.get(i);

            if (entity1.canBeCollidedWith() && entity1 != this
                    && !entity1.equals(shooter) && !entity1.noClip) {
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.3);
                RayTraceResult result = axisalignedbb.calculateIntercept(vec3d, vec3d1);

                if (result != null) {
                    double d7 = vec3d.squareDistanceTo(result.hitVec);

                    if (d7 < d6 || d6 == 0.0D) {
                        entity = entity1;
                        d6 = d7;
                    }
                }
            }
        }
        if(entity == null) {
            if(world.getBlockState(pos).getCollisionBoundingBox(world, pos) != Block.NULL_AABB){
                onImpact();
            }
        } else {
            onImpact(entity);
            onImpact();
        }
    }


    private void onImpact(Entity entity){
        if((mark & 1<<4) == 1<<4){
            if(entity instanceof PlayerSpship){
                PlayerSpship ps = (PlayerSpship) entity;
                if(ps.isSpshipMode()){
                    ps.getSp().getPowerSystem().damage((int) enrgDmg*3);
                }
            }
        }
        entity.attackEntityFrom(
                new SpShootDamage(shooter, physDmg, enrgDmg), 10);
    }


    private void onImpact(){
        setDead();
    }


    @Override
    public void onUpdate() {
        if( isLive() && (this.world.isRemote || (shooter == null ||
                !this.shooter.isDead) && this.world.isBlockLoaded(new BlockPos(this))) ){
            onClientUpdate();
            super.onUpdate();
            calcImpact();
            this.setPosition(
                    this.posX + motionX,
                    this.posY + motionY,
                    this.posZ + motionZ
            );
        } else {
            this.setDead();
        }
    }


    @Override
    public boolean canBeCollidedWith() {
        return true;
    }


    @Override
    public float getCollisionBorderSize() {
        return 1.0F;
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("d",
                this.newFloatNBTList(
                        (float) this.motionX,
                        (float) this.motionY,
                        (float) this.motionZ
//                        this.rotationYaw,
//                        this.rotationPitch
                )
        );
        compound.setShort("m", (short) getModelId());
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("d", 5);
        this.motionX = list.getFloatAt(0);
        this.motionY = list.getFloatAt(1);
        this.motionZ = list.getFloatAt(2);
//        this.rotationYaw = list.getFloatAt(3);
//        this.rotationPitch = list.getFloatAt(4);
        setModelId(compound.getShort("m"));
        clearSpRot();
        //onClientInit();
    }


    @Override
    public float getBrightness() {
        return 1.0F;
    }


    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }


    @Override
    public float getScale() {
        return 0.5f;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getSpModel() {
        return SpModelManager.getSpModelByID(getModelId());
    }


    @SideOnly(Side.CLIENT)
    private boolean first = false;
    @Override
    @SideOnly(Side.CLIENT)
    public void onClientUpdate() {
        if(first){
            super.onClientUpdate();
        } else {
            clearSpRot();
            first = true;
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean needRender() {
        return first;
    }
}