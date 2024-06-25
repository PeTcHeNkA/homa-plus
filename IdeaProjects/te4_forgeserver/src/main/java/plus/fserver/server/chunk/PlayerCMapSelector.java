package plus.fserver.server.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import java.util.Comparator;


public class PlayerCMapSelector{
    private static final Comparator<Integer> COMPARATOR = (o1, o2) -> {
        final int range1, range2;
        if((range1 = o1>>16) == (range2 = o2>>16))return 0;
        if(range1 > range2)return 1;
        return -1;
    };

    private final int[] mask;
    private final int radius;

    public PlayerCMapSelector(int radius){
        mask = makeMask((this.radius = radius)+1);
    }

    public static int range(int x1, int y1, int x2, int y2) {
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
                int range = range(x, z,32, 32);
                if(range <= maxSq) {
                    list.add(range<<16 | x<<8 | z);
                }
            }
        }
        list.sort(COMPARATOR);
        int[]mask = new int[list.size()];
        for (int i=0;i<mask.length;i++) {
            mask[i] = list.getInt(i) & 0xFFFF;
        }
        return mask;
    }


    public void addPlayer(PlayerChunkMap pcm, EntityPlayerMP player) {
        int curX = (int) player.posX >> 4;
        int curZ = (int) player.posZ >> 4;

        for (int item : mask) {
            int mdZ = (item & 0xFF) - 32;
            int mdX = (item >> 8) - 32;
            pcm.getOrCreateEntry(curX - mdX, curZ - mdZ).addPlayer(player);
        }
        player.managedPosX = player.posX;
        player.managedPosZ = player.posZ;

    }


    public void removePlayer(PlayerChunkMap pcm, EntityPlayerMP player){
        int curX = (int) player.posX >> 4;
        int curZ = (int) player.posZ >> 4;

        for (int item : mask) {
            int mdZ = (item & 0xFF) - 32;
            int mdX = (item >> 8) - 32;
            PlayerChunkMapEntry entry = pcm.getEntry(curX - mdX, curZ - mdZ);
            if(entry != null) entry.removePlayer(player);
        }
    }


    private void updatePlayerRadius(PlayerChunkMap pcm, EntityPlayerMP player){
        int curX = (int) player.posX >> 4;
        int curZ = (int) player.posZ >> 4;
        int minRad;


        if(player.prevRenderRadius < player.renderRadius){
            minRad = player.prevRenderRadius;

            minRad = (minRad+1) * (minRad +1);

            for (int item : mask) {
                int mdZ = (item & 0xFF) - 32;
                int mdX = (item >> 8) - 32;

                if(range(0,0, mdX, mdZ) > minRad){
                    pcm.getOrCreateEntry(curX - mdX, curZ - mdZ).addPlayer(player);
                }
            }
        } else {
            minRad = player.renderRadius;

            minRad = (minRad+1) * (minRad +1);

            for (int item : mask) {
                int mdZ = (item & 0xFF) - 32;
                int mdX = (item >> 8) - 32;

                if(range(0,0, mdX, mdZ) > minRad){
                    PlayerChunkMapEntry entry = pcm.getEntry(curX - mdX, curZ - mdZ);
                    if(entry != null) entry.removePlayer(player);
                }
            }
        }

        player.prevRenderRadius = player.renderRadius;
    }


    public void updateMovingPlayer(PlayerChunkMap pcm, EntityPlayerMP player) {
        int curX = (int)player.posX >> 4;
        int curZ = (int)player.posZ >> 4;
        double d0 = player.managedPosX - player.posX;
        double d1 = player.managedPosZ - player.posZ;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64d) {
            int preX = (int)player.managedPosX >> 4;
            int preZ = (int)player.managedPosZ >> 4;

            int dX = curX - preX;
            int dZ = curZ - preZ;

            if (dX != 0 || dZ != 0) {
                int radSq = (radius+1) * (radius+1);

                for (int item:mask){
                    int mdZ = (item & 0xFF) - 32;
                    int mdX = (item >> 8) - 32;


                    if(range(preX, preZ, curX-mdX, curZ-mdZ) > radSq){
                        pcm.getOrCreateEntry(curX-mdX, curZ-mdZ).addPlayer(player);
                    }

                    if(range(curX, curZ, preX-mdX, preZ-mdZ) > radSq){
                        PlayerChunkMapEntry entry = pcm.getEntry(preX-mdX, preZ-mdZ);
                        if(entry != null) entry.removePlayer(player);
                    }
                }
                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
                pcm.markSortPending();
            }
        } else if (player.prevRenderRadius != player.renderRadius && d2 > 16d) {
            if(player.renderRadius > player.prevRenderRadius){
                pcm.getBy(player.renderRadius).updatePlayerRadius(pcm, player);
            } else {
                updatePlayerRadius(pcm, player);
            }
        }
    }
}
