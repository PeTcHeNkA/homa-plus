package plus.utls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


public class ArrayBuilder<T> {
    protected ArrayList<T> data;

    public ArrayBuilder(){
        init(1);
    }

    public ArrayBuilder(int baseSize){
        init(baseSize);
    }

    private void init(int size){
        data = new ArrayList<>(size);
    }

    public ArrayBuilder<T> with(T... items){
        Collections.addAll(data, items);
        return this;
    }


    public ArrayBuilder<T> with(Collection<T> items){
        data.addAll(items);
        return this;
    }

    public ArrayBuilder<T> withReg(Function<String, T> func, String... regs){
        for(String reg:regs) {
            String[] raw = reg.split(":");
            Range range = new Range(raw[1]);
            String gen = raw[0];
            for (int i = range.min; i < range.max; i++) {
                data.add(func.apply(gen + i));
            }
        }
        return this;
    }


    public T[] flushArray(){
        return (T[]) data.toArray();
    }


    public ArrayList<T> flushList(){
        Object copy = data.clone();
        init(1);
        return (ArrayList<T>) copy;
    }


    @Override
    public String toString() {
        return "Builder-> "+data.toString();
    }


    public static class Str extends ArrayBuilder<String>{
        public Str withReg(String... regs){
            for(String reg:regs) {
                String[] raw = reg.split(":");
                Range range = new Range(raw[1]);
                String gen = raw[0];
                for (int i = range.min; i < range.max; i++) {
                    data.add(gen + i);
                }
            }
            return this;
        }


        @Override
        public Str with(String... items) {
            Collections.addAll(data, items);
            return this;
        }


        @Override
        public Str with(Collection<String> items) {
            data.addAll(items);
            return this;
        }


        @Override
        public Str withReg(Function<String, String> func, String... regs) {
            super.withReg(func, regs);
            return this;
        }
    }


    private static class Range{
        final int min, max;
        public Range(String raw){
            if(raw.contains("-")) {
                String[] raw0 = raw.split("-");
                min = Integer.parseInt(raw0[0]);
                max = Integer.parseInt(raw0[1])+1;
            } else {
                min = 0;
                max = Integer.parseInt(raw)+1;
            }
        }
    }
}