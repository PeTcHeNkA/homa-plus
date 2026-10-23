package plus.wepather;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;


public class PathProxy {
    private static boolean enabled = true;

    private static void disablePatcher(Throwable e){
        enabled = false;

        if(e instanceof ClassNotFoundException){
            System.out.println("WORLD EDIT NOT FOUNDED, PATCHER IGNORED");
        } else {
            System.out.println("WORLD EDIT PATCHER DISABLED");
            e.printStackTrace();
        }
    }

    public static void insertToWE(BlockStairs block){
        if(!enabled)return;
        try {
            Path.insertToWE(block);
        }catch (Throwable e){
            disablePatcher(e);
        }
    }


    public static void insertToWE(BlockDirectional block){
        if(!enabled)return;
        try {
            Path.insertToWE(block);
        }catch (Throwable e){
            disablePatcher(e);
        }
    }


    public static void insertToWE(BlockSlab block){
        if(!enabled)return;
        try {
            Path.insertToWE(block);
        }catch (Throwable e){
            disablePatcher(e);
        }
    }


    public static void onInit(Runnable r){
        try {
            Path.onInit(r);
        }catch (Throwable e){
            disablePatcher(e);
        }
    }
}