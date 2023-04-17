package plus.client.buttons;

import scala.actors.threadpool.Arrays;
import java.util.ArrayList;
import java.util.List;
import static plus.utls.MathUtils.isBetween;


public class ButtonSet {
    private Button[] buttons = new Button[0];
    private int activeIndex = 0;

    public void add(Button b) {
        List<Button> nl = new ArrayList<>(Arrays.asList(buttons));
        nl.add(b);
        buttons = nl.toArray(new Button[0]);
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