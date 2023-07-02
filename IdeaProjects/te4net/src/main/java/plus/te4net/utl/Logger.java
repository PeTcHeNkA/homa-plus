package plus.te4net.utl;


public class Logger {
    private static final Logger l = new Logger();
//    private static final org.slf4j.Logger l =
//            org.slf4j.LoggerFactory.getLogger(plus.Main.class);
    public void info(String s){
//        l.info(s);
        System.out.println(s);
    }

    public static Logger get(){
        return l;
    }
}