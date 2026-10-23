package plus.wepather;

import com.sk89q.worldedit.world.registry.*;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.ResourceLocation;
import plus.block.Block;
import java.util.*;


public class Path {
    private static BundledBlockData.BlockEntry insertBaseEnt(net.minecraft.block.Block block){
        Map<Integer, BundledBlockData.BlockEntry> map = BundledBlockData.getInstance().legacyMap;
        ArrayList<String> list = new ArrayList<>();
        list.add(block.getRegistryName().toString());
        BundledBlockData.BlockEntry ent = new BundledBlockData.BlockEntry(
                Block.getIdFromBlock(block),
                block.getRegistryName().toString(),
                block.getLocalizedName(),
                list
        );
        map.put(Block.getIdFromBlock(block), ent);
        return ent;
    }


    public static void onInit(Runnable r){
        BundledBlockData.onInitTask = r;
    }


    private static boolean validBlock(net.minecraft.block.Block block){
        ResourceLocation name = block.getRegistryName();
        return name!=null;
    }


    public static void insertToWE(BlockDirectional block){
        if(!validBlock(block))return;
        BundledBlockData.BlockEntry ent = insertBaseEnt(block);
        SimpleState state = new SimpleState();
        state.values = new HashMap<>();

        state.values.put("up", new SimpleStateValue(state, (byte)0, 0, 1,0));
        state.values.put("down", new SimpleStateValue(state, (byte)1, 0, -1,0));
        state.values.put("north", new SimpleStateValue(state, (byte)2, 0, 0,-1));
        state.values.put("south", new SimpleStateValue(state, (byte)3, 0, 0,1));
        state.values.put("west", new SimpleStateValue(state, (byte)4, -1, 0,0));
        state.values.put("east", new SimpleStateValue(state, (byte)5, 1, 0,0));

        ent.states.put("facing", state);
    }


    public static void insertToWE(BlockStairs block){
        if(!validBlock(block))return;
        BundledBlockData.BlockEntry ent = insertBaseEnt(block);
        SimpleState state = new SimpleState();
        state.values = new HashMap<>();

        state.values.put("north", new SimpleStateValue(state, (byte)3, 0, 0,-1));
        state.values.put("south", new SimpleStateValue(state, (byte)2, 0, 0,1));
        state.values.put("west", new SimpleStateValue(state, (byte)1, -1, 0,0));
        state.values.put("east", new SimpleStateValue(state, (byte)0, 1, 0,0));
        ent.states.put("facing", state);

        state = new SimpleState();
        state.values = new HashMap<>();
        state.values.put("top", new SimpleStateValue(state, (byte)4, 0, 1,0));
        state.values.put("bottom", new SimpleStateValue(state, (byte)0, 0, -1,0));
        ent.states.put("half", state);
    }


    public static void insertToWE(BlockSlab block){
        if(!validBlock(block))return;
        BundledBlockData.BlockEntry ent = insertBaseEnt(block);
        SimpleState state = new SimpleState();
        state.values = new HashMap<>();

        state = new SimpleState();
        state.values = new HashMap<>();
        state.values.put("top", new SimpleStateValue(state, (byte)8, 0, 1,0));
        state.values.put("bottom", new SimpleStateValue(state, (byte)0, 0, -1,0));
        ent.states.put("half", state);
    }
}
