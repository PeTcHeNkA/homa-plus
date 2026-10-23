package exmob;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.mobs.frame.AbcMob;
import plus.mobs.utl.AnimationFolder;


public class %MOB_CLASS% extends AbcMob{
    public static final DataParameter<Byte> STATE =
            EntityDataManager.createKey(%MOB_CLASS%.class, DataSerializers.BYTE);

    public static IEntityApplicator MODEL;
    public static IEntityApplicator ATTRIBUTES;
    public static MobHitbox HITBOX;
    @SideOnly(Side.CLIENT)
    public static AnimationFolder ANIM_FOLDER;

    public %MOB_CLASS%(World worldIn) {
        super(worldIn);
        this.setSize(HITBOX.width, HITBOX.height);
    }


    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(STATE, (byte)-1);
    }


    @Override
    public float getEyeHeight() {
        return HITBOX.eyeHeight;
    }


    @Override
    protected void initEntityAI() {
        MODEL.apply(this);
    }


    @Override
    public void setState(byte state) {
        this.getDataManager().set(STATE, state);
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        ATTRIBUTES.apply(this);
    }


    @SideOnly(Side.CLIENT)
    public byte getState() {
        return this.dataManager.get(STATE);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public AnimationFolder getAnimFolder() {
        return ANIM_FOLDER;
    }
}