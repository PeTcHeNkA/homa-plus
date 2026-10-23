package plus.invlib.render;

import net.minecraft.client.renderer.GlStateManager;

import java.util.List;


public class RMultiString extends RString {

    private int lineHeight = 10;
    private String[] strings = new String[]{};


    public RString update(List<String> text){
        strings = new String[text.size()];
        for(int i=0;i<strings.length;i++){
            strings[i] = parseColors(text.get(i));
        }
        return this;
    }


    public RString update(String text){
        strings = text.split("\n");
        for(int i=0;i<strings.length;i++){
            strings[i] = parseColors(strings[i]);
        }
        return this;
    }


    public RString update(int x, int xLimit, int y, int z, float scale, boolean center, int lineHeight){
        this.lineHeight = (int) (lineHeight/scale);
        return super.update(x, xLimit, y, z, scale, center);
    }


    @Override
    public void draw() {
        if(center){
            int curLineIndex = 0;
            for(String str:strings) {
                flushStr(str);
                if (cantDraw) {
                    continue;
                }
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, zIndex);
                if (needResize) {
                    GlStateManager.scale(curScale, curScale, curScale);
                }
                fontRenderer.drawStringWithShadow(string, curXPos,
                        curYPos+curLineIndex*lineHeight, 0xFFFFFF
                );

                GlStateManager.popMatrix();
                curLineIndex+=1;
            }
        } else {
            super.draw();
        }
    }


    @Override
    protected void onDraw() {
        int curLineIndex = 0;
        for(String str:strings){
            fontRenderer.drawStringWithShadow(str, curXPos, curYPos+curLineIndex*lineHeight, 0xFFFFFF);
            curLineIndex+=1;
        }
    }
}