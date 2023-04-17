package plus.client.mashine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import plus.SpMain;
import plus.client.buttons.Button;
import plus.client.buttons.ButtonSet;
import plus.common.etc.VISlot;
import plus.common.mashine.item.ItemMssCapacitor;
import plus.common.mashine.item.ReactCore;
import plus.common.mashine.item.TehSubcraftItem;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.mashine.lincker.SpCraftContainer;
import plus.common.mashine.lincker.SpReceptBook;
import plus.common.spship.item.ItemCorpus;
import plus.utls.MathUtils;
import java.io.IOException;


@SideOnly(Side.CLIENT)
public class GuiSpCraft extends GuiContainer {
    public static final RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
    private static final ResourceLocation SP_CRAFT_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/sp_craftplus.png");
    private static final ResourceLocation SP_CRAFT_RECEPT_GUI =
            new ResourceLocation(SpMain.MOD_ID,"textures/gui/recept_book2.png");

    private final ButtonSet buttons = new ButtonSet();
    private final VisualRCPage[] pages = new VisualRCPage[]{
            new VisualRCPage(new ItemStack(TehSubcraftItem.MICRO_NRM),1, 2, 3, 18),
            new VisualRCPage(new ItemStack(ItemMssCapacitor.BATTERY_L2),new int[][]{{4},{5},{6,7},{8,9}}),
            new VisualRCPage(new ItemStack(ReactCore.CORE_L2), new int[][]{{10,11},{12,13}, {14,15,16,17}}),
            new VisualRCPage(ItemCorpus.SP_MODEL.createSimple(17), new int[][]{{20},{19}})
    };

    private ItemStack hovRecepieItem = ItemStack.EMPTY;
    private static boolean renderRecepts = false;
    private boolean toggleBtnHovered = false;
    private int prevMx = 0;
    private boolean lastActive = false;
    private int prevShift = 50;
    private int shiftStick = 50;

    public GuiSpCraft(InventoryPlayer playerInv, IInventory spCraftInv) {
        super(new SpCraftContainer(playerInv, spCraftInv){
            @Override
            public void onCallConstruct() {
                ItemStack invb = new ItemStack(Items.APPLE);
                if(1==1){
                    return;
                }
                //InventoryBasic invb = new InventoryBasic("", false, 2);
                int x = -135;
                int y = 16;
                this.addSlotToContainer(new VISlot(invb, 0, x+18*0, y));
                this.addSlotToContainer(new VISlot(invb, 1, x+18*1, y));
                this.addSlotToContainer(new VISlot(invb, 1, x+18*2, y));

                this.addSlotToContainer(new VISlot(invb, 1, x+18*3, y));
                this.addSlotToContainer(new VISlot(invb, 1, x+18*4, y));
                this.addSlotToContainer(new VISlot(invb, 1, x+18*5+14, y));


            }
        });
//        this.playerInventory = playerInv;
//        this.craftInventory = spCraftInv;
    }


    private SpCraftContainer inv(){
        return (SpCraftContainer) this.inventorySlots;
    }


    @Override
    public void initGui() {
        super.initGui();
        renderRecepts = !renderRecepts;
        toggleReceptVisable();
    }


//    VisualRCPage newPage(ItemStack ico, String code){
//        String[] raw = code.replace(" ","").split(",");
//        int[][] data = new int[raw.length][];
//        for(int i=0;i<raw.length;i++){
//            String elm = raw[i];
//            if(elm.contains("/")){
//                String[] raw0 = elm.split("/");
//                int[] data0 = new int[raw0.length];
//                for(int i2=0;i2<raw0.length;i2++){
//                    data0[i2] = Integer.parseInt(raw0[i2]);
//                }
//                data[i] = data0;
//            } else {
//                data[i] = new int[]{Integer.parseInt(elm)};
//            }
//
//        }
//        return new VisualRCPage(ico, data);
//    }


