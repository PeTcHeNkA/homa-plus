package plus.te4net.mds.util;

import plus.te4net.core.api.PublicUsage;


@PublicUsage
public class IpUtls {
    public static String strIpFromInt(int ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }


    public static int intIpFromStr(String str){
        try {
            int result = 0;
            String[] elems = str.split("\\.");
            for (int i = 3; i >= 0; i--) {
                int elm = Integer.parseInt(elems[3 - i]);
                result |= elm << (i * 8);
            }
            return result;
        } catch (NumberFormatException e){
            return 0;
        }
    }


    private static byte ofChar(char c){
        switch (c){
            default :return 0;
            case '1':return 1;
            case '2':return 2;
            case '3':return 3;
            case '4':return 4;
            case '5':return 5;
            case '6':return 6;
            case '7':return 7;
            case '8':return 8;
            case '9':return 9;
        }
    }
}