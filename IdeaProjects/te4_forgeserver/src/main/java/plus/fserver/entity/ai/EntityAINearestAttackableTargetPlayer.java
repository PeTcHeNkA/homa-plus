package plus.fserver.entity.ai;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class EntityAINearestAttackableTargetPlayer extends EntityAITarget {
    //final Predicate<EntityPlayer> targetEntitySelector;
    private final int targetChance;
    protected EntityPlayer targetEntity;

    public EntityAINearestAttackableTargetPlayer(EntityCreature creature, boolean checkSight) {
        this(creature, checkSight, false);
    }


    public EntityAINearestAttackableTargetPlayer(EntityCreature creature, boolean checkSight, boolean onlyNearby) {
        this(creature, 10, checkSight, onlyNearby);
    }


    public EntityAINearestAttackableTargetPlayer(EntityCreature creature, int chance, boolean checkSight, boolean onlyNearby) {
        super(creature, checkSight, onlyNearby);
        this.targetChance = chance;
        this.setMutexBits(1);
        //this.targetEntitySelector = targetSelector==null ? TargetPlayerSelector.NULL_PLAYER_SELECTOR : new TargetPlayerSelector(targetSelector);
    }


    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
            this.targetEntity = null;
            return false;
        } else {
            ArrayList<EntityAINearestAttackableTarget.Player2Double> players = getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double) this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(),
                    player -> {
                        ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                        if (itemstack.getItem() == Items.SKULL) {
                            int i = itemstack.getItemDamage();
                            boolean flag  = this.taskOwner instanceof EntitySkeleton && i == 0;
                            boolean flag1 = this.taskOwner instanceof EntityZombie && i == 2;
                            boolean flag2 = this.taskOwner instanceof EntityCreeper && i == 4;

                            if (flag || flag1 || flag2) {
                                return 0.5D;
                            }
                        }
                        return 1.0D;
                    }, getSelector()
            );
            if (players.isEmpty()) {
                this.targetEntity = null;
                return false;
            }
            //todo check compare
            players.sort(EntityAINearestAttackableTarget.Player2Double::compareTo);
            this.targetEntity = players.get(0).player;
            return true;
        }
    }


    protected Predicate<EntityPlayer> getSelector(){
        return TargetPlayerSelector.NULL_PLAYER_SELECTOR;
    }


    private ArrayList<EntityAINearestAttackableTarget.Player2Double> getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance, double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble, @Nullable Predicate<EntityPlayer> predicate) {
        final ArrayList<EntityAINearestAttackableTarget.Player2Double> players = new ArrayList<>();
        final List<EntityPlayer> players0 = taskOwner.world.playerEntities;

        for (int j2 = 0; j2 < players0.size(); ++j2) {
            EntityPlayer player = players0.get(j2);
            //?
            if(!player.isEntityAlive())continue;

            if (!player.capabilities.disableDamage && player.isEntityAlive() && !player.isSpectator() && (predicate == null || predicate.apply(player))) {
                double range = player.getDistanceSq(posX, player.posY, posZ);
                double curXZ = maxXZDistance;

                if (player.isSneaking()) {
                    curXZ = maxXZDistance * 0.800000011920929D;
                }

                if (player.isInvisible()) {
                    float f = player.getArmorVisibility();

                    if (f < 0.1F) {
                        f = 0.1F;
                    }
                    curXZ *= 0.7F * f;
                }

                if (playerToDouble != null) {//???
                    curXZ *= MoreObjects.firstNonNull(playerToDouble.apply(player), 1.0D);
                }

                curXZ = ForgeHooks.getPlayerVisibilityDistance(player, curXZ, maxYDistance);

                if ((maxYDistance < 0.0D || Math.abs(player.posY - posY) < maxYDistance * maxYDistance) && (maxXZDistance < 0.0D || range < curXZ * curXZ)) {
                    players.add(new EntityAINearestAttackableTarget.Player2Double(player, range));
                }
            }
        }
        return players;
    }


    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }
}