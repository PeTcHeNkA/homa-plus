package asm;

import plus.tson.TsonDouble;
import plus.tson.TsonObj;

public class TestASM implements Operator2Num{

    public static void main(String[] args) {
        System.out.println("NEN");
        //System.out.println(new asm.TestASM().calc());
    }


    @Override
    public TsonObj calc(TsonObj a1, TsonObj a2) {
        return new TsonDouble(a1.getDouble() + a2.getDouble() + a2.getDouble());
    }

    @Override
    public double calc(double d1, double d2) {
        return (d1 + d2) * (d1 - d2);
    }
}



