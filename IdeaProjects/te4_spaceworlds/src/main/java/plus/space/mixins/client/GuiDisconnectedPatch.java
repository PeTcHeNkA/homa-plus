package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import plus.space.client.lscreen.LGuiScreen;


@Mixin(GuiDisconnected.class)
public class GuiDisconnectedPatch extends GuiScreen {
    private final LGuiScreen lGuiScreen = LGuiScreen.getLast();

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        lGuiScreen.setSize(width, height);
    }


    @Override
    public void setGuiSize(int w, int h) {
        super.setGuiSize(w, h);
        lGuiScreen.setSize(w, h);
    }


    @Override
    public void drawBackground(int tint) {
        lGuiScreen.drawImageSafe();
    }
}