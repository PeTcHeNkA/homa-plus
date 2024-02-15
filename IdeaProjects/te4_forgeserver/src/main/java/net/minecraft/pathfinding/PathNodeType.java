package net.minecraft.pathfinding;

import org.jetbrains.annotations.NotNull;
import java.util.Iterator;


public enum PathNodeType {
    BLOCKED(         1 ,-1.0F ),
    OPEN(            2 ,0.0F  ),
    WALKABLE(        3 ,0.0F  ),
    TRAPDOOR(        4 ,0.0F  ),
    FENCE(           5 ,-1.0F ),
    LAVA(            6 ,-1.0F ),
    WATER(           7 ,8.0F  ),
    RAIL(            8 ,0.0F  ),
    DANGER_FIRE(     9 ,8.0F  ),
    DAMAGE_FIRE(     10,16.0F ),
    DANGER_CACTUS(   11,8.0F  ),
    DAMAGE_CACTUS(   12,-1.0F ),
    DANGER_OTHER(    13,8.0F  ),
    DAMAGE_OTHER(    14,-1.0F ),
    DOOR_OPEN(       15,0.0F  ),
    DOOR_WOOD_CLOSED(16,-1.0F ),
    DOOR_IRON_CLOSED(17,-1.0F );

    private final float priority;
    public final int id;

    PathNodeType(int id, float priorityIn) {
        this.priority = priorityIn;
        this.id = id;
    }

    public float getPriority()
    {
        return this.priority;
    }


    public static final PathNodeType[] TYPES = {
            null,
            PathNodeType.BLOCKED,          PathNodeType.OPEN,          PathNodeType.WALKABLE,
            PathNodeType.TRAPDOOR,         PathNodeType.FENCE,         PathNodeType.LAVA,
            PathNodeType.WATER,            PathNodeType.RAIL,          PathNodeType.DANGER_FIRE,
            PathNodeType.DAMAGE_FIRE,      PathNodeType.DANGER_CACTUS, PathNodeType.DAMAGE_CACTUS,
            PathNodeType.DANGER_OTHER,     PathNodeType.DAMAGE_OTHER,  PathNodeType.DOOR_OPEN,
            PathNodeType.DOOR_WOOD_CLOSED, PathNodeType.DOOR_IRON_CLOSED
    };


    public static final class Set implements Iterable<PathNodeType>, Iterator<PathNodeType>{
        private int items = 0;
        private int cursor = 0;
        private int size = 0;
        private int current = 0;

        public void add(PathNodeType type){
            if(!contains(type))++size;
            items |= 1<<type.id;
        }

        public boolean contains(PathNodeType type){
            return ((items >> type.id) & 1) == 1;
        }

        @NotNull
        @Override
        public Iterator<PathNodeType> iterator() {
            cursor = current = 0;
            return this;
        }


        @Override
        public boolean hasNext() {
            return current<size;
        }


        @Override
        public PathNodeType next() {
            do {
                ++cursor;
            } while (((items>>cursor)&1) == 0);
            ++current;
            return TYPES[cursor];
        }
    }
}