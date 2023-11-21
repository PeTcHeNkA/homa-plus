package plus.te4net.core.mapping;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PublicUsage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@PublicUsage
public final class HRouteMap extends RouteNode {
    public HRouteMap() {
        super(0);
    }


    public void put(String key, IRequestProcessor value){
        if(key.charAt(0)=='/')key = key.substring(1);
        put(new RouteKey(key), value, this, 0);
    }


    private void put(RouteKey key, IRequestProcessor value, RouteNode current, int cursor){
        if(cursor==6)return;
        int code = key.code(cursor);
        if(code==0){
            if(current.route != null && value !=null){
                throw new IllegalArgumentException("Ready exists! ["+key+"] -> ["+value+"]");
            }
            current.route = value;
            return;
        }
        RouteNode node = current.get(code);
        if(node==null){
            node = new RouteNode(code);
            current.put(code, node);
        }
        put(key, value, node, cursor+1);
    }


    public IRequestProcessor get(RequestSession session){
        StrKey key = session.routeKey();
        key.readNextNoAdd(0);
        return getNode(key, this, 0).route;
    }


    public IRequestProcessor get(String key0) {
        StrKey key = new StrKey(key0);
        key.readNextNoAdd(0);
        return getNode(key, this, 0).route;
    }


    private RouteNode getNode(StrKey key, RouteNode current, int cursor){
        RouteNode node = current.get(key.code(cursor));
        if(node==null){
            key.addSplat(cursor);
            return current;
        }
        key.readNextNoAdd(++cursor);
        return getNode(key, node, cursor);
    }


    public static final class StrKey extends RouteKey {
        private String[] splat = new String[5];
        private final char[] chars;
        private String afterRoute;
        private Map<String,String> queryParams;
        private int i = 1;
        private int limit;
        private int lastQ;

        public StrKey(String key) {
            chars = key.toCharArray();
            limit = chars.length;
        }


        private void addSplat(int shift){
            if(shift==lastQ) {
                if(queryParams == null) readNextAdd();
                else parseQParams();
            } else {
                splat[0] = null;
                if (queryParams == null) queryParams = Collections.emptyMap();
                else parseQParams();
            }
        }


        private void parseQParams(){
            i = ++limit;
            int prev = i, sep = i, delta;
            for (char c;i<chars.length;i++) {
                c = chars[i];
                if(c=='&'){
                    if(prev != i){
                        if(i == sep)
                            queryParams.put(new String(chars, prev, i-prev-1),"");
                        else
                            queryParams.put(
                                    new String(chars, prev, (delta = sep-prev)-1),
                                    new String(chars, prev+delta, i-sep)
                            );
                    }
                    prev = sep = i+1;
                    continue;
                }
                if(c=='=') sep = i+1;
            }
            if(prev != i){
                if(sep == i){
                    queryParams.put(new String(chars, prev, i-prev-1),"");
                } else {
                    if(prev == sep)
                        queryParams.put(new String(chars, prev, i-prev),"");
                    else
                        queryParams.put(
                                new String(chars, prev, (delta = sep-prev)-1),
                                new String(chars, prev+delta, i-sep)
                        );
                }
            }
            //if(1==1)return;
        }


        private void readNextNoAdd(int shift){
            int hashCode = 0;
            int prev = i;
            for (;i<limit;i++) {
                char c = chars[i];
                if(c == '/') {
                    if(hashCode==0)return;
                    break;
                }
                if(c == '?'){
                    limit = i;
                    queryParams = new HashMap<>();
                    if(hashCode==0)return;
                    break;
                }
                hashCode = (31 * hashCode + (c & 0xff));
            }
            int delta = i++-prev;
            if(delta>0) {
                lastQ = shift;
                items[shift] = hashCode;
                splat[0] = new String(chars, prev, delta);
            }
        }


        private void readNextAdd(){
            for (int shift = 1; i < limit;) {
                int prev = i;
                for (; i < limit; i++) {
                    char c = chars[i];
                    if (c == '/') break;
                    if (c == '?') {
                        limit = i;
                        queryParams = new HashMap<>();
                        break;
                    }
                }
                if (i != prev) {
                    if (shift == splat.length) {
                        String[] ns = new String[shift + 3];
                        System.arraycopy(splat, 0, ns, 0, splat.length);
                        splat = ns;
                    }
                    splat[shift] = new String(chars, prev, i - prev);
                    i++;
                    ++shift;
                }
                if(queryParams!=null) {
                    parseQParams();
                    return;
                }
            }
            queryParams = Collections.emptyMap();
        }


        public String param(String key){
            return queryParams.get(key);
        }


        public String[] splat(){
            return splat;
        }


        public String afterRoute(){
            if(afterRoute==null) {
                if(splat[1] == null){
                    afterRoute = splat[0];
                    if(afterRoute==null){
                        afterRoute = "";
                    }
                } else {
                    StringBuilder b = new StringBuilder(splat[0]);
                    for (int i = 1; i < splat.length; i++) {
                        if (splat[i] == null) break;
                        b.append("/").append(splat[i]);
                    }
                    afterRoute = b.toString();
                }
            }
            return afterRoute;
        }
    }
}

