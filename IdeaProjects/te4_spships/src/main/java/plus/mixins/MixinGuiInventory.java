package plus.mixins;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.client.camera.Camera;
import plus.client.SpGui;
import java.io.IOException;
import static net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen;
import static plus.client.SpGui.SP_BG;


@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer {
    @Shadow
    private float oldMouseX;
    @Shadow
    private float oldMouseY;
    @Final
    @Shadow
    private GuiRecipeBook recipeBookGui;
    @Shadow
    private boolean widthTooNarrow;
    @Shadow
    private GuiButtonImage recipeButton;
    @Shadow
    private boolean buttonClicked;

    private GuiButtonImage spButton;
    private final SpGui spGui = new SpGui(this);
    private boolean recipeBookInit = false;

    public MixinGuiInventory(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }


    /**
     * @author Te4hno
     * @reason add Sp gui
     */
    @Overwrite
    public void initGui() {

        buttonList.clear();
        if (mc.playerController.isInCreativeMode()) {
            mc.displayGuiScreen(new GuiContainerCreative(this.mc.player));
            return;
        }
        super.initGui();

        widthTooNarrow = false;
        if(!recipeBookInit) {
            recipeBookGui.func_194303_a(width, height, mc, widthTooNarrow,
                    ((ContainerPlayer) inventorySlots).craftMatrix);
            recipeBookInit = true;
        }

        spGui.onParentInit(width,height,widthTooNarrow);

        if(recipeBookGui.isVisible() && !SpGui.isVisible()) {
            guiLeft = recipeBookGui.updateScreenPosition(widthTooNarrow, width, xSize);
        } else {
            if(recipeBookGui.isVisible()){
                recipeBookGui.toggleVisibility();
            }
            guiLeft = spGui.updateScreenPosition(widthTooNarrow, width, xSize);
        }

        recipeButton = new GuiButtonImage(10,
                guiLeft + 104, height / 2 - 22,
                20, 18,
                178, 0, 19,
                INVENTORY_BACKGROUND
        );
        spButton = new GuiButtonImage(11,
                guiLeft + 104 + 24, height / 2 - 22,
                20, 18,
                148, 0, 19,
                SP_BG
        );

        buttonList.add(spButton);
        buttonList.add(recipeButton);
    }


    /**
     * @author Te4hno
     * @reason add Sp gui
     */
    @Overwrite
    protected void actionPerformed(GuiButton button){
        if(button.id == 10){
            this.recipeBookGui.initVisuals(this.widthTooNarrow, ((ContainerPlayer)this.inventorySlots).craftMatrix);
            this.recipeBookGui.toggleVisibility();
            this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            if(SpGui.isVisible()){
                spGui.toggleVisibility();
            }
        }
        if(button.id == 11){
            spGui.onParentInit(width,height,widthTooNarrow);
            spGui.toggleVisibility();
            this.guiLeft = spGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            if(recipeBookGui.isVisible()){
                recipeBookGui.toggleVisibility();
            }
        }
        if (button.id == 10 || button.id == 11) {
            this.buttonClicked = true;

            this.recipeButton.setPosition(this.guiLeft + 104, this.height / 2 - 22);
            this.spButton.setPosition(this.guiLeft + 104 + 24, this.height / 2 - 22);
        }
    }


    /**
     * @author Te4hno
     * @reason add Sp gui
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.hasActivePotionEffects = !this.recipeBookGui.isVisible();

        if(SpGui.isVisible()){
            spGui.render(mouseX,mouseY,partialTicks);
            GlStateManager.enableAlpha();
        }
        if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
            super.drawScreen(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, false, partialTicks);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
        if(SpGui.isVisible()){
            ItemStack item = spGui.getSelfSlotHovered();
            if(mc.player.inventory.getItemStack().isEmpty() && !item.isEmpty()){
                renderToolTip(item, mouseX, mouseY);
            }
        }
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }


    /**
     * @author Te4hno
     * @reason remove method
     */
    @Overwrite
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}


    /**
     * @author Te4hno
     * @reason add Sp gui
     */
    @Overwrite
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        Camera.ignoreOverridePlayerRender = true;
        drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) -
                this.oldMouseX, (float)(j + 75 - 50) - this.oldMouseY, this.mc.player);
        Camera.ignoreOverridePlayerRender = false;
    }


    @Override
    protected boolean hasClickedOutside(int n1, int n2,
                                        int n3, int n4) {
        boolean flag = n1 < n3 || n2 < n4 || n1 >= n3 + this.xSize || n2 >= n4 + this.ySize;
        return recipeBookGui.hasClickedOutside(n1, n2, guiLeft, guiTop, xSize, ySize)
                && flag &&
                spGui.hasClickedOutside(n1,n2,guiLeft,guiTop,xSize,ySize);
    }


    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci)
            throws IOException {
        ci.cancel();
        if (!this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
            if(!spGui.mouseClicked(mouseX, mouseY, mouseButton)){
                if (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) {
                    super.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }
}