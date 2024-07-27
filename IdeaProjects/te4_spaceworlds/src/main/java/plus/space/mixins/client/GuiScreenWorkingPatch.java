package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenWorking;
import org.spongepowered.asm.mixin.Mixin;
import plus.space.client.lscreen.LGuiLoadingScreen;


@Mixin(GuiScreenWorking.class)
public class GuiScreenWorkingPatch extends GuiScreen {
    private final LGuiLoadingScreen lGuiScreen = new LGuiLoadingScreen();


    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        lGuiScreen.setWorldAndResolution(mc, width, height);
    }


    @Override
    public void setGuiSize(int w, int h) {
        super.setGuiSize(w, h);
        lGuiScreen.setGuiSize(w, h);
    }


    @Override
    public void drawBackground(int tint) {
        lGuiScreen.drawImgFull();
    }
}