    class VisualRCPage {
        private final ReceptCont[] recepts;

        VisualRCPage(ItemStack ico, int... ids){
            buttons.add(new Button(ico));
            recepts = new ReceptCont[ids.length];
            for(int i=0;i<ids.length;i++){
                recepts[i] = new ReceptPreview(ids[i]);
            }
        }
        VisualRCPage(ItemStack ico, int[]... ids){
            buttons.add(new Button(ico));
            recepts = new ReceptCont[ids.length];
            for(int i=0;i<ids.length;i++){
                int[] ids0 = ids[i];
                if(ids0.length==1){
                    recepts[i] = new ReceptPreview(ids0[0]);
                } else {
                    recepts[i] = new AnimRecept(ids0);
                }
            }
        }


        private void drawAll(int i, int j, int mx, int my){
            int shift = 0;
            for(ReceptCont recept:recepts){
                drawCraft(i, j+shift, recept.getCur(), mx, my);
                shift+=27;
            }
        }
    }

    class AnimRecept implements ReceptCont{
        private final ReceptPreview[] recepts;
        public AnimRecept(int[] ids) {
            recepts = new ReceptPreview[ids.length];
            for(int i=0;i<ids.length;i++){
                recepts[i] = new ReceptPreview(ids[i]);
            }
        }

        private ReceptPreview getCur(int i){
            return recepts[i%recepts.length];
        }

        public ReceptPreview getCur() {
            ReceptPreview result = getCur(animPhase);
            int limit = 10;
            while (result.result.isEmpty()){
                result = getCur(animPhase);
                if(limit--<1)break;
            }
            return result;
        }
    }


    interface ReceptCont{
        ReceptPreview getCur();
    }


    class ReceptPreview implements ReceptCont{
        public final ItemStack[] items;
        private final ReseptKey key;
        private ItemStack result;
        private int prevPow = -1;
        private int prevShift = 50;

        public ReceptPreview(int id){
            key = SpReceptBook.INST.getById(id);
            items = key.getItems();
            result = getResult();
        }


        public ItemStack getResult(){
            int power = inv().getPower();
            if(shiftStick==prevShift){
                if(prevPow==power){
                    return result;
                } else {
                    prevPow = power;
                }
            } else {
                prevShift = shiftStick;
            }
            result = SpReceptBook.INST.getResult(key).calcResult(key, shiftStick, power);
            return result;
        }

        @Override
        public ReceptPreview getCur() {
            return this;
        }
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(!hovRecepieItem.isEmpty()) {
            renderToolTip(hovRecepieItem, mouseX, mouseY);
        } else {
            this.renderHoveredToolTip(mouseX, mouseY);
        }
    }

    private int animPhase = 1;
    private long lastTickTime = 0;
    private void tickAnim(){
        long cur = System.currentTimeMillis();
        if(cur-lastTickTime>2500){
            lastTickTime =  cur;
            animPhase++;
        }
    }

    protected void drawRecepts(int i, int j, int mouseX, int mouseY){
        hovRecepieItem = ItemStack.EMPTY;
        i -= 148;
        mc.getTextureManager().bindTexture(SP_CRAFT_RECEPT_GUI);
        drawTexturedModalRect(i, j, 0, 0, 147, 166);
        buttons.renderAll(i, j);

        tickAnim();

        pages[buttons.getActiveIndex()].drawAll(i, j, mouseX, mouseY);
    }


    protected void drawCraft(int i, int j, ReceptPreview rc, int mx, int my){
        mc.getTextureManager().bindTexture(SP_CRAFT_RECEPT_GUI);
        this.drawTexturedModalRect(i+10, j+12, 0, 166, 127, 25);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();

        int xA = 12;
        int yA = 16;

        drawFantomItem(i+xA+18*0, j+yA, rc.items[0], mx, my);
        drawFantomItem(i+xA+18*1, j+yA, rc.items[1], mx, my);
        drawFantomItem(i+xA+18*2, j+yA, rc.items[2], mx, my);
        drawFantomItem(i+xA+18*3, j+yA, rc.items[3], mx, my);
        drawFantomItem(i+xA+18*4, j+yA, rc.items[4], mx, my);
        drawFantomItem(i+xA+18*5+14, j+yA, rc.getResult(), mx, my);

        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
    }


