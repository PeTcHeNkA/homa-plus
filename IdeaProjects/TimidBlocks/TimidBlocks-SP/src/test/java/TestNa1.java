import java.util.HashSet;


public class TestNa1 {
    static int range = 8, locX = 0, locZ = 3,
               axf = -7, azf = -7, axt = 15, azt = 15;

    public static void main(String[] args) {
        {
            if(args.length > 0){
                if(args.length == 1 && args[0].equals("help")){
                    System.out.println("range, x, z, view(x1, z1, x2, z2); example: [15 7 7 -15 -15 25 25]");
                    return;
                }

                range = Integer.valueOf(args[0]);
                locX = Integer.valueOf(args[1]);
                locZ = Integer.valueOf(args[2]);

                if(args.length > 3){
                    axf = Integer.valueOf(args[3]);
                    azf = Integer.valueOf(args[4]);

                    axt = Integer.valueOf(args[5]);
                    azt = Integer.valueOf(args[6]);
                }
            }

            int deltaMin = range >> 1;
            int minX = locX - deltaMin;
            int minZ = locZ - deltaMin;
            int maxX = minX + range + 16;
            int maxZ = minZ + range + 16;

            for (int x = minX; x <= maxX; x += 16) {
                for (int z = minZ; z <= maxZ; z += 16) {

                    execute((x >> 4), (z >> 4), null,
                            minX, minZ, maxX - 16, maxZ - 16
                    );
                }
            }
        }
        draw(axf, azf, axt, azt);
    }


    private static void draw(int x1, int z1, int x2, int z2){
        int n2 = (z2 - z1 + 1) * 3;
        String header = String.format(" Matrix [%s %s : %s %s] ", x1, z1, x2, z2);

        n2 -= header.length();
        if(n2 > 0){
            header = repeat("=",n2/2) + header + repeat("=", n2-(n2/2) );
        }

        System.out.println(header);

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                boolean b = data.contains(toStr(x, z));
                int chX = x & 7, chZ = z & 7;
                String psh = "[ ]";

                if(x == x1){
                    psh = " "+(z & 15);
                }
                if(z == z1){
                    psh = " "+(x & 15);
                    if(x == x1){
                        psh = "[*]";
                    }
                }
                if(psh.length() < 3){
                    psh += " ";
                }

                if(b){
                    psh = "[#]";

                    if(x == locX && z == locZ){
                        psh = "[+]";
                    }
                }

                System.out.printf(psh);
            }
            System.out.printf("\n");
        }
    }


    private static String repeat(String str, int count){
        StringBuilder sb = new StringBuilder(count*str.length());
        for(int i = 0; i < count; i++){
            sb.append(str);
        }
        return sb.toString();
    }


    private static void execute(int x1, int z1, Object n, int mx, int my, int mxx, int mxz) {
        //System.out.printf("%s %s %s %s %s %s %s\n", x1, z1, n, mx, my, mxx, mxz);

        clearBlocksInRange(x1, z1, mx, my, mxx, mxz);
    }


    protected static void clearBlocksInRange(int CH_X, int CH_Z, int minX, int minZ, int maxX, int maxZ){
        int lim = 100; //todo maybe faster?
        //int lim = calcMaxH(chunk, snap, blocks);


        for (int x = 0; x < 16; x++){
            if(!isBetween(minX, maxX, (CH_X * 16) + x))continue;

            for (int z = 0; z < 16; z++){
                if(!isBetween(minZ, maxZ, (CH_Z * 16) + z))continue;

                String str = toStr(((CH_X * 16) + x), ((CH_Z * 16) + z));
                data.add(str);

                //System.out.println(str);


                //System.out.println(toStr(((CH_X * 16) + x), ((CH_Z * 16) + z)));

                //d.

                //System.out.printf("X: %s Z: %s \n", ((CH_X * 16) + x), ((CH_Z * 16) + z));
            }
        }
    }


    private static String toStr(int x, int z){
        return String.format("X: %s Z: %s", x, z);
    }


    public static boolean isBetween(int min, int max, int val){
        return val >= min && val <= max;
    }


    private static HashSet<String> data = new HashSet<>();
}