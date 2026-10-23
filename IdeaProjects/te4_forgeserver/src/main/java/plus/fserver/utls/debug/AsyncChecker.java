package plus.fserver.utls.debug;


public class AsyncChecker extends RuntimeException{
    StackTraceElement[] elements;
    Throwable parent;
    Thread current;

    public void lock(){
        current = Thread.currentThread();
    }


    public void checkCurrent(){
        Thread cur = Thread.currentThread();
        if(cur != current){
            throw new RuntimeException("Try call async!");
        }
    }


    public void check(){
        elements = (new Throwable()).getStackTrace();
    }


    public AsyncChecker handle(Throwable parent){
        this.parent = parent;
        return this;
    }


    @Override
    public StackTraceElement[] getStackTrace() {
        StackTraceElement[] elems = parent.getStackTrace();
        StackTraceElement[] all = new StackTraceElement[elements.length+elems.length+1];
        System.arraycopy(elements, 0, all, 0, elements.length);
        all[elements.length] = new StackTraceElement("---[ OTHER THREAD ]---", "-------------------------", null, 0);
        System.arraycopy(elems, 0, all, elements.length+1, elems.length);
        return all;
    }
}