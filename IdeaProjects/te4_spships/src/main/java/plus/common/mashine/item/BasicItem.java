package plus.common.mashine.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.common.spship.SPM;
import java.util.List;


public class BasicItem extends Item{
    public static final BasicItem PLATE_GOLD = new BasicItem("plate_gold");
    public static final BasicItem PLATE_IRON = new BasicItem("plate_iron");
    public static final BasicItem PLATE_COPPER = new BasicItem("plate_copper");
    public static final BasicItem PLATE_ALUMINE = new BasicItem("plate_alumine");
    public static final BasicItem PLATE_MEFRIL = new BasicItem("plate_mefril");
    public static final BasicItem PLATE_MACHINE = new BasicItem("plate_machine");
    public static final BasicItem PLATE_ARMORED = new BasicItem("plate_armored");

    public static final BasicItem ALUMINE_INGOT = new BasicItem("alumine_ingot");
    public static final BasicItem COPPER_INGOT = new BasicItem("copper_ingot");
    public static final BasicItem MEFRIL_INGOT = new BasicItem("mefril_ingot");

    public static final BasicItem IRON_GEAR = new BasicItem("iron_gear");
    public static final BasicItem COPPER_COIL = new BasicItem("copper_coil");
    public static final BasicItem AETHER_CRYSTAL = new BasicItem("aether_crystal");


    public BasicItem(String name){
        setRegistryName(new ResourceLocation(SpMain.MOD_ID, name));
        setTranslationKey(name);
        setCreativeTab(SpMain.SP_TAB);
        SpMain.proxy.addToRegObj(this);
    }


    public boolean isSelf(ItemStack item){
        return item.getItem() == this;
    }


    @SideOnly(Side.CLIENT)
    protected static boolean isMoreInfo(){
        return GuiScreen.isShiftKeyDown();
    }


    @SideOnly(Side.CLIENT)
    protected static void addLore(List<String> lore, String s, Object... args){
        lore.add(I18n.translateToLocalFormatted("sp_lore."+s, args));
    }


    @SideOnly(Side.CLIENT)
    protected static float round(float f){
        return ((int)(f*100))/100f;
    }


    @SideOnly(Side.CLIENT)
    protected static void addInf(List<String> lore, int[] data, String s, SPM p){
        if(p.I<data.length) {
            int i = data[p.I];
            if (i != 0) {
                double d = i / 100d;
                lore.add(I18n.translateToLocalFormatted("sp_lore." + s, d));
            }
        }
    }
}