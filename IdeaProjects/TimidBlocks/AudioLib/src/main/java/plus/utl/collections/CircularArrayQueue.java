package plus.utl.collections;

import java.util.Arrays;


/**
 * Быстрая замкнутая очередь обьектов
 * Старые обьекты замещаются новыми, при переполнении внутреннего буфера
 * @author HomaPlus
 */
public class CircularArrayQueue<T>{
    protected int readIdx, writeIdx;
    protected Object[] data;

    protected CircularArrayQueue(){}

    public CircularArrayQueue(int size) {
        data = new Object[size];
    }


    /**
     * Добавить элемент в "хвост" очереди
     */
    public void add(T obj) {
        if(obj == null) return;
        data[writeIdx++] = obj;
        if(writeIdx == data.length) {
            writeIdx = 0;
        }
    }


    /**
     * Забирает элемент из "головы" очереди
     */
    public T pop(){
        Object[] data = this.data;
        Object result = data[readIdx];
        data[readIdx] = null;
        ++readIdx;

        if(readIdx == data.length) {
            readIdx = 0;
        }
        return (T)result;
    }


    /**
     * Очищает очередь
     */
    public void clear(){
        Arrays.fill(data, null);
        readIdx = writeIdx = 0;
    }


    public void copyTo(CircularArrayQueue<T> queue){
        queue.data = Arrays.copyOf(data, data.length);
        queue.writeIdx = writeIdx;
        queue.readIdx = readIdx;
    }


    /**
     * Возвращает элемент относительно "головы" очереди с указанным сдвигом
     */
    public T getAtHead(int idx){
        Object[] data = this.data;
        int finalIdx = readIdx + idx;

        if(finalIdx >= data.length){
            finalIdx -= data.length;
        } else if(finalIdx < 0){
            finalIdx += data.length;
        }
        return (T) data[finalIdx];
    }


    /**
     * Возвращает элемент относительно "хвоста" очереди с указанным сдвигом
     */
    public T getAtTail(int idx){
        return (T) getAtTail(data, writeIdx, idx);
    }


    protected static Object getAtTail(Object[] data, int writeIdx, int idx){
        int finalIdx = writeIdx + idx;

        if(finalIdx >= data.length){
            finalIdx -= data.length;
        } else if(finalIdx < 0){
            finalIdx += data.length;
        }
        return data[finalIdx];
    }


    /**
     * Возвращает размер очереди
     */
    public int size(){
        return data.length;
    }
}