import scala.actors.threadpool.Arrays;

public class test {
    public static void main(String[] args) {
//        Boolean.S
//        int maxX = 3, maxY = 3;
//        int minX = 0,  minY = 0;
//
//        isLoadedR(minX, minY, maxX, maxY);
        isLoadedR(3, -4, 3);
        printMatrix();
    }

    private static void isLoadedR(int cX, int cY, int r){
        isLoadedR(cX-r, cY-r, cX+r+1, cY+r+1);
    }

    private static void isLoadedR(int minX, int minY, int maxX, int maxY){
        matrix = new int[20][20];
        sX = -10; sY = -10;
        while (true) {
            isLoaded(minX, minY, maxX, maxY);
            if (minX + 3 < maxX) {
                ++minX;
                --maxX;
                ++minY;
                --maxY;
            } else break;
        }
        print(minX+1, minY+1);
    }

    private static void isLoaded(int minX, int minY, int maxX, int maxY){
        int x=minX, y = minY;
        for (;y<maxY;y++) {
            print(x, y);
        }
        ++x;
        for (--y;x<maxX;x++) {
            print(x, y);
        }
        --y;
        for (--x;y>=minY;y--) {
            print(x, y);
        }
        --x;
        for (++y;x>minX;x--) {
            print(x, y);
        }
    }

    private static int sX, sY;
    private static int[][] matrix;

    private static void print(int x, int y){
        ++matrix[-sX+x][-sY+y];
        //System.out.printf("POINT: %s %s\n", x, y);
    }

    private static void printMatrix(){
        for (int i=0;i< matrix.length;i++){
            System.out.println(Arrays.toString(matrix[i]));
        }
    }


    private static float[] noiseTable ={0.5f, 0.7f, 0.9f};

    private static int getIndxOf(double d){
        int last = -1;
        for (int i=0;i<noiseTable.length;i++){
            if(noiseTable[i]<d)last = i;
            else break;
        }
        return last;
    }
}
