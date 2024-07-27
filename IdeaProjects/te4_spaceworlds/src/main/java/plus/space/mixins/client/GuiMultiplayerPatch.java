package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import plus.space.client.lscreen.LGuiScreen;


@Mixin(GuiMultiplayer.class)
public class GuiMultiplayerPatch extends GuiScreen {
    private final LGuiScreen lGuiScreen = new LGuiScreen("sel_server", 1280, 729, 0,
            LGuiScreen.Sticky.CENTER, LGuiScreen.Sticky.CENTER);

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
    public void drawDefaultBackground() {
        lGuiScreen.drawImageSafe();
    }
}