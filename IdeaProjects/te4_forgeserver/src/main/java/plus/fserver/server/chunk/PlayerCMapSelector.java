package plus.fserver.server.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;


public class PlayerCMapSelector{
    private final int[] mask;
    private final int radius;
    private final int radiusSq;

    public PlayerCMapSelector(int radius){
        mask = makeMask((this.radius = radius)+1);
        radiusSq = radius * radius;
    }

    public static int inCircle(int x1, int y1, int x2, int y2) {
        int temp;
        if(x1>x2){
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if(y1>y2){
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
        temp = x2 - x1;
        x1 = temp * temp;
        temp = y2 - y1;
        y1 = temp * temp;
        return x1 + y1;
    }


    private static int[] makeMask(int radius){
        IntArrayList list = new IntArrayList();
        int maxSq = radius*radius;
        for (int x=0;x<64;++x){
            for (int z=0;z<64;++z){
                int range = inCircle(x, z,32, 32);
                if(range <= maxSq) {
//                    System.out.println(range);
                    list.add(range<<16 | x<<8 | z);
                }
            }
        }
        list.sort((o1, o2) -> {
            int range1 = o1>>16;
            int range2 = o2>>16;
            return Integer.compare(range1, range2);
        });
        int[]mask = new int[list.size()];
        for (int i=0;i<mask.length;i++) {
            mask[i] = list.getInt(i) & 0xFFFF;
        }
        return mask;
    }


//    public void test(int x, int z){
//        int radX = x >> 4;
//        int radZ = z >> 4;
//
//        for (int item:mask){
//            int dz = (item & 0xFF) - 32;
//            int dx = (item >> 8) - 32;
//            System.out.printf("Add to X: %s Z: %s\n", dx+radX, dz+radZ);
//        }
//    }


    public void addPlayer(PlayerChunkMap pcm, EntityPlayerMP player) {
        int radX = (int)player.posX >> 4;
        int radZ = (int)player.posZ >> 4;
        player.managedPosX = player.posX;
        player.managedPosZ = player.posZ;

        for (int item:mask){
            int dz = (item & 0xFF) - 32;
            int dx = (item >> 8) - 32;
            pcm.getOrCreateEntry(radX-dx, radZ-dz).addPlayer(player);
        }
    }


    public void updateMovingPlayer(PlayerChunkMap pcm, EntityPlayerMP player) {
        int i = (int)player.posX >> 4;
        int j = (int)player.posZ >> 4;
        double d0 = player.managedPosX - player.posX;
        double d1 = player.managedPosZ - player.posZ;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            int senX = (int)player.managedPosX >> 4;
            int senZ = (int)player.managedPosZ >> 4;

            int j1 = i - senX;
            int k1 = j - senZ;

            int radius = this.radius;
            if(player.renderRadius != player.prevRenderRadius){
                radius = player.prevRenderRadius;
                if(radius<3)radius = 3;
                else if(radius>pcm.getMaxPlayerViewRadius())
                    radius = pcm.getMaxPlayerViewRadius();
                player.prevRenderRadius = player.renderRadius;
            }

            if (j1 != 0 || k1 != 0) {
                for (int l1 = i - radius; l1 <= i + radius; ++l1) {
                    for (int i2 = j - radius; i2 <= j + radius; ++i2) {
                        if (overlapsCircle(l1, i2, senX, senZ)) {
                            pcm.getOrCreateEntry(l1, i2).addPlayer(player);
                        }
                        if (overlapsSq(l1 - j1, i2 - k1, i, j, radius)) {
                            PlayerChunkMapEntry playerchunkmapentry = pcm.getEntry(l1 - j1, i2 - k1);

                            if (playerchunkmapentry != null) {
                                playerchunkmapentry.removePlayer(player);
                            }
                        }
                    }
                }
                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
                pcm.markSortPending();
            }
        }
    }


    private boolean overlapsSq(int x1, int z1, int x2, int z2, int radius) {
        int dx = x1 - x2;
        int dy = z1 - z2;
        if (dx >= -radius && dx <= radius) {
            return dy < -radius || dy > radius;
        } else {
            return true;
        }
    }


    private boolean overlapsCircle(int x1, int z1, int senX, int senZ) {
        int dx = x1 - senX;
        int dy = z1 - senZ;
        return (dx * dx) + (dy * dy) < radiusSq;
    }
}
