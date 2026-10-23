package plus.invlib.slot;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import plus.inv.AInventoryClientRenderer;
import plus.invlib.render.RItemFrame;
import plus.invlib.render.RString;


public class AbcSlotRenderable {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    public final SSlot slot;
    private final AInventoryClientRenderer gui;

    boolean needResize = false;
    public boolean enabled = false;

    public AbcSlotRenderable(AInventoryClientRenderer gui, SSlot slot) {
        this.gui = gui;
        this.slot = slot;
    }


    public final boolean draw(int mouseX, int mouseY){
        boolean result = false;
        onDraw();
        if(isInBound(mouseX, mouseY)){
            onHover();
            result = true;
        } else onNormal();

        return result;
    }


    public boolean isInBound(int mouseX, int mouseY){
        return isBetween(mouseY, slot.yPos, slot.yPos+16)
                && isBetween(mouseX, slot.xPos, slot.xPos+16);
    }


    protected void onHover() {
        //System.out.println("HOVER");
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        int j1 = slot.xPos;
        int k1 = slot.yPos;
        GlStateManager.colorMask(true, true, true, false);
        gui.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }


    protected void onNormal() {
        //System.out.println("NOTMA");
    }


    public void onDraw(){
//        int i = slot.xPos;
//        int j = slot.yPos;
        ItemStack item = slot.getStack();
        boolean lighting = false;

        boolean flag1 = slot == gui.clickedSlot && !gui.draggedStack.isEmpty() && !gui.isRightMouseClick;

        ItemStack itemstack1 = mc.player.inventory.getItemStack();
        String s = null;

        if (flag1 && !item.isEmpty()) {
            item = item.copy();
            item.setCount(item.getCount() / 2);
        } else if (
                gui.dragSplitting && gui.dragSplittingSlots.contains(slot)
                && !itemstack1.isEmpty()) {
            if (gui.dragSplittingSlots.size() == 1) {
                return;
            }

            if (Container.canAddItemToSlot(slot, itemstack1, true)
                    && gui.inventorySlots.canDragIntoSlot(slot)) {

                item = itemstack1.copy();
                lighting = true;
                Container.computeStackSize(gui.dragSplittingSlots, gui.dragSplittingLimit,
                        item, slot.getStack().isEmpty() ? 0 : slot.getStack().getCount());
                int k = Math.min(item.getMaxStackSize(), slot.getItemStackLimit(item));

                if (item.getCount() > k)
                {
                    s = TextFormatting.YELLOW.toString() + k;
                    item.setCount(k);
                }
            }
            else {
                gui.dragSplittingSlots.remove(slot);
                gui.updateDragSplitting();
            }
        }

        gui.setZLevel(100);
        RItemFrame.itemRender.zLevel = 100.0F;

        if (item.isEmpty())
        {
            TextureAtlasSprite textureatlassprite = slot.getBackgroundSprite();

            if (textureatlassprite != null)
            {
                GlStateManager.disableLighting();
                mc.getTextureManager().bindTexture(slot.getBackgroundLocation());
                //lighting = true;
                //gui.drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                //flag1 = true;
            }
        }

        if (!flag1) {
            onDraw(item, s, lighting);
        }

        RItemFrame.itemRender.zLevel = 0.0F;
        gui.setZLevel(0);
    }


    public void onDraw(ItemStack item, String s, boolean lighting){
        int i = slot.xPos;
        int j = slot.yPos;
        if (lighting) {
            Gui.drawRect(i, j, i + 16, j + 16, -2130706433);
        }
        GlStateManager.enableDepth();
        RItemFrame.itemRender.renderItemAndEffectIntoGUI(mc.player, item, i, j);
        RItemFrame.itemRender.renderItemOverlayIntoGUI(RString.fontRenderer, item, i, j, s);
    }


    protected static boolean isBetween(double check, double min, double max){
        return check>=min && check <= max;
    }
}