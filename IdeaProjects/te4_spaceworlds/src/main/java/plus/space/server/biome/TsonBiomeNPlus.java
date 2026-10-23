package plus.space.server.biome;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import org.apache.commons.lang3.ArrayUtils;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;


public class TsonBiomeNPlus extends TsonBiomePlus {
    private final float[] noiseTable;
    private final IBlockState[] topTable;
    private final IBlockState[] fillerTable;

    public TsonBiomeNPlus(TsonMap map) {
        super(map);

        TsonObj[] list = map.getList("noises").toArray();
        Arrays.sort(list, new Comparator<TsonObj>() {
            @Override
            public int compare(TsonObj o1, TsonObj o2) {
                return Integer.compare(getId(o1), getId(o2));
            }
            private int getId(TsonObj obj) {
                if (obj.isList()) return obj.getList().getInt(0);
                else return obj.getMap().getInt("noise");
            }
        });
        ArrayUtils.reverse(list);

        noiseTable = new float[list.length];
        topTable = new IBlockState[noiseTable.length];
        fillerTable = new IBlockState[noiseTable.length];

        for(int i=0;i<noiseTable.length;i++){
            TsonObj obj = list[i];
            if(obj.isList()){
                TsonList list0 = obj.getList();
                noiseTable[i] = list0.getFloat(0);
                topTable[i] = blockOfTson(list0.get(1));
                fillerTable[i] = blockOfTson(list0.get(2));
            } else if(obj.isMap()){
                TsonMap map0 = obj.getMap();
                noiseTable[i] = map0.getFloat("noise");
                topTable[i] = blockOfTson(map0.get("block_top"));
                fillerTable[i] = blockOfTson(map0.get("block_fill"));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }


    private int getIndxOf(double d){
        int last = -1;
        for (int i=0;i<noiseTable.length;i++){
            if(noiseTable[i]<d)last = i;
            else break;
        }
        return last;
    }


    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        int ind = getIndxOf(noiseVal);
        IBlockState top, fill;
        if(ind == -1){
            top = this.topBlock;
            fill = this.fillerBlock;
        } else {
            top = topTable[ind];
            fill = fillerTable[ind];
        }
        this.generateBiomeTerrainPatch(worldIn, rand, chunkPrimerIn, x, z, noiseVal, top, fill);
    }
}