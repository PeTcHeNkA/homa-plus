package plus.space.mixins.client;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSnooper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.space.client.gui.GuiSnooperList;
import plus.space.client.lscreen.LGuiMainMenu;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Mixin(GuiSnooper.class)
public class GuiSnooperPatch extends GuiScreen {
    @Shadow private String[] desc;
    @Shadow @Final private List<String> keys;
    @Shadow @Final private List<String> values;
    @Shadow private GuiButton toggleButton;
    @Shadow private String title;
    @Shadow @Final private GameSettings game_settings_2;
    @Shadow @Final private GuiScreen lastScreen;
    private LGuiMainMenu lGuiScreen = LGuiMainMenu.get();
    private GuiSnooperList list;

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        LGuiMainMenu.open("video_stngs_bg");
        super.setWorldAndResolution(mc, width, height);
        lGuiScreen.setSize(width, height);
    }

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    public void initGui() {
        this.title = I18n.format("options.snooper.title");
        String s = I18n.format("options.snooper.desc");
        java.util.List<String> list = Lists.<String>newArrayList();

        for (String s1 : this.fontRenderer.listFormattedStringToWidth(s, this.width - 30)) {
            list.add(s1);
        }
        this.desc = list.toArray(new String[list.size()]);
        this.keys.clear();
        this.values.clear();
        this.toggleButton = this.addButton(new GuiButton(1, this.width / 2 - 152, this.height - 30, 150, 20,
                this.game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED)));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height - 30, 150, 20, I18n.format("gui.done")));
        boolean flag = this.mc.getIntegratedServer() != null &&
                this.mc.getIntegratedServer().getPlayerUsageSnooper() != null;

        for (Map.Entry<String, String> entry : (new TreeMap<>(this.mc.getPlayerUsageSnooper().getCurrentStats())).entrySet()) {
            this.keys.add((flag ? "C " : "") + entry.getKey());
            this.values.add(this.fontRenderer.trimStringToWidth(entry.getValue(), this.width - 220));
        }

        if (flag) {
            for (Map.Entry<String, String> entry1 : (new TreeMap<String, String>(this.mc.getIntegratedServer().getPlayerUsageSnooper().getCurrentStats())).entrySet())
            {
                this.keys.add("S " + (String)entry1.getKey());
                this.values.add(this.fontRenderer.trimStringToWidth(entry1.getValue(), this.width - 220));
            }
        }
        this.list = new GuiSnooperList((GuiSnooper)(Object) this, fontRenderer, keys, values);
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

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        list.handleMouseInput();
    }

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 2) {
                this.game_settings_2.saveOptions();
                this.game_settings_2.saveOptions();
                this.mc.displayGuiScreen(this.lastScreen);
            }
            if (button.id == 1) {
                this.game_settings_2.setOptionValue(GameSettings.Options.SNOOPER_ENABLED, 1);
                this.toggleButton.displayString = this.game_settings_2.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED);
            }
        }
    }

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 8, 16777215);
        int i = 22;

        for (String s : this.desc) {
            this.drawCenteredString(this.fontRenderer, s, this.width / 2, i, 8421504);
            i += this.fontRenderer.FONT_HEIGHT;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
