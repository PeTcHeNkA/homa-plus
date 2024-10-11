package net.minecraft.entity.item;

import com.google.common.base.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.*;
import net.minecraft.util.datafix.walkers.*;
import net.minecraft.util.datafix.*;
import net.minecraft.nbt.*;
import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.world.*;
import net.minecraft.init.*;
import net.minecraft.block.*;
import net.minecraft.util.math.*;
import net.minecraft.item.*;
import net.minecraft.block.material.*;
import net.minecraft.util.*;
import javax.annotation.*;
import net.minecraft.entity.effect.*;
import net.minecraft.network.datasync.*;
import plus.fserver.ElcaMobs;


public class EntityArmorStand extends EntityLivingBase {
    private static final Rotations DEFAULT_HEAD_ROTATION;
    private static final Rotations DEFAULT_BODY_ROTATION;
    private static final Rotations DEFAULT_LEFTARM_ROTATION;
    private static final Rotations DEFAULT_RIGHTARM_ROTATION;
    private static final Rotations DEFAULT_LEFTLEG_ROTATION;
    private static final Rotations DEFAULT_RIGHTLEG_ROTATION;
    public static final DataParameter<Byte> STATUS;
    public static final DataParameter<Rotations> HEAD_ROTATION;
    public static final DataParameter<Rotations> BODY_ROTATION;
    public static final DataParameter<Rotations> LEFT_ARM_ROTATION;
    public static final DataParameter<Rotations> RIGHT_ARM_ROTATION;
    public static final DataParameter<Rotations> LEFT_LEG_ROTATION;
    public static final DataParameter<Rotations> RIGHT_LEG_ROTATION;
    private static final Predicate<Entity> IS_RIDEABLE_MINECART;
    private final NonNullList<ItemStack> handItems;
    private final NonNullList<ItemStack> armorItems;
    private boolean canInteract;
    public long punchCooldown;
    private int disabledSlots;
    private boolean wasMarker;
    private Rotations headRotation;
    private Rotations bodyRotation;
    private Rotations leftArmRotation;
    private Rotations rightArmRotation;
    private Rotations leftLegRotation;
    private Rotations rightLegRotation;

    public EntityArmorStand(final World worldIn) {
        super(worldIn);
        this.handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        this.armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
        this.headRotation = EntityArmorStand.DEFAULT_HEAD_ROTATION;
        this.bodyRotation = EntityArmorStand.DEFAULT_BODY_ROTATION;
        this.leftArmRotation = EntityArmorStand.DEFAULT_LEFTARM_ROTATION;
        this.rightArmRotation = EntityArmorStand.DEFAULT_RIGHTARM_ROTATION;
        this.leftLegRotation = EntityArmorStand.DEFAULT_LEFTLEG_ROTATION;
        this.rightLegRotation = EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION;
        this.noClip = this.hasNoGravity();
        this.setSize(0.5f, 1.975f);
    }

    public EntityArmorStand(final World worldIn, final double posX, final double posY, final double posZ) {
        this(worldIn);
        this.mutPosition(posX, posY, posZ);
    }


    @Override
    protected final void setSize(final float width, final float height) {
        final double d0 = this.posX;
        final double d2 = this.posY;
        final double d3 = this.posZ;
        final float f = this.hasMarker() ? 0.0f : (this.isChild() ? 0.5f : 1.0f);
        super.setSize(width * f, height * f);
        this.mutPosition(d0, d2, d3);
    }


    @Override
    public boolean isServerWorld() {
        return super.isServerWorld() && !this.hasNoGravity();
    }


    @Override
    public boolean ignoreCollisions(){
        return true;
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        EntityDataManager dataManager = this.dataManager;

        dataManager.register(EntityArmorStand.STATUS, (byte)0);
        dataManager.register(EntityArmorStand.HEAD_ROTATION, EntityArmorStand.DEFAULT_HEAD_ROTATION);
        dataManager.register(EntityArmorStand.BODY_ROTATION, EntityArmorStand.DEFAULT_BODY_ROTATION);
        dataManager.register(EntityArmorStand.LEFT_ARM_ROTATION, EntityArmorStand.DEFAULT_LEFTARM_ROTATION);
        dataManager.register(EntityArmorStand.RIGHT_ARM_ROTATION, EntityArmorStand.DEFAULT_RIGHTARM_ROTATION);
        dataManager.register(EntityArmorStand.LEFT_LEG_ROTATION, EntityArmorStand.DEFAULT_LEFTLEG_ROTATION);
        dataManager.register(EntityArmorStand.RIGHT_LEG_ROTATION, EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION);
    }


