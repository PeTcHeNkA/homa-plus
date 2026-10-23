package utils;

public class test3 {
    public static void main(String[] args) {
        VPacketBuf buf = new VPacketBuf(10);
        buf.add("a1");
        buf.add("a2");
        buf.sendOnIter(new String[]{"P1","P2"});
    }

    static class VPacketBuf {
        private String[] packets;
        private int cursor;

        public VPacketBuf(int size) {
            packets = new String[size];
        }

        public void add(String packet){
            packets[cursor++] = packet;
        }

        public void sendOnIter(String[] data){
            for (String player:data){
                for (int i=0;i<cursor;i++){
                    System.out.println(player+":"+packets[i]);
                }
            }
            cursor = 0;
        }
    }
}
