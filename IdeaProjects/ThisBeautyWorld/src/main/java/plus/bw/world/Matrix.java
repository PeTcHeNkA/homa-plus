package plus.bw.world;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.function.Consumer;


public class Matrix implements Cloneable{
    private int[] matrix;
    private int width, height;

    public Matrix(int width, int height){
        this(width, height, new int[width * height]);
    }


    public Matrix(int w, int height, int[] matrix){
        this.width = w; this.height = height; this.matrix = matrix;
    }


    public void apply(Int2IntFunction func){
        int size = width * height;
        for (int i = 0; i < size; i++) matrix[i] = func.get(matrix[i]);
    }


    public void fill(int value){
        int size = width * height;
        for (int i = 0; i < size; i++) matrix[i] = value;
    }


    public int get(int x, int y){
        return matrix[x * width + y];
    }


    public void set(int x, int y, int v){
        matrix[x * width + y] = v;
    }


    public void apply(int x, int y, Int2IntFunction func){
        x = x * width + y;
        matrix[x] = func.apply(matrix[x]);
    }


    public void apply(Consumer<Matrix> consumer){
        consumer.accept(this);
    }


    public int size(){
        return width * height;
    }


    public int width(){
        return width;
    }


    public int height(){
        return height;
    }


    public int[] raw(){
        return matrix;
    }


    public boolean legalPos(int x, int y){
        if(x < 0 || y < 0)return false;
        return x < height && y < width;
    }


    public int[] array(){
        int[] copy = new int[width * height];
        System.arraycopy(matrix, 0, copy, 0, copy.length);
        return copy;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = width * height, max = 0;

        for (int i=0;i<size;i++){
            if(matrix[i] > max) max = matrix[i];
        }

        int len = String.valueOf(max).length();
        int temp;

        for (int cx = 0; cx < height; cx++){
            builder.append('|');
            for (int cy = 0; cy < width; cy++){
                String num = String.valueOf(get(cx, cy));
                temp = num.length();
                while (temp < len){
                    ++temp;
                    builder.append(' ');
                }
                builder.append(num);
                builder.append('|');
            }
            builder.append('\n');
        }
        builder.setLength(builder.length()-1);
        return builder.toString();
    }


    public String toString(Int2ObjectFunction<String> func){
        return toString("|", func);
    }


    public String toString(String sep, Int2ObjectFunction<String> func){
        StringBuilder builder = new StringBuilder();
        for (int cx = 0; cx < height; cx++){
            builder.append(sep);
            for (int cy = 0; cy < width; cy++){
                builder.append(func.get(get(cx, cy)));
                builder.append(sep);
            }
            builder.append('\n');
        }
        builder.setLength(builder.length()-1);
        return builder.toString();
    }


    @Override
    public Matrix clone() {
        Matrix clone = new Matrix(width, height);
        System.arraycopy(matrix, 0, clone.matrix, 0, size());
        return clone;
    }
}