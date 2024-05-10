package net.minecraft.item;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ItemWrittenBook extends Item {
    public ItemWrittenBook() {
        this.setMaxStackSize(1);
    }


    public static boolean validBookTagContents(NBTTagCompound nbt) {
        if (!ItemWritableBook.isNBTValid(nbt)) {
            return false;
        } else if (!nbt.hasKey("title", 8)) {
            return false;
        } else {
            String s = nbt.getString("title");
            return s != null && s.length() <= 32 ? nbt.hasKey("author", 8) : false;
        }
    }


    public static int getGeneration(ItemStack book) {
        return book.getTagCompound().getInteger("generation");
    }


    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();
            String s = nbttagcompound.getString("title");
            if (!StringUtils.isNullOrEmpty(s)) {
                return s;
            }
        }
        return super.getItemStackDisplayName(stack);
    }


    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();
            String s = nbttagcompound.getString("author");

            if (!StringUtils.isNullOrEmpty(s)) {
                tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("book.byAuthor", s));
            }
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("book.generation." + nbttagcompound.getInteger("generation")));
        }
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            this.resolveContents(itemstack, playerIn);
        }

        playerIn.openBook(itemstack, handIn);
        playerIn.addStat(Objects.requireNonNull(StatList.getObjectUseStats(this)));
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }


    private void resolveContents(ItemStack stack, EntityPlayer player) {
        if (stack.getTagCompound() != null) {
            NBTTagCompound tag = stack.getTagCompound();

            if (!tag.getBoolean("resolved")) {
                tag.setBoolean("resolved", true);

                if (validBookTagContents(tag)) {
                    NBTTagList pages = tag.getTagList("pages", 8);

                    for (int i = 0; i < pages.tagCount(); ++i) {
                        String s = pages.getStringTagAt(i);
                        ITextComponent comp;

                        try {
                            comp = ITextComponent.Serializer.fromJsonLenient(s);
                            comp = TextComponentUtils.processComponent(player, comp, player);
                        } catch (Exception var9) {
                            comp = new TextComponentString(s);
                        }

                        pages.set(i, new NBTTagString(ITextComponent.Serializer.componentToJson(comp)));
                    }

                    tag.setTag("pages", pages);

                    if (player instanceof EntityPlayerMP && player.getHeldItemMainhand() == stack) {
                        //todo UNSAFE
                        //Текущий 0, номер 36 (player.inventory.currentItem) -> 36
                        //Текущий 8, номер 44
                        int cur = player.inventory.currentItem;
                        //Slot slot = player.openContainer.getSlotFromInventory(player.inventory, cur);
                        ((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(0, /*slot.slotNumber*/ cur+36, stack));
                    }
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}