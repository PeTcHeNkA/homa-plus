package test1;

import java.lang.reflect.InvocationTargetException;


public class Test3 {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {


        int num = 0;

        System.out.println(join(15,15,255));


    }


    public static int join(int x, int y, int z){
        return x & 15 | (y & 15)<<4 | (z & 255)<<8;
    }
}





