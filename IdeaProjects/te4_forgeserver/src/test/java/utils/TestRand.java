package utils;

import net.minecraft.util.math.BlockPos;
import plus.fserver.utls.math.FastRandom;

import java.util.Random;

public class TestRand {
    public static void main(String[] args) {

        BlockPos start = new BlockPos(8,8,8);

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        Random rand = new Random(1);

        for (int i = 0; i < 100; i++){
            FastRandom.setRandom8x8x4(rand, mut, start);
            System.out.println(mut);
        }



    }
}
