package plus.inv;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import plus.invlib.slot.AbcSlotRenderable;
import plus.invlib.slot.SSlot;
import plus.inv.part.CraftMatrix;
import plus.invlib.slot.ASlotPlus;
import plus.invlib.render.RItemFrame;
import plus.invlib.render.RMultiString;
import plus.invlib.render.RString;
import plus.invlib.render.RTexture;
import plus.inv.part.PlayerInScreenRendered;
import plus.inv.part.ScroolBarSlots;
import plus.invlib.render.animate.Animation;
import plus.invlib.render.animate.Point1f;
import plus.invlib.utl.RRunnable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AInventoryClientRenderer extends GuiContainer{
	public int shift;
	public int yInvSize;
	private int yMsize = 0;

    private final ScroolBarSlots right5X15Slots = new ScroolBarSlots(this);
	private final PlayerInScreenRendered playerModelRenderer = new PlayerInScreenRendered(this);
    private final CraftMatrix craftMatrix = new CraftMatrix(this);
	private final List<AbcSlotRenderable> advenchedSlots = new ArrayList<>();


	private final ASlotPlus slot1 = new ASlotPlus(this, getSlot(9));
	private final ASlotPlus slot2 = new ASlotPlus(this, getSlot(10));
	private final ASlotPlus slot3 = new ASlotPlus(this, getSlot(11));
	private final ASlotPlus slot4 = new ASlotPlus(this, getSlot(12));
	private final ASlotPlus slot5 = new ASlotPlus(this, getSlot(13));
	private final ASlotPlus slot6 = new ASlotPlus(this, getSlot(14));


	public AInventoryClientRenderer(InventoryPlayer playerInventory, AInventory cInventory, EntityPlayer player) {
		super(new AInventoryContainer(playerInventory, cInventory, player));

		AInventoryClientRenderer inst = this;

		forSlots(0, 9, new RRunnable<SSlot>() {
			@Override
			public void run(SSlot slot) {
				advenchedSlots.add(new AbcSlotRenderable(inst, slot));
			}
		});
		forSlots(9, 15, new RRunnable<SSlot>() {
			@Override
			public void run(SSlot slot) {
				advenchedSlots.add(new AbcSlotRenderable(inst, slot));
			}
		});
		advenchedSlots.addAll(Arrays.asList(slot1,slot2,slot6,slot3,slot4,slot5));

		forSlots(20, inventorySlots.inventorySlots.size(), new RRunnable<SSlot>() {
			@Override
			public void run(SSlot slot) {
				advenchedSlots.add(new AbcSlotRenderable(inst, slot));
			}
		});


	}


	public void forSlots(int indexStart, int indexStop, RRunnable<SSlot>r){
		List<Slot> slots = inventorySlots.inventorySlots;
		for(int i=indexStart;i<indexStop;i+=1){
			r.run((SSlot) slots.get(i));
		}
	}


	public void initGui() {
		super.initGui();
		this.mc.player.openContainer = this.inventorySlots;

		yInvSize = AInventory.ySlots*18 + 27;
		int horizontalShift = width/24;
		if(height>=yInvSize){
			shift = (height - yInvSize)/2;
		} else {
			shift = 9;
		}

		playerModelRenderer.update();

		hotbarGuiTexture.update();
		//leftGuiTexture.update();
		//rightInfoPanelTexture.update();
		itemFrame.update();
		itemName.update();
		itemLore.update();

		forSlots(0, 9, new RRunnable<SSlot>() {
			@Override
			public void run(SSlot slot) {
				slot.xPos = ((width - 162)/2) + slot.defXpos;
				slot.yPos = (int) (((height - 18) * 0.8f) + slot.defYpos);
			}
		});


		forSlots(9, 15, new RRunnable<SSlot>() {
			@Override
			public void run(SSlot slot) {
				slot.xPos = horizontalShift + slot.defXpos;
				slot.yPos = slot.defYpos-700;
			}
		});


        right5X15Slots.update();
        craftMatrix.update();

		yMsize = horizontalShift + 5*18 + 9;
		double scale = ((height)/6d)/18d;

		slot1.update(yMsize,           0,500,scale);
		slot2.update(yMsize, slot1.curWidth,500,scale);
		slot6.update(yMsize, slot2.curWidth,500,scale);
		slot3.update(yMsize, slot6.curWidth,500,scale);
		slot4.update(yMsize, slot3.curWidth,500,scale);
		slot5.update(yMsize, slot4.curWidth,500,scale);

	}


//	private final RTexture leftGuiTexture = new RTexture("textures/gui/gui.png"){
//		@Override
//		public void update(){
//			update(
//					(width/24)-3, shift-3, 210, 0,0,
//					AInventory.xSlots*18+4,
//					height/18> AInventory.ySlots?yInvSize-23:((height/18) -1)*18+4
//			);
//		}
//	};
//	private final RTexture rightInfoPanelTexture = new RTexture(leftGuiTexture.texture){
//		@Override
//		public void update(){
//			update(
//					width - ((width/24)+1 + AInventory.xSlots*18), shift-3, 60,
//					AInventory.xSlots*18+4, 0,
//					AInventory.xSlots*18+4,
//					height/18> AInventory.ySlots?yInvSize-23:((height/18) -1)*18+4
//			);
//		}
//	};


	private final RTexture hotbarGuiTexture = new RTexture("textures/gui/guip2.png"){
		@Override
		public void update(){
			update(
					((width - 162)/2)-3,(int) ((height - 18) * 0.8f)-1,210,
					0,0,18*9+4, 18+4
			);
		}
	};


	private final RItemFrame itemFrame = new RItemFrame(){
		@Override
		public void update(){
			update(
					width - ((width/24)+1 + AInventory.xSlots*18) + 9,
					shift+24, 32, 5
			);
		}
	};


	private final RString itemName = new RString(){
		@Override
		public void update(){
			update(
					width - ((width/24)+1 + AInventory.xSlots*18) + 6,
					width - (width/24)+10,
					shift+5, 500, 1.15f, true
			);
			recalcForCurString();
		}
	};



	private final RMultiString itemLore = new RMultiString(){
		@Override
		public void update(){
			update(
					width - ((width/24)+1 + AInventory.xSlots*18) + 3,width,
//					width - (width/24)+3,
					shift+111, 500, 1, false, 12
			);
			recalcForCurString();
		}
	};


	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		right5X15Slots.scrool(mouseY);


		this.renderHoveredToolTip(mouseX, mouseY);

		playerModelRenderer.setOldMouse(mouseX, mouseY);
	}


	private float scaleDraggedStack = 1f;
	@Override
	protected void drawAndFlushHoveredSlot(int mouseX, int mouseY){
		hoveredSlot = null;
		scaleDraggedStack = 1f;
		for(AbcSlotRenderable asp:advenchedSlots){
			if(asp.draw(mouseX,mouseY)){
				hoveredSlot = asp.slot;
				if(asp instanceof ASlotPlus){
					scaleDraggedStack = (float) ((ASlotPlus) asp).scale;
				}
			}
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		//if(1==1)return;
		//this.drawWorldBackground(0);
		//GlUtls.drawSolid(yMsize,0, width, height, 1,1,1, 0.42f, this.zLevel);

		GlStateManager.translate(0,0,50f);
		playerModelRenderer.drawPlayerOnScreen();

		//GlUtls.drawSolid(0,0, yMsize, height, 0,0,0, 0.57f, 130);
        right5X15Slots.renderScroolBar();

		//drawAndFlushHoveredSlot(mouseX,mouseY);

		GlStateManager.translate(0,0,500f);

		hotbarGuiTexture.draw();

	}


	@Override
	protected Slot getSlotAtPosition(int x, int y) {
		for (AbcSlotRenderable slot : advenchedSlots) {
			if (slot.isInBound(x, y)) {
				return slot.slot;
			}
		}
		return null;
	}


	@Override
	public boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
		return advenchedSlots.get(slotIn.getSlotIndex()).isInBound(mouseX, mouseY);
		//ошибка тут
	}


	public SSlot getSlot(int i) {
		return (SSlot) inventorySlots.getSlot(i);
	}


	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		if (this.mc.player.inventory.getItemStack().isEmpty()
				&& hoveredSlot != null && hoveredSlot.getHasStack()) {
			this.renderToolTip(hoveredSlot.getStack(), mouseX, mouseY);
		}
	}


	public int lastMouseCode = -1;
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state){
		lastMouseCode = -1;
		right5X15Slots.disableListing();
		super.mouseReleased(mouseX, mouseY, state);
	}

	private float curCursorSize = 1f;
	private final Animation<Point1f<Object>, Object> itemOnMouseAnimation =
			new Animation<>(150,new Point1f<>(1f));


	@Override
	public void renderStackOnMouse(ItemStack item, int mouseX, int mouseY){
		GlStateManager.pushMatrix();

		if(scaleDraggedStack!=curCursorSize){
			itemOnMouseAnimation.setTarget(new Point1f<>(scaleDraggedStack));
			curCursorSize = scaleDraggedStack;
		}
		float sscale = itemOnMouseAnimation.get().value;

		if(sscale != 1f){
			mouseX = (int) (mouseX/sscale);
			mouseY = (int) (mouseY/sscale);
			GlStateManager.scale(sscale,sscale,sscale);
			GlStateManager.translate(0,0,-100*sscale);
		}

		super.renderStackOnMouse(item,mouseX,mouseY);
		GlStateManager.popMatrix();
	}


	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		curCursorSize = scaleDraggedStack;
		itemOnMouseAnimation.setTarget(new Point1f<>(scaleDraggedStack));
		itemOnMouseAnimation.stop();
		lastMouseCode = mouseButton;
		if(lastMouseCode==0 || lastMouseCode==1) {
			right5X15Slots.enableListing(mouseX, mouseY);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}