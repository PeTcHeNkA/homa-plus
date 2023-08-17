package plus.client.buttons;

import static plus.utls.MathUtils.isBetween;


public class ButtonSet {
    private Button[] buttons = new Button[0];
    private int activeIndex = 0;

    public void add(Button b) {
        if(buttons.length==0){
            buttons = new Button[]{b};
        } else {
            Button[] btnsNew = new Button[buttons.length+1];
            System.arraycopy(buttons, 0, btnsNew, 0, buttons.length);
            btnsNew[buttons.length] = b;
            buttons = btnsNew;
        }
    }

    public void renderAll(int x, int y) {
        int curShift = 3;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].draw(x, y + curShift, i == activeIndex);
            curShift += 27;
        }
    }


    public int getActiveIndex() {
        return activeIndex;
    }


    public void onClick(int mouseX, int mouseY, int drawX, int drawY) {
        int index = getHoverIndex(mouseX, mouseY, drawX, drawY);
        if (index == -1) return;
        if (activeIndex != index) {
            activeIndex = index;
        }
    }


    public int getHoverIndex(int mouseX, int mouseY, int drawX, int drawY) {
        if (isBetween(mouseX, drawX + 5, drawX - 30)) {
            int curShift = drawY;
            for (int i = 0; i < buttons.length; i++) {
                if (isBetween(mouseY, curShift + 26, curShift)) {
                    return i;
                }
                curShift += 27;
            }
        }
        return -1;
    }
}