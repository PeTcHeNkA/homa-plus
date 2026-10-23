package plus.sol.core.handler;

import java.util.Scanner;


public abstract class ConsoleHandler<U, E> extends BaseHandler<U, E>{
    private boolean active = true;
    private final CThread thread;

    public ConsoleHandler(boolean deamon) {
        thread = new CThread();
        if(deamon){
            thread.setDaemon(true);
        }
    }

    public void disable(){
        if(active){
            active = false;
        }
    }

    class CThread extends Thread{
        public CThread() {
            super("Console listener");
            start();
        }
        @Override
        public void run() {
            Scanner console = new Scanner(System.in);
            while (active){
                String msg = console.nextLine();
                if(active) {
                    onMsg(msg.split(" "));
                }
            }
        }
    }


    protected abstract void onMsg(String[] args);
}