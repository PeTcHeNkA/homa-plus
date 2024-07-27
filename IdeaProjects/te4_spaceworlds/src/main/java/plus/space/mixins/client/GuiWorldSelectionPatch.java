package plus.space.mixins.client;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.space.client.lscreen.LGuiScreen;


@Mixin(GuiWorldSelection.class)
public class GuiWorldSelectionPatch extends GuiScreen {
    @Shadow private String worldVersTooltip;
    @Shadow private GuiListWorldSelection selectionList;
    @Shadow protected String title;
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


    /**
     * @author Te4hno
     * @reason fix error
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        lGuiScreen.drawImageSafe();
        this.worldVersTooltip = null;
        this.selectionList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.worldVersTooltip != null)
        {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.worldVersTooltip)), mouseX, mouseY);
        }
    }
}