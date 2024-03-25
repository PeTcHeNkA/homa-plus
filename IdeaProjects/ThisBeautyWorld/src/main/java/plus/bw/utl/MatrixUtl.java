package plus.bw.utl;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import plus.bw.world.Matrix;


/**
 * Utilities for work with matrixes
 */
public class MatrixUtl {
    /**
     * Mathematical operations over each element of an initial matrix
     * <p>First matrix which should be changed
     * <p>Second Matrix which will be used in calculations. Does not change
     */
    public interface MatrixOperator extends IntInt2IntFunction{
        default void apply(Matrix first, Matrix second){
            MatrixUtl.apply(this, first, second);
        }
    }

    public static final Int2IntFunction EMPTY_FUNC = n -> n;
    public static final MatrixOperator  ADD_FUNC = Integer::sum;
    public static final MatrixOperator  POW_FUNC = (a, b) -> a * b;
    public static final MatrixOperator  MAX_FUNC = Math::max;
    public static final MatrixOperator  DEC_FUNC = (a, b) -> a - b;
    public static final MatrixOperator  DIV_FUNC = (a, b) -> a / b;

    /**
     * Example: matrix.toString(" ", MatrixUtl.viFunc(5, 30, 70, " ", ".", "+", "#")
     * @param lim threshold to which the line "a" is used
     * @return function which will transform number to a line
     */
    public static Int2ObjectFunction<String> viFunc(int lim, String a, String b){
        return num -> num >= lim ? a:b;
    }


    public static Int2ObjectFunction<String> viFunc(int lim, int lim2, String a, String b, String c){
        return num -> {
            if(num>=lim) return num >= lim2 ? c:b;
            return a;
        };
    }


    public static Int2ObjectFunction<String> viFunc(int lim1, int lim2, int lim3, String a, String b, String c, String d){
        return num -> {
            if(num >= lim2) return num >= lim3 ? d:c;
            return num >= lim1? b:a;
        };
    }


    public static Int2IntFunction clamp(int min, int max){
        return num -> {
            if(num < min)return min;
            return num > max ? max : min;
        };
    }


    public static void apply(IntInt2IntFunction func, Matrix first, Matrix second){
        int[] rawFirst = first.raw(), rawSecond = second.raw();
        for (int i = 0, max = first.size(); i < max; i++) rawFirst[i] = func.get(rawFirst[i], rawSecond[i]);
    }


    /**
     * @return Function which will add the specified number to input data
     */
    public static Int2IntFunction add(int num){
        return key -> key + num;
    }


    /**
     * @return Function which will multiply the specified number by input data
     */
    public static Int2IntFunction boost(float factor){
        return key -> Math.round(key * factor);
    }


    /**
     * @return Function which will multiply the specified number by input data
     */
    public static Int2IntFunction boost(int factor){
        return key -> key * factor;
    }


    /**
     * @return Function which returns a certain value, regardless of input data
     */
    public static Int2IntFunction use(int num){
        return key -> num;
    }


    public static Matrix smooth5(Matrix src){
        return smooth5var(src, 5);
    }


    public static Matrix smooth5var(Matrix src, final int del){
        Matrix result = new Matrix(src.width(), src.height());
        for (int cX = 1; cX < src.height()-1; cX++){
            for (int cY = 1; cY<  src.height()-1; cY++){

                int orig =  src.get(cX, cY);
                float summ =  orig +
                        src.get(cX+1, cY) +
                        src.get(cX-1, cY) +
                        src.get(cX, cY+1) +
                        src.get(cX, cY-1);

                result.set(cX, cY, Math.round((summ)/del));
            }
        }
        copyBounds(src, result);
        return result;
    }


    /**
     * @param src Initial matrix
     * @return The copy of a matrix with smoothing on the basis of average values in a square 3х3
     */
    public static Matrix smooth9(Matrix src){
        return smooth9var(src, 9, null);
    }


    public static Matrix smooth9(Matrix src, int[] alloc){
        return smooth9var(src, 9, alloc);
    }


    public static Matrix smooth9var(Matrix src, final int del, int[] alloc){
        int w;
        Matrix result = alloc ==null?new Matrix(w = src.width(), src.height()):
                new Matrix(w = src.width(), src.height(), alloc);
        int[] raw = src.raw();

        for (int cX = 1, p0; cX < src.height() - 1; cX++){
            p0 = cX * w;
            for (int cY = 1; cY < w - 1; cY++){
                int p = p0 + cY;

                int summ =
                        raw[p] +
                        raw[p + 1] +
                        raw[p - 1] +
                        raw[p + w] +
                        raw[p + w - 1] +
                        raw[p + w + 1] +
                        raw[p - w] +
                        raw[p - w - 1] +
                        raw[p - w + 1];

                result.set(cX, cY, summ/del);
            }
        }
        copyBounds(src, result);
        return result;
    }


    /**
     * Applies function to matrix borders
     * @param to Initial matrix
     * @param func The applied function
     */
    public static void applyBounds(Matrix to, Int2IntFunction func){
        int temp = to.width()-1;

        for (int x=0; x < to.height(); x++){
            to.set(x,  0, func.get(to.get(x, 0  )));
            to.set(x,  temp, func.get(to.get(x, temp  )));
        }

        temp = to.height()-1;

        for (int y = 1; y < to.width(); y++){
            to.set(0, y , func.get(to.get(0, y)));
            to.set(temp, y,  func.get(to.get(temp, y)));
        }
    }


    private static void copyBounds(Matrix src, Matrix to){
        int temp = src.width()-1;

        for (int x=0; x < src.height(); x++){
            to.set(x,  0, src.get(x, 0  ));
            to.set(x,  temp, src.get(x, temp  ));
        }

        temp = src.height()-1;

        for (int y = 1; y < src.width(); y++){
            to.set(0, y , src.get(0, y));
            to.set(temp, y,  src.get(temp, y));
        }
    }


    /**
     * @param src Initial matrix
     * @param factor How much to increase a matrix
     * @return The increased copy of a matrix.
     * Increases resolution by duplication of columns and lines
     */
    public static Matrix zoom(Matrix src, int factor){
        int tempX = src.width()*factor, tempY = src.height()*factor;

        Matrix scaled = new Matrix(tempX, tempY);

        for (int dX = 0; dX < src.height(); dX++){
            for (int dY = 0; dY < src.width(); dY++){
                tempX = dX*factor; tempY = dY*factor;

                for (int eX = 0, src0 = src.get(dX, dY); eX < factor; eX++){
                    for (int eY = 0; eY < factor; eY++){
                        scaled.set(tempX + eX, tempY + eY, src0);
                    }
                }
            }
        }
        return scaled;
    }


    public static Matrix copyOf(Matrix src, int xStart, int yStart, int xEnd, int yEnd){
        Matrix copy = new Matrix(yEnd - yStart, xEnd - xStart);
        for (int x = xStart; x < xEnd; x++){
            for (int y = yStart; y < yEnd; y++){
                copy.set(x - xStart, y - yStart, src.get(x, y));
            }
        }
        return copy;
    }
}
