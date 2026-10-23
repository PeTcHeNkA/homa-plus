package asm;

import plus.tson.TsonObj;


public interface Operator2Num {
    TsonObj calc(TsonObj a1, TsonObj a2);
    double calc (double d1, double d2);
}