    @Override
    public Iterable<ItemStack> getHeldEquipment() {
        return this.handItems;
    }


    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return this.armorItems;
    }


    @Override
    public ItemStack getItemStackFromSlot(final EntityEquipmentSlot slotIn) {
        switch (slotIn.getSlotType()) {
            case HAND: {
                return this.handItems.get(slotIn.getIndex());
            }
            case ARMOR: {
                return this.armorItems.get(slotIn.getIndex());
            }
            default: {
                return ItemStack.EMPTY;
            }
        }
    }


    @Override
    public void setItemStackToSlot(final EntityEquipmentSlot slotIn, final ItemStack stack) {
        switch (slotIn.getSlotType()) {
            case HAND: {
                this.playEquipSound(stack);
                this.handItems.set(slotIn.getIndex(), stack);
                break;
            }
            case ARMOR: {
                this.playEquipSound(stack);
                this.armorItems.set(slotIn.getIndex(), stack);
                break;
            }
        }
    }


    @Override
    public boolean replaceItemInInventory(final int index, final ItemStack stack) {
        EntityEquipmentSlot slot;
        if (index == 98) {
            slot = EntityEquipmentSlot.MAINHAND;
        }
        else if (index == 99) {
            slot = EntityEquipmentSlot.OFFHAND;
        }
        else if (index == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
            slot = EntityEquipmentSlot.HEAD;
        }
        else if (index == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
            slot = EntityEquipmentSlot.CHEST;
        }
        else if (index == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
            slot = EntityEquipmentSlot.LEGS;
        }
        else {
            if (index != 100 + EntityEquipmentSlot.FEET.getIndex()) {
                return false;
            }
            slot = EntityEquipmentSlot.FEET;
        }
        if (!stack.isEmpty() && !EntityLiving.isItemStackInSlot(slot, stack) && slot != EntityEquipmentSlot.HEAD) {
            return false;
        }
        this.setItemStackToSlot(slot, stack);
        return true;
    }


    public static void registerFixesArmorStand(final DataFixer fixer) {
        fixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(EntityArmorStand.class, "ArmorItems", "HandItems"));
    }


    @Override
    public void writeEntityToNBT(final NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        final NBTTagList nbttaglist = new NBTTagList();
        for (final ItemStack itemstack : this.armorItems) {
            final NBTTagCompound nbttagcompound = new NBTTagCompound(8);
            if (!itemstack.isEmpty()) {
                itemstack.writeToNBT(nbttagcompound);
            }
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("ArmorItems", nbttaglist);
        final NBTTagList nbttaglist2 = new NBTTagList();
        for (final ItemStack itemstack2 : this.handItems) {
            final NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            if (!itemstack2.isEmpty()) {
                itemstack2.writeToNBT(nbttagcompound2);
            }
            nbttaglist2.appendTag(nbttagcompound2);
        }
        compound.setTag("HandItems", nbttaglist2);
        compound.setBoolean("Invisible", this.isInvisible());
        compound.setBoolean("Small", this.isSmall());
        compound.setBoolean("ShowArms", this.getShowArms());
        compound.setIntegerU("DisabledSlots", this.disabledSlots);
        compound.setBoolean("NoBasePlate", this.hasNoBasePlate());
        if (this.hasMarker()) {
            compound.setBoolean("Marker", this.hasMarker());
        }
        compound.setTag("Pose", this.readPoseFromNBT());
    }


    @Override
    public void readEntityFromNBT(final NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("ArmorItems", 9)) {
            final NBTTagList nbttaglist = compound.getTagList("ArmorItems", 10);
            for (int i = 0; i < this.armorItems.size(); ++i) {
                this.armorItems.set(i, new ItemStack(nbttaglist.getCompoundTagAt(i)));
            }
        }
        if (compound.hasKey("HandItems", 9)) {
            final NBTTagList nbttaglist2 = compound.getTagList("HandItems", 10);
            for (int j = 0; j < this.handItems.size(); ++j) {
                this.handItems.set(j, new ItemStack(nbttaglist2.getCompoundTagAt(j)));
            }
        }
        this.setInvisible(compound.getBoolean("Invisible"));
        this.setSmall(compound.getBoolean("Small"));
        this.setShowArms(compound.getBoolean("ShowArms"));
        this.disabledSlots = compound.getInteger("DisabledSlots");
        this.setNoBasePlate(compound.getBoolean("NoBasePlate"));
        this.setMarker(compound.getBoolean("Marker"));
        this.wasMarker = !this.hasMarker();
        this.noClip = this.hasNoGravity();
        final NBTTagCompound nbttagcompound = compound.getCompoundTag("Pose");
        this.writePoseToNBT(nbttagcompound);
    }


    private void writePoseToNBT(final NBTTagCompound tagCompound) {
        final NBTTagList nbttaglist = tagCompound.getTagList("Head", 5);
        this.setHeadRotation(nbttaglist.isEmpty() ? EntityArmorStand.DEFAULT_HEAD_ROTATION : new Rotations(nbttaglist));
        final NBTTagList nbttaglist2 = tagCompound.getTagList("Body", 5);
        this.setBodyRotation(nbttaglist2.isEmpty() ? EntityArmorStand.DEFAULT_BODY_ROTATION : new Rotations(nbttaglist2));
        final NBTTagList nbttaglist3 = tagCompound.getTagList("LeftArm", 5);
        this.setLeftArmRotation(nbttaglist3.isEmpty() ? EntityArmorStand.DEFAULT_LEFTARM_ROTATION : new Rotations(nbttaglist3));
        final NBTTagList nbttaglist4 = tagCompound.getTagList("RightArm", 5);
        this.setRightArmRotation(nbttaglist4.isEmpty() ? EntityArmorStand.DEFAULT_RIGHTARM_ROTATION : new Rotations(nbttaglist4));
        final NBTTagList nbttaglist5 = tagCompound.getTagList("LeftLeg", 5);
        this.setLeftLegRotation(nbttaglist5.isEmpty() ? EntityArmorStand.DEFAULT_LEFTLEG_ROTATION : new Rotations(nbttaglist5));
        final NBTTagList nbttaglist6 = tagCompound.getTagList("RightLeg", 5);
        this.setRightLegRotation(nbttaglist6.isEmpty() ? EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION : new Rotations(nbttaglist6));
    }


    private NBTTagCompound readPoseFromNBT() {
        final NBTTagCompound nbttagcompound = new NBTTagCompound(8,1);
        if (!EntityArmorStand.DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
            nbttagcompound.setTag("Head", this.headRotation.writeToNBT());
        }
        if (!EntityArmorStand.DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
            nbttagcompound.setTag("Body", this.bodyRotation.writeToNBT());
        }
        if (!EntityArmorStand.DEFAULT_LEFTARM_ROTATION.equals(this.leftArmRotation)) {
            nbttagcompound.setTag("LeftArm", this.leftArmRotation.writeToNBT());
        }
        if (!EntityArmorStand.DEFAULT_RIGHTARM_ROTATION.equals(this.rightArmRotation)) {
            nbttagcompound.setTag("RightArm", this.rightArmRotation.writeToNBT());
        }
        if (!EntityArmorStand.DEFAULT_LEFTLEG_ROTATION.equals(this.leftLegRotation)) {
            nbttagcompound.setTag("LeftLeg", this.leftLegRotation.writeToNBT());
        }
        if (!EntityArmorStand.DEFAULT_RIGHTLEG_ROTATION.equals(this.rightLegRotation)) {
            nbttagcompound.setTag("RightLeg", this.rightLegRotation.writeToNBT());
        }
        return nbttagcompound;
    }


    @Override
    public boolean canBePushed() {
        return false;
    }


    @Override
    protected void collideWithEntity(final Entity entityIn) {}


    @Override
    public void collideWith(Entity entity){
        if (this.getDistanceSq(entity) <= 0.2) {
            entity.applyEntityCollision(this);
        }
    }


    @Override
    protected void collideWithNearbyEntities() {
        if(lodLvl() != 0)return;
        final List<Entity> list = this.world.getEntitiesInAABBexcludingLimited(this, this.getEntityBoundingBox(), EntityArmorStand.IS_RIDEABLE_MINECART, 3);
        for (int i = 0; i < list.size(); ++i) {
            final Entity entity = list.get(i);
            if (this.getDistanceSq(entity) <= 0.2) {
                entity.applyEntityCollision(this);
            }
        }
    }


    @Override
    public EnumActionResult applyPlayerInteraction(final EntityPlayer player, final Vec3d vec, final EnumHand hand) {
        final ItemStack itemstack = player.getHeldItem(hand);
        if (this.hasMarker() || itemstack.getItem() == Items.NAME_TAG) {
            return EnumActionResult.PASS;
        }
        if (!this.world.isRemote && !player.isSpectator()) {
            final EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);
            if (itemstack.isEmpty()) {
                final EntityEquipmentSlot entityequipmentslot2 = this.getClickedSlot(vec);
                final EntityEquipmentSlot entityequipmentslot3 = this.isDisabled(entityequipmentslot2) ? entityequipmentslot : entityequipmentslot2;
                if (this.hasItemInSlot(entityequipmentslot3)) {
                    this.swapItem(player, entityequipmentslot3, itemstack, hand);
                }
            }
            else {
                if (this.isDisabled(entityequipmentslot)) {
                    return EnumActionResult.FAIL;
                }
                if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.HAND && !this.getShowArms()) {
                    return EnumActionResult.FAIL;
                }
                this.swapItem(player, entityequipmentslot, itemstack, hand);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.SUCCESS;
    }


    protected EntityEquipmentSlot getClickedSlot(final Vec3d vec3d) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.MAINHAND;
        final boolean flag = this.isSmall();
        final double d0 = flag ? (vec3d.y * 2.0) : vec3d.y;
        final EntityEquipmentSlot slot2 = EntityEquipmentSlot.FEET;
        if (d0 >= 0.1 && d0 < 0.1 + (flag ? 0.8 : 0.45) && this.hasItemInSlot(slot2)) {
            slot = EntityEquipmentSlot.FEET;
        }
        else if (d0 >= 0.9 + (flag ? 0.3 : 0.0) && d0 < 0.9 + (flag ? 1.0 : 0.7) && this.hasItemInSlot(EntityEquipmentSlot.CHEST)) {
            slot = EntityEquipmentSlot.CHEST;
        }
        else if (d0 >= 0.4 && d0 < 0.4 + (flag ? 1.0 : 0.8) && this.hasItemInSlot(EntityEquipmentSlot.LEGS)) {
            slot = EntityEquipmentSlot.LEGS;
        }
        else if (d0 >= 1.6 && this.hasItemInSlot(EntityEquipmentSlot.HEAD)) {
            slot = EntityEquipmentSlot.HEAD;
        }
        return slot;
    }


    private boolean isDisabled(final EntityEquipmentSlot slotIn) {
        return (this.disabledSlots & 1 << slotIn.getSlotIndex()) != 0x0;
    }


    private void swapItem(final EntityPlayer player, final EntityEquipmentSlot slot, final ItemStack stack, final EnumHand hand) {
        final ItemStack itemstack = this.getItemStackFromSlot(slot);
        if ((itemstack.isEmpty() || (this.disabledSlots & 1 << slot.getSlotIndex() + 8) == 0x0) && (!itemstack.isEmpty() || (this.disabledSlots & 1 << slot.getSlotIndex() + 16) == 0x0)) {
            if (player.capabilities.isCreativeMode && itemstack.isEmpty() && !stack.isEmpty()) {
                final ItemStack copy = stack.copy();
                copy.setCount(1);
                this.setItemStackToSlot(slot, copy);
            }
            else if (!stack.isEmpty() && stack.getCount() > 1) {
                if (itemstack.isEmpty()) {
                    final ItemStack itemstack3 = stack.copy();
                    itemstack3.setCount(1);
                    this.setItemStackToSlot(slot, itemstack3);
                    stack.shrink(1);
                }
            }
            else {
                this.setItemStackToSlot(slot, stack);
                player.setHeldItem(hand, itemstack);
            }
        }
    }


    @Override
    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        if (this.world.isRemote || this.isDead) {
            return false;
        }
        if (DamageSource.OUT_OF_WORLD.equals(source)) {
            this.setDead();
            return false;
        }
        if (this.isEntityInvulnerable(source) || this.canInteract || this.hasMarker()) {
            return false;
        }
        if (source.isExplosion()) {
            this.dropContents();
            this.setDead();
            return false;
        }
        if (DamageSource.IN_FIRE.equals(source)) {
            if (this.isBurning()) {
                this.damageArmorStand(0.15f);
            }
            else {
                this.setFire(5);
            }
            return false;
        }
        if (DamageSource.ON_FIRE.equals(source) && this.getHealth() > 0.5f) {
            this.damageArmorStand(4.0f);
            return false;
        }
        final boolean flag = "arrow".equals(source.getDamageType());
        final boolean flag2 = "player".equals(source.getDamageType());
        if (!flag2 && !flag) {
            return false;
        }
        if (source.getImmediateSource() instanceof EntityArrow) {
            source.getImmediateSource().setDead();
        }
        if (source.getTrueSource() instanceof EntityPlayer && !((EntityPlayer)source.getTrueSource()).capabilities.allowEdit) {
            return false;
        }
        if (source.isCreativePlayer()) {
            this.playBrokenSound();
            this.playParticles();
            this.setDead();
            return false;
        }
        final long i = this.world.getTotalWorldTime();
        if (i - this.punchCooldown > 5L && !flag) {
            this.world.setEntityState(this, (byte)32);
            this.punchCooldown = i;
        }
        else {
            this.dropBlock();
            this.playParticles();
            this.setDead();
        }
        return false;
    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(final byte id) {
        if (id == 32) {
            if (this.world.isRemote) {
                this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_HIT, this.getSoundCategory(), 0.3f, 1.0f, false);
                this.punchCooldown = this.world.getTotalWorldTime();
            }
        }
        else {
            super.handleStatusUpdate(id);
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(final double distance) {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0;
        if (Double.isNaN(d0) || d0 == 0.0) {
            d0 = 4.0;
        }
        d0 *= 64.0;
        return distance < d0 * d0;
    }


    private void playParticles() {
        if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + this.height / 1.5, this.posZ, 10, this.width / 4.0f, this.height / 4.0f, this.width / 4.0f, 0.05, Block.getStateId(Blocks.PLANKS.getDefaultState()));
        }
    }


    private void damageArmorStand(final float damage) {
        float f = this.getHealth();
        f -= damage;
        if (f <= 0.5f) {
            this.dropContents();
            this.setDead();
        }
        else {
            this.setHealth(f);
        }
    }


    private void dropBlock() {
        Block.spawnAsEntity(this.world, new BlockPos(this), new ItemStack(Items.ARMOR_STAND));
        this.dropContents();
    }


    private void dropContents() {
        this.playBrokenSound();
        for (int i = 0; i < this.handItems.size(); ++i) {
            final ItemStack itemstack = this.handItems.get(i);
            if (!itemstack.isEmpty()) {
                Block.spawnAsEntity(this.world, new BlockPos(this).up(), itemstack);
                this.handItems.set(i, ItemStack.EMPTY);
            }
        }
        for (int j = 0; j < this.armorItems.size(); ++j) {
            final ItemStack itemstack2 = this.armorItems.get(j);
            if (!itemstack2.isEmpty()) {
                Block.spawnAsEntity(this.world, new BlockPos((Entity)this).up(), itemstack2);
                this.armorItems.set(j, ItemStack.EMPTY);
            }
        }
    }

    private void playBrokenSound() {
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_BREAK, this.getSoundCategory(), 1.0f, 1.0f);
    }


    @Override
    protected float updateDistance(final float f1, final float f2) {
        this.prevRenderYawOffset = this.prevRotationYaw;
        this.renderYawOffset = this.rotationYaw;
        return 0.0f;
    }


    @Override
    public float getEyeHeight() {
        return this.isChild() ? (this.height * 0.5f) : (this.height * 0.9f);
    }


    @Override
    public double getYOffset() {
        return this.hasMarker() ? 0.0 : 0.10000000149011612;
    }


    @Override
    public void travel(final float strafe, final float vertical, final float forward) {
        if (!this.hasNoGravity()) {
            super.travel(strafe, vertical, forward);
        }
    }


    @Override
    public void setRenderYawOffset(final float offset) {
        this.prevRotationYaw = offset;
        this.prevRenderYawOffset = offset;
        this.rotationYawHead = offset;
        this.prevRotationYawHead = offset;
    }


    @Override
    public void setRotationYawHead(final float rotation) {
        this.prevRotationYaw = rotation;
        this.prevRenderYawOffset = rotation;
        this.rotationYawHead = rotation;
        this.prevRotationYawHead = rotation;
    }


    @Override
    public void onUpdate() {
        if (world.onLivingUpdate(this)) return;
        onUpdateE();
        onUpdateB();

        final Rotations rotations = this.dataManager.get(EntityArmorStand.HEAD_ROTATION);
        if (!this.headRotation.equals(rotations)) {
            this.setHeadRotation(rotations);
        }
        final Rotations rotations2 = this.dataManager.get(EntityArmorStand.BODY_ROTATION);
        if (!this.bodyRotation.equals(rotations2)) {
            this.setBodyRotation(rotations2);
        }
        final Rotations rotations3 = this.dataManager.get(EntityArmorStand.LEFT_ARM_ROTATION);
        if (!this.leftArmRotation.equals(rotations3)) {
            this.setLeftArmRotation(rotations3);
        }
        final Rotations rotations4 = this.dataManager.get(EntityArmorStand.RIGHT_ARM_ROTATION);
        if (!this.rightArmRotation.equals(rotations4)) {
            this.setRightArmRotation(rotations4);
        }
        final Rotations rotations5 = this.dataManager.get(EntityArmorStand.LEFT_LEG_ROTATION);
        if (!this.leftLegRotation.equals(rotations5)) {
            this.setLeftLegRotation(rotations5);
        }
        final Rotations rotations6 = this.dataManager.get(EntityArmorStand.RIGHT_LEG_ROTATION);
        if (!this.rightLegRotation.equals(rotations6)) {
            this.setRightLegRotation(rotations6);
        }
        final boolean flag = this.hasMarker();
        if (this.wasMarker != flag) {
            this.updateBoundingBox(flag);
            this.preventEntitySpawning = !flag;
            this.wasMarker = flag;
        }
    }


    @Override
    public void spawnRunningParticles() {
        return;
    }


    private void updateBoundingBox(final boolean hasAABB) {
        if (hasAABB) {
            this.setSize(0.0f, 0.0f);
        } else {
            this.setSize(0.5f, 1.975f);
        }
    }


    @Override
    protected void updatePotionMetadata() {
        this.setInvisible(this.canInteract);
    }


    @Override
    public void setInvisible(final boolean invisible) {
        super.setInvisible(this.canInteract = invisible);
    }


    @Override
    public boolean isChild() {
        return this.isSmall();
    }


    @Override
    public void onKillCommand() {
        this.setDead();
    }


    @Override
    public boolean isImmuneToExplosions() {
        return this.isInvisible();
    }


    @Override
    public EnumPushReaction getPushReaction() {
        return this.hasMarker() ? EnumPushReaction.IGNORE : super.getPushReaction();
    }


    private void setSmall(final boolean small) {
        this.dataManager.set(EntityArmorStand.STATUS, this.setBit(this.dataManager.get(EntityArmorStand.STATUS), 1, small));
        this.setSize(0.5f, 1.975f);
    }


    public boolean isSmall() {
        return (this.dataManager.get(EntityArmorStand.STATUS) & 0x1) != 0x0;
    }


    private void setShowArms(final boolean showArms) {
        this.dataManager.set(EntityArmorStand.STATUS, this.setBit(this.dataManager.get(EntityArmorStand.STATUS), 4, showArms));
    }


    public boolean getShowArms() {
        return (this.dataManager.get(EntityArmorStand.STATUS) & 0x4) != 0x0;
    }


    private void setNoBasePlate(final boolean noBasePlate) {
        this.dataManager.set(EntityArmorStand.STATUS, this.setBit(this.dataManager.get(EntityArmorStand.STATUS), 8, noBasePlate));
    }


    public boolean hasNoBasePlate() {
        return (this.dataManager.get(EntityArmorStand.STATUS) & 0x8) != 0x0;
    }


    private void setMarker(final boolean marker) {
        this.dataManager.set(EntityArmorStand.STATUS, this.setBit(this.dataManager.get(EntityArmorStand.STATUS), 16, marker));
        this.setSize(0.5f, 1.975f);
    }


    public boolean hasMarker() {
        return (this.dataManager.get(EntityArmorStand.STATUS) & 0x10) != 0x0;
    }


    private byte setBit(byte p_184797_1_, final int p_184797_2_, final boolean p_184797_3_) {
        if (p_184797_3_) {
            p_184797_1_ |= (byte)p_184797_2_;
        }
        else {
            p_184797_1_ &= (byte)~p_184797_2_;
        }
        return p_184797_1_;
    }


    public void setHeadRotation(final Rotations vec) {
        this.headRotation = vec;
        this.dataManager.set(EntityArmorStand.HEAD_ROTATION, vec);
    }

    public void setBodyRotation(final Rotations vec) {
        this.bodyRotation = vec;
        this.dataManager.set(EntityArmorStand.BODY_ROTATION, vec);
    }

    public void setLeftArmRotation(final Rotations vec) {
        this.leftArmRotation = vec;
        this.dataManager.set(EntityArmorStand.LEFT_ARM_ROTATION, vec);
    }

    public void setRightArmRotation(final Rotations vec) {
        this.rightArmRotation = vec;
        this.dataManager.set(EntityArmorStand.RIGHT_ARM_ROTATION, vec);
    }

    public void setLeftLegRotation(final Rotations vec) {
        this.leftLegRotation = vec;
        this.dataManager.set(EntityArmorStand.LEFT_LEG_ROTATION, vec);
    }

    public void setRightLegRotation(final Rotations vec) {
        this.rightLegRotation = vec;
        this.dataManager.set(EntityArmorStand.RIGHT_LEG_ROTATION, vec);
    }

    public Rotations getHeadRotation() {
        return this.headRotation;
    }

    public Rotations getBodyRotation() {
        return this.bodyRotation;
    }

    @SideOnly(Side.CLIENT)
    public Rotations getLeftArmRotation() {
        return this.leftArmRotation;
    }

    @SideOnly(Side.CLIENT)
    public Rotations getRightArmRotation() {
        return this.rightArmRotation;
    }

    @SideOnly(Side.CLIENT)
    public Rotations getLeftLegRotation() {
        return this.leftLegRotation;
    }

    @SideOnly(Side.CLIENT)
    public Rotations getRightLegRotation() {
        return this.rightLegRotation;
    }


    @Override
    public boolean canBeCollidedWith() {
        return super.canBeCollidedWith() && !this.hasMarker();
    }


    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
    }


    @Override
    protected SoundEvent getFallSound(final int heightIn) {
        return SoundEvents.ENTITY_ARMORSTAND_FALL;
    }


    @Nullable
    protected SoundEvent getHurtSound(final DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ARMORSTAND_HIT;
    }


    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMORSTAND_BREAK;
    }


    public void onStruckByLightning(final EntityLightningBolt lightningBolt) {}


    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }


    @Override
    public void notifyDataManagerChange(final DataParameter<?> key) {
        if (EntityArmorStand.STATUS.equals(key)) {
            this.setSize(0.5f, 1.975f);
        }
        super.notifyDataManagerChange(key);
    }


    @Override
    public boolean attackable() {
        return false;
    }


    @Override
    public int lodLvl() {
        return ElcaMobs.ARMOR_ST.lodLvl(elcaMinRange);
    }

    static {
        DEFAULT_HEAD_ROTATION = new Rotations(0.0f, 0.0f, 0.0f);
        DEFAULT_BODY_ROTATION = new Rotations(0.0f, 0.0f, 0.0f);
        DEFAULT_LEFTARM_ROTATION = new Rotations(-10.0f, 0.0f, -10.0f);
        DEFAULT_RIGHTARM_ROTATION = new Rotations(-15.0f, 0.0f, 10.0f);
        DEFAULT_LEFTLEG_ROTATION = new Rotations(-1.0f, 0.0f, -1.0f);
        DEFAULT_RIGHTLEG_ROTATION = new Rotations(1.0f, 0.0f, 1.0f);
        STATUS = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.BYTE);
        HEAD_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
        BODY_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
        LEFT_ARM_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
        RIGHT_ARM_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
        LEFT_LEG_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
        RIGHT_LEG_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
        IS_RIDEABLE_MINECART = entity -> entity instanceof EntityMinecart && ((EntityMinecart)entity).canBeRidden();
    }
}