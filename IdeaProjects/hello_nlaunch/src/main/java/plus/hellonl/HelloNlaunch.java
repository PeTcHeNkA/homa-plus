package plus.hellonl;


public class HelloNlaunch {
    private static String _codePass0A_ = "Home Homa Homa Plus";
    public static void main(String... args){
        printLogo();
        seeUrl();
    }

    private static void printLogo(){
        String prefix = "----------------------------------------------------";
        print(
                prefix,
                "  _   _   _                                  _     \n" +
                        " | \\ | | | |   __ _   _   _   _ __     ___  | |__  \n" +
                        " |  \\| | | |  / _` | | | | | | '_ \\   / __| | '_ \\ \n" +
                        " | |\\  | | | | (_| | | |_| | | | | | | (__  | | | |\n" +
                        " |_| \\_| |_|  \\__,_|  \\__,_| |_| |_|  \\___| |_| |_|\n" +
                        "        PROTECT YOUR APP WITH NLAUNCH NOW              ",
                prefix
        );
    }

    private static void seeUrl(){
        try {
            Runtime rt = Runtime.getRuntime();
            String url = "https://vk.cc/crIL2s";
            rt.exec("rundll32 url.dll, FileProtocolHandler "+url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(Object... o){
        for (Object l:o){
            System.out.println(l);
        }
    }
}