package plus.fserver.utls;

import net.minecraft.item.EnumDyeColor;


public class FastDyeBlend {
    private static final EnumDyeColor[] BLENDABLE_DYE_COLORS = new EnumDyeColor[15|15<<4];

    static {
        BLENDABLE_DYE_COLORS[15|8<<4 ] = EnumDyeColor.SILVER;
        BLENDABLE_DYE_COLORS[15|4<<4 ] = EnumDyeColor.LIGHT_BLUE;
        BLENDABLE_DYE_COLORS[15|2<<4 ] = EnumDyeColor.LIME;
        BLENDABLE_DYE_COLORS[15|1<<4 ] = EnumDyeColor.PINK;
        BLENDABLE_DYE_COLORS[15|0<<4 ] = EnumDyeColor.GRAY;
        BLENDABLE_DYE_COLORS[11|1<<4 ] = EnumDyeColor.ORANGE;
        BLENDABLE_DYE_COLORS[9 |5<<4 ] = EnumDyeColor.MAGENTA;
        BLENDABLE_DYE_COLORS[8 |15<<4] = EnumDyeColor.SILVER;
        BLENDABLE_DYE_COLORS[5 |9<<4 ] = EnumDyeColor.MAGENTA;
        BLENDABLE_DYE_COLORS[4 |15<<4] = EnumDyeColor.LIGHT_BLUE;
        BLENDABLE_DYE_COLORS[4 |2<<4 ] = EnumDyeColor.CYAN;
        BLENDABLE_DYE_COLORS[4 |1<<4 ] = EnumDyeColor.PURPLE;
        BLENDABLE_DYE_COLORS[2 |15<<4] = EnumDyeColor.LIME;
        BLENDABLE_DYE_COLORS[2 |4<<4 ] = EnumDyeColor.CYAN;
        BLENDABLE_DYE_COLORS[1 |15<<4] = EnumDyeColor.PINK;
        BLENDABLE_DYE_COLORS[1 |11<<4] = EnumDyeColor.ORANGE;
        BLENDABLE_DYE_COLORS[1 |4<<4 ] = EnumDyeColor.PURPLE;
        BLENDABLE_DYE_COLORS[0 |15<<4] = EnumDyeColor.GRAY;
    }

    public static EnumDyeColor getBlendedColor(EnumDyeColor first, EnumDyeColor second) {
        return BLENDABLE_DYE_COLORS[first.getDyeDamage()|second.getDyeDamage()>>4];
    }
}
