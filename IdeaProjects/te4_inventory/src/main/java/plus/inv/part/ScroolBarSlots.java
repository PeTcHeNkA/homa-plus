package plus.inv.part;

import net.minecraft.inventory.Slot;
import plus.inv.AInventoryClientRenderer;
import plus.invlib.slot.SSlot;
import plus.inv.AInventory;
import plus.invlib.render.Updateable;
import plus.invlib.render.r2d.GlUtls;

import java.util.List;


public class ScroolBarSlots extends AbcPart implements Updateable {
    private int shift = 0;
    public int maxAllowedShift = 0;
    private float listeningAL = -1;
    private float listeningAR = -1;
    private int lastY = -1;
    private boolean listing = false;

    protected int topShift=30;
    protected int globalHeight;

    public ScroolBarSlots(AInventoryClientRenderer aicr){
        super(aicr);
    }


    @Override
    public void update() {
        if(!isEnabled())return;
        topShift = 0;
        globalHeight = invrenderer.height-topShift;
        shift = -1;
        maxAllowedShift = calcMaxShift();
        listeningAL = invrenderer.width/24f + 18*5;
        listeningAR = listeningAL + 9;
        setShift(0);
        if(maxAllowedShift==0) {
            updateSlotPositions();
        }
    }


    public void setShift(int newShift){
        if(newShift>maxAllowedShift){
            newShift = maxAllowedShift;
        }
        if(newShift<0){
            newShift = 0;
        }
        if(shift==newShift)return;
        shift = newShift;
        updateSlotPositions();
    }


    private int calcMaxShift(){
        int slotMatrixYSize = AInventory.ySlots*18;
        if(globalHeight<slotMatrixYSize){
            return slotMatrixYSize-globalHeight;
        } else {
            return 0;
        }
    }


    public void renderScroolBar(){
        if(maxAllowedShift==0)return;

        float horizontalShift = listeningAL + 2.5f;
        float hrzSh2 = horizontalShift+3.5f;
        GlUtls.drawSolid(
                horizontalShift, topShift,
                hrzSh2, topShift+globalHeight,
                0.5f,0.5f,0.5f,1,200
        );

        int guiYSize = AInventory.ySlots*18;

        float scrSize = globalHeight/ (guiYSize / (float)(guiYSize-maxAllowedShift));
        float scrPos = (globalHeight-scrSize) * (shift/(float)maxAllowedShift);

        GlUtls.drawSolid(
                 horizontalShift, topShift+scrPos,
                 hrzSh2, topShift+scrPos+scrSize,
                0.7f,0.7f,0.7f,1,250
        );
    }


    public void scrool(int mouseY){
        if(!listing || maxAllowedShift==0)return;
        if(lastY==-1){
            lastY = mouseY;
            return;
        }
        int newShift = shift;
        if(mouseY>lastY) {
            newShift +=(mouseY - lastY);
        } else {
            newShift -= (lastY - mouseY);
        }
        if(shift!=newShift){
            lastY = mouseY;
        }
        setShift(newShift);
    }


    public void disableListing(){
        lastY = -1;
        listing = false;
    }


    public void enableListing(int mouseX, int mouseY){
        if(mouseY == lastY || mouseX<listeningAL || mouseX>listeningAR) return;
        listing = true;
        lastY = -1;
    }


    public void updateSlotPositions() {
        int horizontalShift = invrenderer.width/24;
        int localShift;
        if(maxAllowedShift==0){
            localShift = ((AInventory.ySlots*18)-globalHeight);
        } else {
            localShift = shift;
        }

        List<Slot> slots = invrenderer.inventorySlots.inventorySlots;
        for(int i=20;i<slots.size();i+=1) {
            SSlot slot = (SSlot) slots.get(i);

            slot.xPos = horizontalShift + slot.defXpos;
            int dpos = slot.defYpos - localShift;
            if(dpos < -3 || dpos>globalHeight-15){
                slot.yPos = globalHeight*2;
            } else {
                slot.yPos = topShift+dpos;
            }
        }
    }


    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled){
            update();
        } else {
            shift = 10000;
            updateSlotPositions();
            maxAllowedShift = 0;
        }
    }
}