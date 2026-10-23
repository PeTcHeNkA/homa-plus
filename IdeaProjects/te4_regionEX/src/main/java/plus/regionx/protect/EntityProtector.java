package plus.regionx.protect;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import plus.region.Region;
import plus.region.RegionQuery;
import plus.regionx.FlagUtils;
import plus.regionx.access.WorldFieldAccess;
import plus.regionx.access.WorldLocal;
import plus.regionx.data.RegionData;
import plus.regionx.data.flag.UserData;


public class EntityProtector extends BaseProtector {
    public EntityProtector(WorldFieldAccess access) {
        super(access);
    }


    @SubscribeEvent
    public void onEntityAttack(LivingDamageEvent event) {
        DamageSource src = event.getSource();
        if(event.getEntityLiving() instanceof EntityPlayer) {
            if (src instanceof EntityDamageSource) {

                EntityDamageSource source = (EntityDamageSource) src;
                if (source.getTrueSource() instanceof EntityPlayer) {
                    WorldLocal local = access.get(event.getEntity().getEntityWorld());
                    BlockPos pos = event.getEntity().getPosition();

                    if(canPvp(local, (EntityPlayer) source.getTrueSource(), (EntityPlayer) event.getEntityLiving(), pos)) {
                        event.setCanceled(true);
                    }
                }
            }
        } else {
            if (src instanceof EntityDamageSource) {
                EntityDamageSource source = (EntityDamageSource) src;
                if (source.getTrueSource() instanceof EntityPlayer) {
                    WorldLocal local = access.get(event.getEntity().getEntityWorld());
                    BlockPos pos = event.getEntity().getPosition();
                    if(canPve(local, (EntityPlayer) source.getTrueSource(), event.getEntityLiving(), pos)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }


    private boolean canPve(WorldLocal local, EntityPlayer player, EntityLivingBase target, BlockPos pos){
        RegionQuery regions = local.context().getRegions(pos.getX(), pos.getY(), pos.getZ());

        if(regions.isEmpty()) return true;

        for (Region region : regions) {
            RegionData data = region.getData(local.getDataManager());
            if(data == null) continue;
            UserData atkData    = data.getData(player.getUniqueID());

            if(!FlagUtils.canPVE(data, atkData)) return false;
        }
        return true;
    }


    private boolean canPvp(WorldLocal local, EntityPlayer attacker, EntityPlayer target, BlockPos pos){
        RegionQuery regions = local.context().getRegions(pos.getX(), pos.getY(), pos.getZ());

        if(regions.isEmpty()) return true;

        for (Region region : regions) {
            RegionData data = region.getData(local.getDataManager());
            if(data == null) continue;

            UserData atkData    = data.getData(attacker.getUniqueID());
            UserData targetData = data.getData(target.getUniqueID()  );

            if(!FlagUtils.canPVP(data, atkData, targetData)) return false;
        }
        return true;
    }
}
