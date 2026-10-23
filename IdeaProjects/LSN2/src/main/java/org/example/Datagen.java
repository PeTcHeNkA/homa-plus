package org.example;

import java.util.Random;


public class Datagen {
    public static void main(String[] args) {
        Random r = new Random();
        for (int i = 0; i < 500; i++){
            System.out.print(Integer.toHexString(r.nextInt(30))+" ");

            String val = Integer.toHexString(r.nextInt());
            val = val.substring(r.nextInt(val.length()-3));

            System.out.print(val+" ");
        }
    }
}