package net.minecraft.item;

import net.minecraft.creativetab.*;
import net.minecraft.entity.player.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.inventory.*;
import com.google.common.collect.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.*;
import plus.fserver.api.IBlockEAccess;


public class ItemHoe extends Item {
    private final float speed;
    protected Item.ToolMaterial toolMaterial;

    public ItemHoe(final Item.ToolMaterial material) {
        this.toolMaterial = material;
        this.maxStackSize = 1;
        this.setMaxDamage(material.getMaxUses());
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.speed = material.getAttackDamage() + 1.0f;
    }


    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final ItemStack itemstack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
            return EnumActionResult.FAIL;
        }
        final int hook = ForgeEventFactory.onHoeUse(itemstack, player, world, pos);
        if (hook != 0) {
            return (hook > 0) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
        final IBlockState iblockstate = world.access().getBlockState(pos);
        final Block block = iblockstate.getBlock();
        if (facing != EnumFacing.DOWN && world.access().isAirBlock(pos.up())) {
            if (block == Blocks.GRASS || block == Blocks.GRASS_PATH) {
                this.setBlock(itemstack, player, world, pos, Blocks.FARMLAND.getDefaultState());
                return EnumActionResult.SUCCESS;
            }
            if (block == Blocks.DIRT) {
                switch ((BlockDirt.DirtType)iblockstate.getValue((IProperty)BlockDirt.VARIANT)) {
                    case DIRT: {
                        this.setBlock(itemstack, player, world, pos, Blocks.FARMLAND.getDefaultState());
                        return EnumActionResult.SUCCESS;
                    }
                    case COARSE_DIRT: {
                        this.setBlock(itemstack, player, world, pos, Blocks.DIRT.getDefaultState().withProperty((IProperty)BlockDirt.VARIANT, (Comparable)BlockDirt.DirtType.DIRT));
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }


    @Override
    public boolean hitEntity(final ItemStack stack, final EntityLivingBase target, final EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }


    protected void setBlock(final ItemStack stack, final EntityPlayer player, final World worldIn, final BlockPos pos, final IBlockState state) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
        if (!worldIn.isRemote) {
            worldIn.access().setBlockState(pos, state, 11);
            stack.damageItem(1, player);
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }


    public String getMaterialName() {
        return this.toolMaterial.toString();
    }


    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(final EntityEquipmentSlot equipmentSlot) {
        final Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ItemHoe.ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 0.0, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ItemHoe.ATTACK_SPEED_MODIFIER, "Weapon modifier", this.speed - 4.0f, 0));
        }
        return multimap;
    }
}