package plus.sol.utls;


public class Logger {
    private static Ilogger logger = new Ilogger() {
        @Override
        public void log(String msg) {
            System.out.println(msg);
        }

        @Override
        public void error(Exception e) {
            e.printStackTrace();
        }
    };

    interface Ilogger{
        void log(String msg);
        void error(Exception e);
    }

    public static void setLogger(Ilogger l){
        logger = l;
    }

    public static void log(String msg){
        logger.log(msg);
    }
    public static void error(Exception e){
        logger.error(e);
    }
}