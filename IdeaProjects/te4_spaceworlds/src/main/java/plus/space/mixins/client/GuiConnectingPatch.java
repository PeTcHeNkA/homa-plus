package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.space.client.lscreen.LGuiLoadingScreen;
import plus.space.client.lscreen.LGuiScreen;


@Mixin(GuiConnecting.class)
public class GuiConnectingPatch extends GuiScreen {
    @Shadow private NetworkManager networkManager;
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


    /**
     * @author Te4hno
     * @reason lscreen
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lGuiScreen.drawImgFull();
        if (this.networkManager == null) {
            this.drawCenteredString(this.fontRenderer, I18n.format("connect.connecting"), this.width / 2, this.height / 2 - 50, 16777215);
        } else {
            this.drawCenteredString(this.fontRenderer, I18n.format("connect.authorizing"), this.width / 2, this.height / 2 - 50, 16777215);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}