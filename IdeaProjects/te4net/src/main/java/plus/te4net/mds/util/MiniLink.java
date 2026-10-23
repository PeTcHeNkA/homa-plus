package plus.te4net.mds.util;

import plus.te4net.core.api.PublicUsage;


@PublicUsage
public final class MiniLink {
    public static String toMini(String link){
        if(link.charAt(0)=='/')return 'I'+ toMin0(link.substring(1));
        if(link.startsWith("https://")){
            return 'H'+ toMin0(link.substring(8));
        } else if(link.startsWith("http://")){
            return toMin0(link.substring(7));
        }
        return 'I'+ toMin0(link);
    }


    public static String ofMini(String link){
        if(link==null || link.isBlank())return null;
        char c = link.charAt(0);
        switch (c) {
            case 'H': return "https://" + ofMin0(link.substring(1));
            case 'V': return "https://www.youtube.com/watch?v=" + link.substring(1);
            case 'E': return "https://www.youtube.com/embed/" + link.substring(1);
            case 'I': return '/'+ ofMin0(link.substring(1));
            default: return "http://" + ofMin0(link);
        }
    }


    private static String toMin0(String raw){
        return raw.replace("/","->").replace("=","-e>")
                .replace("?","-q>").replace("&","-n>");
    }


    private static String ofMin0(String raw){
        return raw.replace("->","/").replace("-e>","=")
                .replace("-q>","?").replace("-n>","&");
    }
}