package net.minecraft.entity.ai;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import plus.fserver.world.TargetSelector;


public class EntityAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAITarget {
    protected final Class<T> targetClass;
    private final int targetChance;
    protected final EntityAINearestAttackableTarget.Sorter sorter;
    protected final Predicate <? super T > targetEntitySelector;
    protected T targetEntity;

    public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
        this(creature, classTarget, checkSight, false);
    }


    public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
        this(creature, classTarget, 10, checkSight, onlyNearby, null);
    }


    public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super T > targetSelector) {
        super(creature, checkSight, onlyNearby);
        this.targetClass = classTarget;
        this.targetChance = chance;
        this.sorter = new EntityAINearestAttackableTarget.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = targetSelector==null ? TargetSelector.NULL_TARGET_SELECTOR : new TargetSelector(targetSelector);
    }


    //private
    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
            this.targetEntity = null;
            return false;
        } else if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class) {
            List<T> list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            if (!list.isEmpty()) {
                list.sort(this.sorter);
                for (T potentialTarget : list) {
                    if(!potentialTarget.isEntityAlive())continue;
                    if (isSuitableTarget(potentialTarget, false)) {
                        this.targetEntity = potentialTarget;
                        return true;
                    }
                }
            }
            this.targetEntity = null;
            return false;
        } else {
            ArrayList<Player2Double> players = getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double) this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(),
                    player -> {
                        ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                        if (itemstack.getItem() == Items.SKULL) {
                            int i = itemstack.getItemDamage();
                            boolean flag = EntityAINearestAttackableTarget.this.taskOwner instanceof EntitySkeleton && i == 0;
                            boolean flag1 = EntityAINearestAttackableTarget.this.taskOwner instanceof EntityZombie && i == 2;
                            boolean flag2 = EntityAINearestAttackableTarget.this.taskOwner instanceof EntityCreeper && i == 4;

                            if (flag || flag1 || flag2) {
                                return 0.5D;
                            }
                        }
                        return 1.0D;
                    }, (Predicate<EntityPlayer>) this.targetEntitySelector
            );
            if (players.isEmpty()) {
                this.targetEntity = null;
                return false;
            }
            //todo check compare
            players.sort(Player2Double::compareTo);
            this.targetEntity = (T)players.get(0).player;
            return true;
        }
    }


    private static final class Player2Double implements Comparable<Player2Double>{
        private final EntityPlayer player;
        private final double factor;
        private Player2Double(EntityPlayer player, double factor) {
            this.player = player;
            this.factor = factor;
        }
        @Override
        public int compareTo(@NotNull Player2Double o) {
            return Double.compare(factor, o.factor);
        }
    }


    private ArrayList<Player2Double> getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance, double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble, @Nullable Predicate<EntityPlayer> predicate) {
        World world = taskOwner.world;

        ArrayList<Player2Double> players = new ArrayList<>();

        for (int j2 = 0; j2 < world.playerEntities.size(); ++j2) {
            EntityPlayer player = world.playerEntities.get(j2);
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
                    players.add(new Player2Double(player, range));
                }
            }
        }
        return players;
    }


    private static double round(double d){
        return ((long)d) & 0xFFFFFFFFFFFFFFFEL;
    }
    protected AxisAlignedBB getTargetableArea(double distance) {
        AxisAlignedBB aabb = this.taskOwner.getEntityBoundingBox();
        return new AxisAlignedBB(
                round(aabb.minX - distance),
                round(aabb.minY - 5),
                round(aabb.minZ - distance),
                round(aabb.maxX + distance),
                round(aabb.maxY + 5),
                round(aabb.maxZ + distance)
        );
    }


    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }


    public static class Sorter implements Comparator<Entity> {
        private final Entity entity;

        public Sorter(Entity entityIn) {
            this.entity = entityIn;
        }

        @Override
        public int compare(Entity entity, Entity other) {
            Entity cur;
            double d0 = (cur = this.entity).getDistanceSq(entity);
            double d1 = cur.getDistanceSq(other);
            if (d0 < d1) {
                return -1;
            } else {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
}