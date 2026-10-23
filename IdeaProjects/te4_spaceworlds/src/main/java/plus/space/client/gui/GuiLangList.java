package plus.space.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.lscreen.LResLocation;
import java.util.Map;


@SideOnly(Side.CLIENT)
public class GuiLangList extends GuiSlot {
    private final java.util.List<String> langCodeList = Lists.newArrayList();
    private final Map<String, Language> languageMap = Maps.newHashMap();
    private final GuiLanguage gui;
    private final LanguageManager languageManager;
    private final GameSettings settings;
    private final FontRenderer fontRenderer;
    private final GuiOptionButton confirmBtn, forceUnicode;

    public GuiLangList(Minecraft mcIn, GuiLanguage gui, LanguageManager languageManager,
                       GameSettings settings, FontRenderer renderer, GuiOptionButton confirm,
                       GuiOptionButton forceUnicode, int width, int height) {
        super(mcIn, width, height, 32, height - 65 + 4, 18);
        this.gui = gui;
        this.languageManager = languageManager;
        this.settings = settings;
        this.fontRenderer = renderer;
        this.confirmBtn = confirm;
        this.forceUnicode = forceUnicode;
        for (Language language : languageManager.getLanguages()) {
            this.languageMap.put(language.getLanguageCode(), language);
            this.langCodeList.add(language.getLanguageCode());
        }
    }


    protected int getSize() {
        return this.langCodeList.size();
    }


    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        Language language = this.languageMap.get(this.langCodeList.get(slotIndex));
        languageManager.setCurrentLanguage(language);
        settings.language = language.getLanguageCode();
        net.minecraftforge.fml.client.FMLClientHandler.instance().refreshResources(net.minecraftforge.client.resource.VanillaResourceType.LANGUAGES);
        fontRenderer.setUnicodeFlag(languageManager.isCurrentLocaleUnicode() || settings.forceUnicodeFont);
        fontRenderer.setBidiFlag(languageManager.isCurrentLanguageBidirectional());
        confirmBtn.displayString = I18n.format("gui.done");
        forceUnicode.displayString = settings.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
        settings.saveOptions();
    }


    protected boolean isSelected(int slotIndex) {
        return this.langCodeList.get(slotIndex).equals(languageManager.getCurrentLanguage().getLanguageCode());
    }


    protected int getContentHeight() {
        return this.getSize() * 18;
    }


    protected void drawBackground() {
        gui.drawDefaultBackground();
    }


    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        fontRenderer.setBidiFlag(true);
        gui.drawCenteredString(fontRenderer, this.languageMap.get(this.langCodeList.get(slotIndex)).toString(), this.width / 2, yPos + 1, 16777215);
        fontRenderer.setBidiFlag(languageManager.getCurrentLanguage().isBidirectional());
    }


    @Override
    protected void drawContainerBackground(Tessellator tessellator) {}


    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(LResLocation.MEH_BG);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 128.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.left, endY, 0.0D).tex(0.0D, (float)endY / f).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.left + this.width, endY, 0.0D).tex((float)this.width / f, (float)endY / f).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.left + this.width, startY, 0.0D).tex((float)this.width / f, (float)startY / f).color(64, 64, 64, startAlpha).endVertex();
        bufferbuilder.pos(this.left, startY, 0.0D).tex(0.0D, (float)startY / f).color(64, 64, 64, startAlpha).endVertex();
        tessellator.draw();
    }
}