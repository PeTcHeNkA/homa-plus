package net.minecraft.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class McPatch {
    public static boolean isRunning(Minecraft mc){
        return mc.running;
    }
}