    protected void drawFantomItem(int i, int j, ItemStack item, int mX, int mY){
        itemRender.renderItemAndEffectIntoGUI(item, i, j);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, item, i, j, null);
        if(
                MathUtils.isBetween(mX, i+16, i) &&
                MathUtils.isBetween(mY, j+16, j)) {
            hovRecepieItem = item;
        }
    }


    protected void drawToggleBtn(int i, int j, int mouseX, int mouseY){
        if(MathUtils.isBetween(mouseX, i+7+20,i+7) &&
                MathUtils.isBetween(mouseY, j+12+18,j+12)){
            toggleBtnHovered = true;
            this.drawTexturedModalRect(i+7, j+12, 176, 24, 20, 18);
        } else {
            toggleBtnHovered = false;
        }
    }


    private void toggleReceptVisable(){
        renderRecepts = !renderRecepts;
        guiLeft = ((width - xSize) / 2) + (renderRecepts?77:0);
    }


    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        if(renderRecepts){
            i+=77;
        }

        this.mc.getTextureManager().bindTexture(SP_CRAFT_GUI);
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        GlStateManager.translate(0,0,50);

        boolean isStickHov = isHoveredStick(i+shiftStick, j, mouseX, mouseY);
        boolean clicked = isClicked();
        if(clicked && (lastActive || isStickHov)){
            int dX = (int) MathUtils.range(mouseX, prevMx);
            if(dX!=0) {
                shiftStick = shiftStick - dX;
                if (shiftStick < 0) shiftStick = 0;
                else if (shiftStick > 100) {
                    shiftStick = 100;
                }
            } else {
                if(prevShift != shiftStick){
                    sysClick(20000 + shiftStick);
                    prevShift = shiftStick;
                }
            }
            lastActive = true;
        } else {
            if(prevShift != shiftStick){
                sysClick(20000 + shiftStick);
                prevShift = shiftStick;
            }
            lastActive = false;
        }

        drawStick(i+shiftStick, j, isStickHov || lastActive);
        drawToggleBtn(i, j, mouseX, mouseY);

        if(renderRecepts) {
            drawRecepts(i, j, mouseX, mouseY);
        }

        prevMx = mouseX;
    }


    protected boolean isClicked(){
        return Mouse.isButtonDown(0);
    }


    private void sysClick(int num){
        this.mc.playerController.windowClick(
                this.inventorySlots.windowId, num, 0, ClickType.PICKUP, this.mc.player
        );
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(toggleBtnHovered){
            toggleReceptVisable();
        } else {
            if(renderRecepts) {
                int lX = (this.width - this.xSize) / 2;
                int lY = (this.height - this.ySize) / 2;
                buttons.onClick(mouseX, mouseY, lX - 74, lY + 3);
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }


    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotIn != null) {
            slotId = slotIn.slotNumber;
        }
        if(slotId > 0 && slotId < 6){
            slotId = 10000 + shiftStick*10 + slotId;
        }
        this.mc.playerController.windowClick(
                this.inventorySlots.windowId, slotId, mouseButton, type, this.mc.player
        );
    }


    private boolean isHoveredStick(int x, int y, int mouseX, int mouseY){
        return mouseX>=x+36 && mouseX < x+36+15 && mouseY>=y+16 && mouseY < y+16+12;
    }


    private void drawStick(int x, int y, boolean hov){
        this.drawTexturedModalRect(x+36, y+16, 176, hov?12:0, 15, 12);
    }
}