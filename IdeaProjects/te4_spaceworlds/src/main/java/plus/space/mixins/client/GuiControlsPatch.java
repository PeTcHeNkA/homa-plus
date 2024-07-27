package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import plus.space.client.lscreen.LGuiMainMenu;


@Mixin(GuiControls.class)
public class GuiControlsPatch extends GuiScreen {
    private LGuiMainMenu lGuiScreen = LGuiMainMenu.get();

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        LGuiMainMenu.open("video_stngs_bg");
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