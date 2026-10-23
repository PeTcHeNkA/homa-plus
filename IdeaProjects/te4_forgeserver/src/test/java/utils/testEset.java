package utils;

import net.minecraft.pathfinding.PathNodeType;

public class testEset {

    public static void main(String[] args) {
        PathNodeType.Set set = new PathNodeType.Set();

        set.add(PathNodeType.LAVA);
        set.add(PathNodeType.OPEN);
        set.add(PathNodeType.BLOCKED);
        set.add(PathNodeType.LAVA);


        for (PathNodeType type:set){
            System.out.println(type);
        }

    }




}
