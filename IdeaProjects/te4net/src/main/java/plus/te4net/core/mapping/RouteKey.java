package plus.te4net.core.mapping;


class RouteKey {
    protected final int[] items = new int[6];

    public RouteKey(){}


    public RouteKey(String key){
        char[] chars = key.toCharArray();
        int hashCode = 0;
        int shift = 0;
        for (char c : chars) {
            if (c == '?') break;
            if (c == '/') {
                if (shift > 5) return;
                items[shift++] = hashCode;
                hashCode = 0;
                continue;
            }
            hashCode = (31 * hashCode + (c & 0xff));
        }
        if (shift < 6) items[shift] = hashCode;
    }


    protected int code(int i){
        return items[i];
    }


    @Override
    public String toString() {
        return "["+items[0]+", "+items[1]+", "+items[2]+", "+items[3]+", "+items[4]+", "+items[5]+"]";
    }
}