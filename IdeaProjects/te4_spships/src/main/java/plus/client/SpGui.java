package plus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import plus.SpMain;
import plus.client.buttons.Button;
import plus.client.buttons.ButtonSet;
import plus.client.camera.Camera;
import plus.client.entity.SpModelManager;
import plus.client.invlib.BigSlotRenderer;
import plus.common.network.spship.SpModePacket;
import plus.common.etc.DSlot;
import plus.common.player.PlayerSpship;

import static plus.client.entity.LivingBlockRenderer.renderItemModel;
import static plus.utls.MathUtils.*;


@SideOnly(Side.CLIENT)
public class SpGui extends Gui {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final ResourceLocation SP_BG =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/sp_tex.png");
    public static final ResourceLocation RECIPE_BOOK =
            new ResourceLocation("textures/gui/recipe_book.png");

    protected static boolean visible;
    private static SpGui INST;
    private static final float scale = 30;
    private static float angleY = 12.5f;
    private static float angleX = 180-12.5f;

    private int xOffset;
    private int width;
    private int height;

    private boolean prevOnSp = false;
    private int prevX;
    private int prevY;

    private final ButtonSet buttons = new ButtonSet();
    private final GuiInventory parent;

    private final BigSlotRenderer.Set slotRender;

    public SpGui(InventoryEffectRenderer inv) {
        INST = this;
        parent = (GuiInventory) inv;
        buttons.add(new Button(new ItemStack(Blocks.STONE)));
        buttons.add(new Button(new ItemStack(Blocks.PUMPKIN)));

        slotRender = new BigSlotRenderer.Set(inv, getPrnSlotD(46,47,48,49,50,51,52,53,54,55));

        //slotRender = new BigSlotRenderer(inv, (DSlot) inv.inventorySlots.getSlot(46));
    }

    public void onParentInit(int width, int height, boolean widthToNarrow){
        this.width = width;
        this.height = height;
        this.xOffset = widthToNarrow ? 0 : 86;
    }


    public ItemStack getSelfSlotHovered(){
        if(lastOnSp){
            lastOnSp = false;
            return getPrnSlotD(56).getStack();
        }
        return slotRender.getHovered();
    }


