package plus.client.invlib;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import plus.common.etc.DSlot;

import static plus.utls.MathUtils.isBetween;
//import plus.inv.AInventoryClientRenderer;
//import plus.invlib.render.RItemFrame;
//import plus.invlib.render.RString;


public class BigSlotRenderer {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    public final DSlot slot;
    private final InventoryEffectRenderer gui;

    public BigSlotRenderer(InventoryEffectRenderer gui, DSlot slot) {
        this.gui = gui;
        this.slot = slot;
    }


    public static class Set{
        private final BigSlotRenderer[] slots;
        private boolean enabled = true;
        private ItemStack hovered = ItemStack.EMPTY;

        public Set(InventoryEffectRenderer gui, DSlot... dSlots){
            slots = new BigSlotRenderer[dSlots.length];
            for(int i=0;i<slots.length;i++){
                slots[i] = new BigSlotRenderer(gui, dSlots[i]);
            }
        }


        public void draw(int x, int y, int mouseX, int mouseY){
            if(!enabled)return;
            hovered = ItemStack.EMPTY;
            for(BigSlotRenderer r:slots){
                if(r.draw(x, y, mouseX, mouseY)){
                    hovered = r.slot.getStack();
                }
            }
        }


        public ItemStack getHovered(){
            return hovered;
        }


        public boolean handleClick(int mouseX, int mouseY, int mouseButton){
            if(!enabled)return false;
            for(BigSlotRenderer r:slots){
                if(r.handleClick(mouseX, mouseY, mouseButton)){
                    return true;
                }
            }
            return false;
        }


        public void setEnabled(boolean enabled){
            this.enabled = enabled;
        }
    }


    protected boolean hovered = false;
    public final boolean draw(int x, int y, int mouseX, int mouseY){
        GlStateManager.pushMatrix();

        GlStateManager.translate(x+slot.xPos,y+slot.yPos,-200);
        GlStateManager.scale(1.5,1.5,1.5);
        boolean result = false;
        onDraw(x, y);
        if(isInBound(  mouseX-x, mouseY-y )){
            hovered = true;
            onHover(x, y);
            result = true;
        } else {
            hovered = false;
            onNormal(x, y);
        }

        GlStateManager.popMatrix();
        return result;
    }

    private static final int shiftX = 1;
    private static /*final*/ int shiftY = -1;
    private static final int sSize = 26;

    public boolean handleClick(int mouseX, int mouseY, int mouseButton){
        if(hovered) {
            mc.playerController.windowClick(gui.inventorySlots.windowId,
                    slot.slotNumber, mouseButton, ClickType.PICKUP, mc.player);
        }
        return hovered;
    }

    public boolean isInBound(int mouseX, int mouseY){
        return     isBetween(mouseY, slot.yPos+sSize+shiftY, slot.yPos+shiftY)
                && isBetween(mouseX, slot.xPos+sSize+shiftX, slot.xPos+shiftX);
    }


    protected void onHover(int x, int y) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        GlStateManager.colorMask(true, true, true, false);

        RenderUtls.drawGradientRect(0, 0, 16, 16, -2130706433, -2130706433, 0);

        GlStateManager.colorMask(true, true, true, true);
        //GlStateManager.enableLighting();
        //GlStateManager.enableDepth();
    }


    protected void onNormal(int x, int y) {
        //System.out.println("NOTMA");
    }



    public void onDraw(int x, int y){
        onDraw(slot.getStack(), x, y);
    }
//    public void onDraw(){
//        ItemStack item = slot.getStack();
//        boolean lighting = false;
//
//        boolean flag1 = slot == gui.clickedSlot && !gui.draggedStack.isEmpty() && !gui.isRightMouseClick;
//
//        ItemStack itemstack1 = mc.player.inventory.getItemStack();
//        String s = null;
//
//        if (flag1 && !item.isEmpty()) {
//            item = item.copy();
//            item.setCount(item.getCount() / 2);
//        } else if (
//                gui.dragSplitting && gui.dragSplittingSlots.contains(slot)
//                        && !itemstack1.isEmpty()) {
//            if (gui.dragSplittingSlots.size() == 1) {
//                return;
//            }
//
//            if (Container.canAddItemToSlot(slot, itemstack1, true)
//                    && gui.inventorySlots.canDragIntoSlot(slot)) {
//
//                item = itemstack1.copy();
//                lighting = true;
//                Container.computeStackSize(gui.dragSplittingSlots, gui.dragSplittingLimit,
//                        item, slot.getStack().isEmpty() ? 0 : slot.getStack().getCount());
//                int k = Math.min(item.getMaxStackSize(), slot.getItemStackLimit(item));
//
//                if (item.getCount() > k)
//                {
//                    s = TextFormatting.YELLOW.toString() + k;
//                    item.setCount(k);
//                }
//            }
//            else {
//                gui.dragSplittingSlots.remove(slot);
//                gui.updateDragSplitting();
//            }
//        }
//
//        //gui.setZLevel(100);
//        RItemFrame.itemRender.zLevel = 100.0F;
//
//        if (item.isEmpty()) {
//            TextureAtlasSprite textureatlassprite = slot.getBackgroundSprite();
//
//            if (textureatlassprite != null) {
//                GlStateManager.disableLighting();
//                mc.getTextureManager().bindTexture(slot.getBackgroundLocation());
//                GlStateManager.enableLighting();
//            }
//        }
//
//        if (!flag1) {
//            onDraw(item, s, lighting);
//        }
//
//        RItemFrame.itemRender.zLevel = 0.0F;
//        //gui.setZLevel(0);
//    }


    public void onDraw(ItemStack item, int x, int y){
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();

        RItemFrame.itemRender.zLevel = 200;
        RItemFrame.itemRender.renderItemAndEffectIntoGUI(mc.player, item, 0, 0);
        RItemFrame.itemRender.renderItemOverlayIntoGUI(RString.fontRenderer, item, 0, 0, null);
        RItemFrame.itemRender.zLevel = 0;

        //GlStateManager.enableLighting();
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        //GlStateManager.enableLighting();

        //GlStateManager.disableLighting();
    }
}