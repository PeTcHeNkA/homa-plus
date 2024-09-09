package net.minecraft.util;

import net.minecraft.client.util.*;
import net.minecraft.item.crafting.*;
import javax.annotation.*;
import net.minecraft.network.play.server.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraftforge.common.crafting.*;
import java.util.*;


public class ServerRecipeBookHelper {
    public ServerRecipeBookHelper() {}

    @Deprecated
    public void func_194327_a(final EntityPlayerMP player, @Nullable final IRecipe recipe, final boolean flag) {
        placeRecipe(player, recipe, flag);
    }


    public static void placeRecipe(final EntityPlayerMP player, @Nullable final IRecipe recipe, final boolean flag) {
        if (recipe != null && player.getRecipeBook().isUnlocked(recipe)) {
            List<Slot> slots = player.openContainer.inventorySlots;
            final Container container = player.openContainer;
            InventoryCraftResult invResult = null;
            InventoryCrafting invCraft = null;
            if (container instanceof ContainerWorkbench) {
                invResult = ((ContainerWorkbench)container).craftResult;
                invCraft = ((ContainerWorkbench)container).craftMatrix;
            }
            else if (container instanceof ContainerPlayer) {
                invResult = ((ContainerPlayer)container).craftResult;
                invCraft = ((ContainerPlayer)container).craftMatrix;
            }
            else if (container instanceof IRecipeContainer) {
                invResult = ((IRecipeContainer)container).getCraftResult();
                invCraft = ((IRecipeContainer)container).getCraftMatrix();
            }
            if (invResult != null && invCraft != null && (checkA(player, invCraft) || player.isCreative())) {
                RecipeItemHelper helper = player.getServerWorld().getRecipeItemHelper();
                player.inventory.fillStackedContents(helper, false);
                invCraft.fillStackedContents(helper);
                if (helper.canCraft(recipe, null)) {
                    doCraft(helper, player, invCraft, invResult, recipe, slots, flag);
                } else {
                    takeItems(player, invCraft, invResult);
                    player.connection.netManager.sendPacketE(new SPacketPlaceGhostRecipe(player.openContainer.windowId, recipe));
                }
                player.inventory.markDirty();
            }
        }
    }


    private static void takeItems(EntityPlayerMP player, InventoryCrafting invCraft, InventoryCraftResult invResult) {
        final InventoryPlayer inventoryplayer = player.inventory;
        for (int i = 0; i < invCraft.getSizeInventory(); ++i) {
            final ItemStack itemstack = invCraft.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                while (itemstack.getCount() > 0) {
                    int j = inventoryplayer.storeItemStack(itemstack);
                    if (j == -1) {
                        j = inventoryplayer.getFirstEmptyStack();
                    }
                    final ItemStack itemstack2 = itemstack.copy();
                    itemstack2.setCount(1);
                    inventoryplayer.add(j, itemstack2);
                    invCraft.decrStackSize(i, 1);
                }
            }
        }
        invCraft.clear();
        invResult.clear();
    }


    private static void doCraft(RecipeItemHelper recipeItemHelper, EntityPlayerMP player, InventoryCrafting invCraft, InventoryCraftResult invResult,
                                IRecipe recipe, List<Slot> slots, boolean flag) {
        final boolean flag0 = recipe.matches(invCraft, player.world);
        final int i = recipeItemHelper.getBiggestCraftableStack(recipe, null);
        if (flag0) {
            boolean flag2 = true;
            for (int j = 0; j < invCraft.getSizeInventory(); ++j) {
                final ItemStack itemstack = invCraft.getStackInSlot(j);
                if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) > itemstack.getCount()) {
                    flag2 = false;
                }
            }
            if (flag2) {
                return;
            }
        }
        final int i2 = calcA(invCraft, i, flag, flag0);
        final IntList intlist = new IntArrayList();
        if (recipeItemHelper.canCraft(recipe, intlist, i2)) {
            int max = i2;
            for (final int k : intlist) {
                final int l = RecipeItemHelper.unpack(k).getMaxStackSize();
                if (l < max) {
                    max = l;
                }
            }
            if (recipeItemHelper.canCraft(recipe, intlist, max)) {
                takeItems(player, invCraft, invResult);
                takeItems2(player, invCraft, recipe, slots, max, intlist);
            }
        }
    }


    private static int calcA(InventoryCrafting invCraft, final int amount, final boolean flag, final boolean flag2) {
        int i = 1;
        if (flag) {
            i = amount;
        }
        else if (flag2) {
            i = 64;
            for (int j = 0; j < invCraft.getSizeInventory(); ++j) {
                final ItemStack itemstack = invCraft.getStackInSlot(j);
                if (!itemstack.isEmpty() && i > itemstack.getCount()) {
                    i = itemstack.getCount();
                }
            }
            if (i < 64) {
                ++i;
            }
        }
        return i;
    }


    private static void takeItems2(EntityPlayerMP player, InventoryCrafting invCraft, IRecipe recipe,
                                   List<Slot> slots, final int max, final IntList list) {
        int width = invCraft.getWidth();
        int height = invCraft.getHeight();
        if (recipe instanceof IShapedRecipe) {
            final IShapedRecipe shapedrecipes = (IShapedRecipe)recipe;
            width = shapedrecipes.getRecipeWidth();
            height = shapedrecipes.getRecipeHeight();
        }
        int j2 = 1;
        final IntListIterator iterator = list.iterator();
        for (int k = 0; k < width && height != k; ++k) {
            for (int l = 0; l < height; ++l) {
                if (width == l || !iterator.hasNext()) {
                    j2 += width - l;
                    break;
                }
                final Slot slot = slots.get(j2);
                final ItemStack itemstack = RecipeItemHelper.unpack(iterator.next());
                if (!itemstack.isEmpty()) {
                    for (int i = 0; i < max; ++i) {
                        takeItem(player, slot, itemstack);
                    }
                }
                ++j2;
            }
            if (!iterator.hasNext()) {
                break;
            }
        }
    }


    private static void takeItem(final EntityPlayerMP player, final Slot slot, final ItemStack stack) {
        final InventoryPlayer inventoryplayer = player.inventory;
        final int i = inventoryplayer.findSlotMatchingUnusedItem(stack);
        if (i != -1) {
            final ItemStack itemstack = inventoryplayer.getStackInSlot(i).copy();
            if (!itemstack.isEmpty()) {
                if (itemstack.getCount() > 1) {
                    inventoryplayer.decrStackSize(i, 1);
                }
                else {
                    inventoryplayer.removeStackFromSlot(i);
                }
                itemstack.setCount(1);
                if (slot.getStack().isEmpty()) {
                    slot.putStack(itemstack);
                }
                else {
                    slot.getStack().grow(1);
                }
            }
        }
    }


    private static boolean checkA(EntityPlayerMP player, InventoryCrafting invCraft) {
        final InventoryPlayer inventoryplayer = player.inventory;
        for (int i = 0; i < invCraft.getSizeInventory(); ++i) {
            final ItemStack itemstack = invCraft.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                int j = inventoryplayer.storeItemStack(itemstack);
                if (j == -1) {
                    j = inventoryplayer.getFirstEmptyStack();
                }
                if (j == -1) {
                    return false;
                }
            }
        }
        return true;
    }
}