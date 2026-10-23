package plus.fserver.world.save;

import java.util.Arrays;


public class VarIntBuf {
    //3 int, 2 ref, and var-byte
    private byte[] data = new byte[16];

    private int b1len = 0, b1size = 16;
    private int b2len = 0, b2size = 0;
    private int b3len = 0, b3size = 0;


    private void expand(int shift, int point){
        //int shift = 6;
        byte[] cData = this.data;
        byte[] nData = new byte[cData.length+shift];

        System.arraycopy(cData, 0, nData, 0, point);
        System.arraycopy(cData, point, nData, point+shift, cData.length-point);

        this.data = nData;
    }


    private void expandE(int shift){
        byte[] cData = this.data;
        this.data = Arrays.copyOf(cData, cData.length+shift);
    }


    public void add(int uInt){
        uInt += 1;

        byte b1 = (byte) ((uInt)        & 0xFF);
        byte b2 = (byte) ((uInt >>>  8) & 0xFF);
        byte b3 = (byte) ((uInt >>> 16) & 0xFF);

        if(b3 != 0){
            add3(b1, b2, b3);
            return;
        }
        if(b2 != 0){
            add2(b1, b2);
            return;
        }
        add1(b1);
    }


    public int size(){
        return b1len + b2len + b3len;
    }


    private void add1(byte b){
        int bl, sz1;
        if((bl = b1len) == (sz1 = b1size)){
            expand(8, sz1);
            b1size += 8;
        }
        data[bl] = b;
        ++b1len;
    }


    private void add2(byte b1, byte b2){
        int bl, sz2;
        if((bl = b2len) == (sz2 = b2size)){
            //add to prev
            if(b1size - b1len >= 2){
                bl = (b1size -= 2);
                ++b2size;
                ++b2len;
                byte[] data = this.data;
                data[bl]   = b1;
                data[bl+1] = b2;
                return;
            }
            expand(16, (sz2 << 1) + b1size);
            b2size += 8;
        }
        byte[] data = this.data;
        bl = ((bl << 1) + b1size);
        data[bl]   = b1;
        data[bl+1] = b2;
        ++b2len;
    }


    private void add3(byte b1, byte b2, byte b3){
        int bl;
        if((bl = b3len) == b3size){
            expandE(24);
            b3size += 8;
        }
        byte[] data = this.data;
        bl = ((bl * 3) + (b1size + (b2size<<1)));
        data[bl]   = b1;
        data[bl+1] = b2;
        data[bl+2] = b3;
        ++b3len;
    }


    public int remMax(){
        byte[] data = this.data;
        int len = this.b3len-1;

        if(len >= 0){
            len = ((len * 3) + (b1size + (b2size<<1)));
            --this.b3len;
            int result   = merge(data[len], data[len+1], data[len+2])-1;
            return result;
        }

        len = this.b2len-1;

        if(len >= 0){
            len = ((len << 1) + b1size);
            --this.b2len;
            int result   = merge(data[len], data[len+1])-1;
            return result;
        }

        len = this.b1len-1;

        if(len >= 0){
            --this.b1len;
            int result   = (data[len] & 255)-1;
            return result;
        }
        return -1;
    }


    @Override
    public String toString() {
        int realSize = data.length;
        int used = b1len + (b2len*2) + (b3len*3);
        int size = this.size();
        int economy = (b1len) * 3 + (b2len)*2 + (b3len);

        return "["+used+"b/"+realSize+"b+24b] (nums: "+size+", eco: "+economy+"b)";
    }


    private static int merge(byte b1, byte b2){
        return ((b2 & 255) << 8) | (b1 & 255);
    }
    private static int merge(byte b1, byte b2, byte b3){
        return ((b3 & 255) << 16) | ((b2 & 255) << 8) | (b1 & 255);
    }
}