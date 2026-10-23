package plus.util;

import plus.tson.TsonClass;


public class PCLTask{
    protected final Class<?> clazz;
    private boolean loaded = false;
    public final int priority;

    public PCLTask(Class<?> clazz) {
        this.clazz = clazz;
        priority = 1000;
    }

    public PCLTask(Class<?> clazz, int priority) {
        this.clazz = clazz;
        this.priority = priority;
    }

    public Object inst() {
        return new TsonClass(clazz).createInst();
    }


    protected final void markLoaded(){
        loaded = true;
    }


    protected final boolean isLoaded(){
        return loaded;
    }


    protected String getName(){
        return clazz.getSimpleName();
    }
}
