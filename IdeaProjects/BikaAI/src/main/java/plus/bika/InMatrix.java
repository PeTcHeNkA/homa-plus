package plus.bika;


public class InMatrix {
    public static final int MAX_POS_VALUE = 255;
    public static final int BIT_SIZE = 8;
    public static final int BIT_SIZE_2 = BIT_SIZE*2;

    private byte[] data = new byte[(MAX_POS_VALUE+1)*(MAX_POS_VALUE+1)*(MAX_POS_VALUE+1)]; //16mb

    public InMatrix() {

    }


    public byte getData(int x, int y, int z){
        return getData(data, x, y, z);
    }


    public static byte getData(byte[] src, int x, int y, int z) {
        return src[(x << BIT_SIZE_2) | (y << BIT_SIZE) | z];
    }


    public void setData(int x, int y, int z, byte data){
        setData(this.data, x, y, z, data);
    }


    public static void setData(byte[] src, int x, int y, int z, byte data){
        src[(x << BIT_SIZE_2) | (y << BIT_SIZE) | z] = data;
    }


    public static boolean validNum(int num){
        return (num | MAX_POS_VALUE) == MAX_POS_VALUE;
    }


    public static boolean validPos(int x, int y, int z){
        return (x | y | z | MAX_POS_VALUE) == MAX_POS_VALUE;
    }


    public byte[] getRaw(){
        return this.data;
    }
}