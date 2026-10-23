package plus.invlib.slot;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import plus.inv.AInventoryClientRenderer;
import plus.invlib.render.RItemFrame;
import plus.invlib.render.RString;
import plus.invlib.render.RenderableBasic;
import plus.invlib.render.animate.ABCPoint;
import plus.invlib.render.animate.Animation;
import plus.invlib.render.r2d.GlUtls;


public class ASlotPlus extends AbcSlotRenderable {

    public double scale = 1;
    private float zIndex = 0;

    double yPos = 0;
    double xPos = 0;
    public double curHeight;
    public double curWidth;

    private final APoint normal = new APoint(0,0,0,0.5f);
    private final APoint hovered = new APoint(1,1,1,0.8f);
    private boolean isHovered = false;

    private final Animation<APoint, ASlotPlus> animation = new Animation<>(
            165, normal
    );


    public ASlotPlus(AInventoryClientRenderer gui, SSlot slot) {
        super(gui, slot);
    }


    @Override
    public boolean isInBound(int mouseX, int mouseY){
        return isBetween(mouseX, yPos, curHeight) && isBetween(mouseY, xPos, curWidth);
    }


    public ASlotPlus update(double y, double x, double z, double scale) {
        if(RenderableBasic.validScale((float) scale)){
            if(RenderableBasic.isNeedScale((float) scale)){
                this.scale = scale;
            }
        } else {
            enabled = false;
            return this;
        }

        yPos = y;
        xPos = x;

        zIndex = (float) (z/scale);

        curHeight = (yPos +18f*scale);
        curWidth = (xPos +18f*scale);

        enabled = true;

        needResize=false;
        return this;
    }


//    public void onDraw(){
//        //animation.get().render(this);
//        super.onDraw();
////        if(slot!=null){
////            ItemStack item = slot.getStack();
////            draw(item);
////        } else {
////            draw(ItemStack.EMPTY);
////        }
//    }


    protected void onNormal(){
        if(!isHovered)return;
        isHovered = false;
        animation.setTarget(normal);
    }


    protected void onHover(){
        if(isHovered)return;
        isHovered = true;
        animation.setTarget(hovered);
    }


    @Override
    public void onDraw(ItemStack item, String text, boolean lighting){
        if(!lighting){
            animation.get().render(this);
        } else {
            hovered.render(this);
        }
        GlStateManager.pushMatrix();

        //GlStateManager.disableLighting();

        GlStateManager.translate(yPos, xPos, -100*scale);
        GlStateManager.scale(scale,scale,scale);

        RItemFrame.itemRender.zLevel = 0.0F;
        RItemFrame.itemRender.renderItemAndEffectIntoGUI(item, 1, 1);

        if(item.getCount()>1) {
            net.minecraft.client.gui.FontRenderer font = item.getItem().getFontRenderer(item);
            if (font == null) font = RString.fontRenderer;
            if(scale>2) {
                GlStateManager.scale(0.5, 0.5, 0.5);

                RItemFrame.itemRender.renderItemOverlayIntoGUI(font, item, 18, 18, text);
            } else {
                RItemFrame.itemRender.renderItemOverlayIntoGUI(font, item, 1, 1,
                        String.valueOf(item.getCount()));
            }
        }
        //GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }


    public static class APoint extends ABCPoint<APoint, ASlotPlus> {
        public float r;
        public float g;
        public float b;
        public float a;

        public APoint(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }


        @Override
        public APoint range(APoint p) {
            return new APoint(
                    range(r, p.r),
                    range(g, p.g),
                    range(b, p.b),
                    range(a, p.a)
            );
        }


        @Override
        public APoint copy() {
            return new APoint(r,g,b,a);
        }


        @Override
        public APoint add(APoint p) {
            return new APoint(
                    r+p.r,
                    g+p.g,
                    b+p.b,
                    a+p.a
            );
        }


        @Override
        public APoint pow(float f) {
            return new APoint(
                    r*f,
                    g*f,
                    b*f,
                    a*f
            );
        }


        @Override
        public void render(ASlotPlus slot) {
            GlUtls.drawSolid(
                    slot.yPos, slot.xPos,
                    slot.curHeight, slot.curWidth,
                    r,g,b,a, 1
            );
        }
    }
}