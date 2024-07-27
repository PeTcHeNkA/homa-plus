package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.space.client.gui.GuiLangList;
import plus.space.client.lscreen.LGuiMainMenu;
import java.io.IOException;


@Mixin(GuiLanguage.class)
public class GuiLanguagePatch extends GuiScreen {
    @Shadow private GuiOptionButton forceUnicodeFontBtn;
    @Shadow private GuiOptionButton confirmSettingsBtn;
    @Shadow @Final private LanguageManager languageManager;
    @Shadow @Final private GameSettings game_settings_3;
    @Shadow protected GuiScreen parentScreen;
    private final LGuiMainMenu lGuiScreen = LGuiMainMenu.get();
    private GuiLangList list;

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        LGuiMainMenu.open("options_bg");
        super.setWorldAndResolution(mc, width, height);
        lGuiScreen.setSize(width, height);
    }

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    public void initGui() {
        this.forceUnicodeFontBtn = this.addButton(new GuiOptionButton(100,
                this.width / 2 - 155,
                this.height - 38, GameSettings.Options.FORCE_UNICODE_FONT,
                this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));

        this.confirmSettingsBtn = this.addButton(new GuiOptionButton(6,
                this.width / 2 - 155 + 160, this.height - 38,
                I18n.format("gui.done")));


        this.list = new GuiLangList(mc, (GuiLanguage)(Object)this, languageManager, game_settings_3,
                fontRenderer, confirmSettingsBtn, forceUnicodeFontBtn, width, height);
        this.list.registerScrollButtons(7, 8);
    }


    @Override
    public void setGuiSize(int w, int h) {
        super.setGuiSize(w, h);
        lGuiScreen.setSize(w, h);
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


    @Override
    public void drawDefaultBackground() {
        lGuiScreen.drawImageSafe();
    }

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 5:
                    break;
                case 6:
                    this.mc.displayGuiScreen(this.parentScreen);
                    break;
                case 100:
                    if (button instanceof GuiOptionButton) {
                        this.game_settings_3.setOptionValue(((GuiOptionButton)button).getOption(), 1);
                        button.displayString = this.game_settings_3
                                .getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
                        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        this.setWorldAndResolution(this.mc, i, j);
                    }
                    break;
                default:
                    list.actionPerformed(button);
            }
        }
    }

    /**
     * @author Te4hno
     * @reason bg
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("options.language"),
                this.width / 2, 16, 16777215);
        this.drawCenteredString(this.fontRenderer, "(" +
                        I18n.format("options.languageWarning") + ")",
                this.width / 2, this.height - 56, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}