package utils;

import net.minecraft.pathfinding.PathNodeType;
import plus.fserver.world.PntMatrix;

public class TestRD {
    public static void main(String[] args) {

        PntMatrix matrix = new PntMatrix();

        PntMatrix.Chunked chunked = matrix.getChunked(0, 0);

        chunked.setType(15, 0, 15, PathNodeType.BLOCKED);
        chunked.setType(7, 0, 7, PathNodeType.BLOCKED);
        chunked.setType(1, 0, 1, PathNodeType.BLOCKED);

        chunked.setType(150, 255, 1, PathNodeType.OPEN);
        chunked.setType(150, 254, 1, PathNodeType.BLOCKED);
        chunked.setType(150, 253, 1, PathNodeType.DAMAGE_FIRE);
        chunked.setType(150, 252, 1, PathNodeType.DAMAGE_CACTUS);
        chunked.setType(150, 251, 1, PathNodeType.DOOR_OPEN);
        chunked.setType(150, 251, 1, PathNodeType.DOOR_WOOD_CLOSED);


        System.out.println(chunked.getType(150, 255, 1));
        System.out.println(chunked.getType(150, 254, 1));
        System.out.println(chunked.getType(150, 253, 1));
        System.out.println(chunked.getType(150, 252, 1));
        System.out.println(chunked.getType(150, 251, 1));


    }
}
