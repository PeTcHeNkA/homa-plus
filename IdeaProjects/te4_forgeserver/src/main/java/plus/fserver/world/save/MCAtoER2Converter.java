package plus.fserver.world.save;

import net.minecraft.world.chunk.storage.RegionFile;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;


public class MCAtoER2Converter {
    public static void main(String[] args) throws IOException {
        if(args.length < 3){
            System.out.println("USE: er2/mca path/to/first path/to/second");
        }
        File first  = new File(args[1]);
        File second = new File(args[2]);

        if(args[0].equals("er2")){
            MCAToER2(new RegionFile(first), new ERMapData.ErChunkFile(second));
        } else if (args[0].equals("mca")) {
            ER2ToMCA(new ERMapData.ErChunkFile(first), new RegionFile(second));
        }
    }


    protected static void MCAToER2(RegionFile from, ERMapData.ErChunkFile to) throws IOException {
        byte[] temp = new byte[512];
        for (int x = 0;x < 32; x++){
            for (int z = 0; z < 32; z++){
                DataInputStream in = from.getChunkDataInputStream(x, z);
                if(in != null){
                    DataOutputStream out = to.writeC(x, z);
                    int count = in.read(temp);
                    while (count > 0){
                        out.write(temp, 0, count);
                        count = in.read(temp);
                    }
                    out.close();
                    in.close();
                }
            }
        }
    }


    protected static void ER2ToMCA(ERMapData.ErChunkFile from, RegionFile to) throws IOException {
        byte[] temp = new byte[512];
        for (int x = 0;x < 32; x++){
            for (int z = 0; z < 32; z++){
                DataInputStream in = from.readC(x, z);
                if(in != null){
                    DataOutputStream out = to.getChunkDataOutputStream(x, z);
                    if(out != null) {
                        int count = in.read(temp);
                        while (count > 0) {
                            out.write(temp, 0, count);
                            count = in.read(temp);
                        }
                        out.close();
                    }
                    in.close();
                }
            }
        }
    }
}
