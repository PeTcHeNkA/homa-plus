package plus.util.cons;


public class Logger {
    private final ConsoleListener listener;

    public Logger(String name) {
        listener = ConsoleListener.consoles.get(name);
    }


    public void info(String s){
        if(listener!=null){
            if(!listener.isNowActive){
                listener.ignoredMessages.add(s);
                return;
            }
        }
        System.out.println(s);
    }


    public void info(Object obj){
        info(obj.toString());
    }


    public static Logger get(){
        return new Logger(null);
    }


    public static Logger get(String name){
        return new Logger(name);
    }
}