package plus.fserver.utls.collections;

import net.minecraft.server.management.PlayerChunkMapEntry;


public class ConCurMapEntrySet {
    private Node[] nodes;
    private int mask = 127;
    private int count = 0;

    public ConCurMapEntrySet(){
        Node[] nodes = this.nodes = new Node[128];
        for (int i = 0, s = nodes.length; i < s; i++) {
            nodes[i] = new Node();
        }
    }

    final int hash(int posX, int posZ){
        return (posX * 31 + posZ) & mask;
    }

    private static class Node{
        private PlayerChunkMapEntry entry;
        private Node next;
    }


    public void add(PlayerChunkMapEntry entry){
        Node node = nodes[hash(entry.posX, entry.posZ)];
        if(node.entry == entry)return;
        synchronized (node){
            Node wNode = node;
            while (true) {
                Node cur = wNode.next;
                if (cur == null) {
                    cur = new Node();
                    cur.entry = entry;
                    wNode.next = cur;
                    count++;
                    return;
                } else {
                    if (cur.entry == entry) return;
                    wNode = cur;
                }
            }
        }
    }


    public void remove(PlayerChunkMapEntry entry){
        Node node = nodes[hash(entry.posX, entry.posZ)];
        if(node.entry == entry)return;
        synchronized (node){
            Node wNode = node;
            while (true) {
                Node cur = wNode.next;
                if (cur == null) return;
                if (cur.entry == entry) {
                    wNode.next = cur.next;
                    count--;
                    return;
                } else {
                    wNode = cur;
                }
            }
        }
    }


    public void updateAndClear(){
        Node[] nodes = this.nodes;
        for (int i = 0, s = nodes.length; i < s; i++) {
            Node node = nodes[i];
            if(node.entry == null)continue;
            synchronized (node){
                node.entry.update();
                node.entry = null;
                Node next = node.next;
                node.next = null;
                count--;
                while (next != null) {
                    next.entry.update();
                    next = next.next;
                    count--;
                }
            }
        }
    }


    public int size(){
        return count;
    }


    public boolean isEmpty(){
        return count == 0;
    }
}