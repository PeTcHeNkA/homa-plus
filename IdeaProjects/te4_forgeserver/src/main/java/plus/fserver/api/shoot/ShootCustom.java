package plus.fserver.api.shoot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import plus.fserver.server.elca.ChunkSelector;
import plus.tson.utl.Te4HashSet;


public class ShootCustom extends ShootRTX{
    private final Te4HashSet<Entity> targets = new Te4HashSet<>();
    private EnumParticleTypes type = EnumParticleTypes.FLAME;
    private int num = 50;
    private double speed = 0.05d;

    protected int maxTargets = 1;
    protected int curTargets;
    protected int damage = 10;
    protected double pow = 1, maxDistance = 100;

    public ShootCustom setParticle(EnumParticleTypes type, int num, double speed){
        this.type = type;
        this.num = num;
        this.speed = speed;
        return this;
    }


    public ShootRTX calc(Entity shooter, float yaw, float pitch) {
        return super.calc(shooter, yaw, pitch, pow, maxDistance);
    }


    public ShootCustom setMaxTargets(int max){
        this.maxTargets = curTargets = max;
        return this;
    }


    public ShootCustom setPower(double power, double maxDistance, int damage){
        this.pow = power;
        this.maxDistance = maxDistance;
        this.damage = damage;
        return this;
    }


    @Override
    public void onInit() {
        curTargets = maxTargets;
        AxisAlignedBB aabb = getAABB();
        double scale = pow * 0.1d;
        aabb.growTo(scale, scale, scale, aabb);
    }


    @Override
    public void onComplete() {
        targets.clear();
    }


    @Override
    public void onNextPos(ChunkSelector acc, double x, double y, double z) {
        acc.getWorld().spawnParticle(type, x, y, z, num, 0d, 0d, 0d, speed);
    }


    @Override
    public boolean onHitEntities(ChunkSelector acc, Entity s) {
        Te4HashSet<Entity> targets = this.targets;
        boolean hit = false;

        DamageSource src;
        if(s != null && s.isLiving()) {
            if (s instanceof EntityPlayerMP) {
                src = DamageSource.causePlayerDamage((EntityPlayer) s);
            } else {
                src = DamageSource.causeMobDamage((EntityLivingBase) s);
            }
        } else {
            src = DamageSource.MAGIC;
        }

        for (Entity entity:this.entities()){
            if(targets.add(entity)){
                if(onEntityAttacked(src, entity))
                    hit = true;
            }
        }
        if(hit) return --curTargets < 0;
        return false;
    }


    public boolean onEntityAttacked(DamageSource src, Entity entity){
        int dmg = damage;
        int cp = curTargets;

        if(cp < 0){
            dmg >>= 1;
        } else {
            int mx = maxTargets;

            if(cp != mx){
                float dmg2 = dmg / 2f;
                dmg = (int)(dmg2 + MathHelper.floor(dmg2 * ((float) mx / cp)));
            }
        }
        return entity.attackEntityFrom(src, dmg);
    }
}