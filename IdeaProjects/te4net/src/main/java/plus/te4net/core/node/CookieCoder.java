package plus.te4net.core.node;

import io.netty.handler.codec.http.cookie.*;
import java.nio.CharBuffer;
import java.util.*;


final class CookieCoder {
    static HashMap<String,Cookie> decode(String raw){
        HashMap<String,Cookie> map = new HashMap<>();
        decode(map, raw);
//        ServerCookieDecoder
        return map;
    }


    static void decode(Map<String,Cookie> map, String header) {
        if(header==null)return;
        final int headerLen = header.length();
        if (headerLen == 0) {
            return;
        }
        int i = 0;
        boolean rfc2965Style = false;
        String RFC2965_VERSION = "$Version";
        if (header.regionMatches(true, 0, RFC2965_VERSION, 0, RFC2965_VERSION.length())) {
            i = header.indexOf(';') + 1;
            rfc2965Style = true;
        }
        loop: for (;;) {
            for (;;) {
                if (i == headerLen) {
                    break loop;
                }
                char c = header.charAt(i);
                if (c == '\t' || c == '\n' || c == 0x0b || c == '\f'
                        || c == '\r' || c == ' ' || c == ',' || c == ';') {
                    i++;
                    continue;
                }
                break;
            }
            int nameBegin = i;
            int nameEnd;
            int valueBegin;
            int valueEnd;
            for (;;) {
                char curChar = header.charAt(i);
                if (curChar == ';') {
                    nameEnd = i;
                    valueBegin = valueEnd = -1;
                    break;
                } else if (curChar == '=') {
                    nameEnd = i;
                    i++;
                    if (i == headerLen) {
                        valueBegin = valueEnd = 0;
                        break;
                    }
                    valueBegin = i;
                    int semiPos = header.indexOf(';', i);
                    valueEnd = i = semiPos > 0 ? semiPos : headerLen;
                    break;
                } else {
                    i++;
                }
                if (i == headerLen) {
                    nameEnd = headerLen;
                    valueBegin = valueEnd = -1;
                    break;
                }
            }
            String RFC2965_PATH = "$" + CookieHeaderNames.PATH;
            String RFC2965_DOMAIN = "$" + CookieHeaderNames.DOMAIN;
            String RFC2965_PORT = "$Port";
            if (rfc2965Style && (header.regionMatches(nameBegin, RFC2965_PATH, 0, RFC2965_PATH.length()) ||
                    header.regionMatches(nameBegin, RFC2965_DOMAIN, 0, RFC2965_DOMAIN.length()) ||
                    header.regionMatches(nameBegin, RFC2965_PORT, 0, RFC2965_PORT.length()))) {
                continue;
            }
            if (nameBegin != nameEnd) {
                DefaultCookie cookie = initCookie0(header, nameBegin, nameEnd, valueBegin, valueEnd);
                map.put(cookie.name(), cookie);
            }
        }
    }


    static String code(Cookie cookie) {
        return ServerCookieEncoder.LAX.encode(cookie);
    }


    private static DefaultCookie initCookie0(String header, int nameBegin, int nameEnd, int valueBegin, int valueEnd) {
        CharSequence cs = CharBuffer.wrap(header, valueBegin, valueEnd);
        final int len = cs.length();
        if(len>3){
            if(cs.charAt(0) == '"' && cs.charAt(len-1) == '"')
                cs = cs.subSequence(1, len - 1);
        }

        DefaultCookie cookie = new DefaultCookie(
                header.substring(nameBegin, nameEnd),
                cs.toString()
        );
        cookie.setWrap(cs.length() != valueEnd - valueBegin);
        return cookie;
    }
}