    public void render(int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            GlStateManager.pushMatrix();
            //GlStateManager.translate(0.0F, 0.0F, 10.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(SP_BG);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int lX = (this.width - 147) / 2 - this.xOffset;
            int lY = (this.height - 166) / 2;
            this.drawTexturedModalRect(lX, lY, 0, 0, 147, 166);

            boolean clicked = Mouse.isButtonDown(0);

            switch (buttons.getActiveIndex()){
                case 0:
                    renderMain(lX, lY, mouseX, mouseY, clicked);
                    break;
                case 1:
                    slotRender.draw(lX, lY, mouseX, mouseY);
                    //renderToolTip(hovRecepieItem, mouseX, mouseY);
                    //GlStateManager.translate(0,0,200);
                    break;
            }

            GlStateManager.disableLighting();

            buttons.renderAll(lX, lY);

            renderSp(lX, lY, mouseX, mouseY);

            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            handleRotationSp(lX-mouseX, lY-mouseY, clicked);
        }
    }
    private boolean curMode = Camera.getSelfSp().isSpshipMode();
    private long lastClickTime = 0;
    private void sendToggleSp(){
        long cur = System.currentTimeMillis();
        if(cur-lastClickTime<5000)return;
        lastClickTime = cur;
        curMode = !curMode;
        ((PlayerSpship)Minecraft.getMinecraft().player).setSpMode(curMode);
        new SpModePacket(curMode).sendToServer();
    }


    private boolean toggleSpModeButtonHovered = false;

    private void renderMain(int x, int y, int mouseX, int mouseY, boolean clicked){
        this.drawTexturedModalRect(x, y+95, 0, 166, 147, 63);
        toggleSpModeButtonHovered = isBetween(mouseX, x+139, x+6) && isBetween(mouseY, y+158,y+140);
        if(toggleSpModeButtonHovered){
            this.drawTexturedModalRect(x+7, y+140, 7, 229, 132, 18);
//            if(clicked){
//                sendToggleSp();
//            }
        }
    }

    private boolean lastOnSp = false;

    private boolean onSp(int x, int y){
        lastOnSp = isBetween(x, -6, -139) && isBetween(y, -6, -79);
        return lastOnSp;
    }


    private void handleRotationSp(int x, int y, boolean active){
        if((onSp(x, y) || prevOnSp) && active) {
            if (x != prevX) {
                angleX += 0.5 * (x - prevX);
            }
            if (y != prevY) {
                angleY += 0.5 * (y - prevY);
            }
            prevOnSp = true;
        } else {
            prevOnSp = false;
        }

        prevX = x;
        prevY = y;
    }


    public static void resetDefaultAngle(){
        angleY = 12.5f;
        angleX = 180-12.5f;
    }


    public void renderSp(int x, int y, int mx, int my){
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.translate(x+74,y+45,100);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180,0,0,1);
        RenderHelper.enableStandardItemLighting();

        GlStateManager.rotate(angleX, 0, 1, 0);
        GlStateManager.rotate(angleY, 1, 0, 0);

        ItemStack item = getPrnSlotD(56).getStack();
        if(!item.isEmpty()){
            renderItemModel(item);
        } else {//
            renderItemModel(
                    SpModelManager.getSpModelByID(
                            ((PlayerSpship) Minecraft.getMinecraft().player).getSpModelId())
            );
        }//

        GlStateManager.disableColorMaterial();

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    public void toggleVisibility() {
        visible = !visible;
    }


    public static boolean isVisible() {
        return visible;
    }


    private DSlot[] getPrnSlotD(int... ints){
        DSlot[] slots = new DSlot[ints.length];
        for(int i=0;i<slots.length;i++){
            slots[i] = getPrnSlotD(ints[i]);
        }
        return slots;
    }

    private DSlot getPrnSlotD(int i){
        return (DSlot) parent.inventorySlots.getSlot(i);
    }


    public static void setVisible(boolean visible) {
        SpGui.visible = visible;
        if(visible){
            resetDefaultAngle();
        }
    }


    public int updateScreenPosition(boolean b, int width, int xSize) {
        int i;
        if (isVisible() && !b) {
            i = 177 + (width - xSize - 200) / 2;
        } else {
            i = (width - xSize) / 2;
        }
        return i;
    }


    public boolean hasClickedOutside(int n1, int n2, int guiLeft,
                                     int guiTop, int xSize, int ySize) {
        if (!visible) {
            return true;
        } else {
            boolean flag = n1 < guiLeft || n2 < guiTop || n1 >= guiLeft + xSize || n2 >= guiTop + ySize;
            boolean flag1 = guiLeft - 147 < n1 && n1 < guiLeft && guiTop < n2 && n2 < guiTop + ySize;

            int lX = (this.width - 147) / 2 - this.xOffset;
            int lY = (this.height - 166) / 2;

            boolean flag2 = buttons.getHoverIndex(n1, n2,lX,lY)==-1;


            return flag && !flag1 && flag2;
        }
    }


    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(!visible)return false;
        int lX = (this.width - 147) / 2 - this.xOffset;
        int lY = (this.height - 166) / 2;
        boolean flag = isBetween(mouseX, lX+147, lX) && isBetween(mouseY, lY+166,lY);

        if(mouseButton == 1 && onSp(lX-mouseX,lY-mouseY) &&
                !mc.player.inventory.getItemStack().isEmpty()){
            mc.playerController.windowClick(parent.inventorySlots.windowId,
                    56, mouseButton, ClickType.PICKUP, mc.player);
            return true;
        }

        switch (buttons.getActiveIndex()){
            case 0:
                if(toggleSpModeButtonHovered){
                    sendToggleSp();
                }
                break;
            case 1:
                slotRender.handleClick(mouseX, mouseY, mouseButton);
                break;
        }

        buttons.onClick(mouseX, mouseY, lX, lY+3);

        return flag;
    }
}