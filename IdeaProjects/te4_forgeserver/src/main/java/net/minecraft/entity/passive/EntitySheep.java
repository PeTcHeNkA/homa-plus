package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.FastDyeBlend;


public class EntitySheep extends EntityAnimal implements IShearable {
    private static final DataParameter<Byte> DYE_COLOR;
    private static final Map<EnumDyeColor, float[]> DYE_TO_RGB;
    private int sheepTimer;
    private EntityAIEatGrass entityAIEatGrass;


    private static float[] createSheepColor(EnumDyeColor p_192020_0_) {
        float[] afloat = p_192020_0_.getColorComponentValues();
        float f = 0.75F;
        return new float[]{afloat[0] * 0.75F, afloat[1] * 0.75F, afloat[2] * 0.75F};
    }


    @SideOnly(Side.CLIENT)
    public static float[] getDyeRgb(EnumDyeColor color) {
        return DYE_TO_RGB.get(color);
    }


    public EntitySheep(World world) {
        super(world);
        this.setSize(0.9F, 1.3F);
    }


    @Override
    protected void initEntityAI() {
        EntityAITasks tasks = this.tasks;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25));
        tasks.addTask(2, new EntityAIMate(this, 1.0));
        tasks.addTask(3, new EntityAITempt(this, 1.1, Items.WHEAT, false));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.1));
        tasks.addTask(5, entityAIEatGrass = new EntityAIEatGrass(this));
        tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
    }


    @Override
    protected void updateAITasks() {
        this.sheepTimer = this.entityAIEatGrass.getEatingGrassTimer();
        super.updateAITasks();
    }


    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote) {
            this.sheepTimer = Math.max(0, this.sheepTimer - 1);
        }
        super.onLivingUpdate();
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AbstractAttributeMap map = getAttributeMap();
        map.getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
        map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513);
    }


    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DYE_COLOR, (byte)0);
    }


    @Nullable
    protected ResourceLocation getLootTable() {
        if (this.getSheared()) {
            return LootTableList.ENTITIES_SHEEP;
        } else {
            switch (this.getFleeceColor()) {
                case WHITE:
                default:
                    return LootTableList.ENTITIES_SHEEP_WHITE;
                case ORANGE:
                    return LootTableList.ENTITIES_SHEEP_ORANGE;
                case MAGENTA:
                    return LootTableList.ENTITIES_SHEEP_MAGENTA;
                case LIGHT_BLUE:
                    return LootTableList.ENTITIES_SHEEP_LIGHT_BLUE;
                case YELLOW:
                    return LootTableList.ENTITIES_SHEEP_YELLOW;
                case LIME:
                    return LootTableList.ENTITIES_SHEEP_LIME;
                case PINK:
                    return LootTableList.ENTITIES_SHEEP_PINK;
                case GRAY:
                    return LootTableList.ENTITIES_SHEEP_GRAY;
                case SILVER:
                    return LootTableList.ENTITIES_SHEEP_SILVER;
                case CYAN:
                    return LootTableList.ENTITIES_SHEEP_CYAN;
                case PURPLE:
                    return LootTableList.ENTITIES_SHEEP_PURPLE;
                case BLUE:
                    return LootTableList.ENTITIES_SHEEP_BLUE;
                case BROWN:
                    return LootTableList.ENTITIES_SHEEP_BROWN;
                case GREEN:
                    return LootTableList.ENTITIES_SHEEP_GREEN;
                case RED:
                    return LootTableList.ENTITIES_SHEEP_RED;
                case BLACK:
                    return LootTableList.ENTITIES_SHEEP_BLACK;
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte p_70103_1_) {
        if (p_70103_1_ == 10) {
            this.sheepTimer = 40;
        } else {
            super.handleStatusUpdate(p_70103_1_);
        }
    }


    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        player.getHeldItem(hand);
        return super.processInteract(player, hand);
    }


    public static void registerFixesSheep(DataFixer p_189802_0_) {
        EntityLiving.registerFixesMob(p_189802_0_, EntitySheep.class);
    }


    @SideOnly(Side.CLIENT)
    public float getHeadRotationPointY(float p_70894_1_) {
        if (this.sheepTimer <= 0) {
            return 0.0F;
        } else if (this.sheepTimer >= 4 && this.sheepTimer <= 36) {
            return 1.0F;
        } else {
            return this.sheepTimer < 4 ? ((float)this.sheepTimer - p_70894_1_) / 4.0F : -((float)(this.sheepTimer - 40) - p_70894_1_) / 4.0F;
        }
    }


    @SideOnly(Side.CLIENT)
    public float getHeadRotationAngleX(float p_70890_1_) {
        if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
            float f = ((float)(this.sheepTimer - 4) - p_70890_1_) / 32.0F;
            return 0.62831855F + 0.2199115F * MathHelper.sin(f * 28.7F);
        } else {
            return this.sheepTimer > 0 ? 0.62831855F : this.rotationPitch * 0.017453292F;
        }
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("Sheared", this.getSheared());
        nbt.setByte("Color", (byte)this.getFleeceColor().getMetadata());
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.setSheared(nbt.getBoolean("Sheared"));
        this.setFleeceColor(EnumDyeColor.byMetadata(nbt.getByte("Color")));
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SHEEP_AMBIENT;
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.ENTITY_SHEEP_HURT;
    }


    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SHEEP_DEATH;
    }


    @Override
    protected void playStepSound(BlockPos p_180429_1_, Block p_180429_2_) {
        this.playSound(SoundEvents.ENTITY_SHEEP_STEP, 0.15F, 1.0F);
    }


    public EnumDyeColor getFleeceColor() {
        return EnumDyeColor.byMetadata(this.dataManager.get(DYE_COLOR) & 15);
    }


    public void setFleeceColor(EnumDyeColor p_175512_1_) {
        byte b0 = this.dataManager.get(DYE_COLOR);
        this.dataManager.set(DYE_COLOR, (byte)(b0 & 240 | p_175512_1_.getMetadata() & 15));
    }


    public boolean getSheared() {
        return (this.dataManager.get(DYE_COLOR) & 16) != 0;
    }


    public void setSheared(boolean b) {
        byte b0 = this.dataManager.get(DYE_COLOR);
        if (b) {
            this.dataManager.set(DYE_COLOR, (byte)(b0 | 16));
        } else {
            this.dataManager.set(DYE_COLOR, (byte)(b0 & -17));
        }
    }


    public static EnumDyeColor getRandomSheepColor(Random rand) {
        int i = rand.nextInt(100);
        if (i < 5) {
            return EnumDyeColor.BLACK;
        } else if (i < 10) {
            return EnumDyeColor.GRAY;
        } else if (i < 15) {
            return EnumDyeColor.SILVER;
        } else if (i < 18) {
            return EnumDyeColor.BROWN;
        } else {
            return rand.nextInt(500) == 0 ? EnumDyeColor.PINK : EnumDyeColor.WHITE;
        }
    }


    @Override
    public EntitySheep createChild(EntityAgeable p_90011_1_) {
        EntitySheep entitysheep = (EntitySheep)p_90011_1_;
        EntitySheep entitysheep1 = new EntitySheep(this.world);
        entitysheep1.setFleeceColor(this.getDyeColorMixFromParents(this, entitysheep));
        return entitysheep1;
    }


    @Override
    public void eatGrassBonus() {
        this.setSheared(false);
        if (this.isChild()) {
            this.addGrowth(60);
        }
    }

    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance p_180482_1_, @Nullable IEntityLivingData p_180482_2_) {
        p_180482_2_ = super.onInitialSpawn(p_180482_1_, p_180482_2_);
        this.setFleeceColor(getRandomSheepColor(this.world.rand));
        return p_180482_2_;
    }


    @Override
    public boolean isShearable(ItemStack p_isShearable_1_, IBlockAccess p_isShearable_2_, BlockPos p_isShearable_3_) {
        return !this.getSheared() && !this.isChild();
    }


    @Override
    public List<ItemStack> onSheared(ItemStack p_onSheared_1_, IBlockAccess p_onSheared_2_, BlockPos p_onSheared_3_, int p_onSheared_4_) {
        this.setSheared(true);
        int i = 1 + this.rand.nextInt(3);
        List<ItemStack> ret = new ArrayList(i);

        Item item = Item.getItemFromBlock(Blocks.WOOL);

        for(int j = 0; j < i; ++j) {
            ret.add(new ItemStack(item, 1, this.getFleeceColor().getMetadata()));
        }

        this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
        return ret;
    }


    @Override
    public float getEyeHeight() {
        return 0.95F * this.height;
    }


    static {
        DYE_COLOR = EntityDataManager.createKey(EntitySheep.class, DataSerializers.BYTE);
        DYE_TO_RGB = Maps.newEnumMap(EnumDyeColor.class);
        EnumDyeColor[] var0 = EnumDyeColor.values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            EnumDyeColor enumdyecolor = var0[var2];
            DYE_TO_RGB.put(enumdyecolor, createSheepColor(enumdyecolor));
        }

        DYE_TO_RGB.put(EnumDyeColor.WHITE, new float[]{0.9019608F, 0.9019608F, 0.9019608F});
    }


    private EnumDyeColor getDyeColorMixFromParents(EntitySheep father, EntitySheep mother) {
        EnumDyeColor i = father.getFleeceColor();
        EnumDyeColor j = mother.getFleeceColor();
        final EnumDyeColor blended = FastDyeBlend.getBlendedColor(i, j);

        if (blended != null) {
            return blended;
        } else {
            return father.world.rand.nextBoolean() ? i : j;
        }
    }
}
