package col;

import plus.fserver.utls.collections.Int1LinkedList;


public class TestIntList {
    public static void main(String[] args) {
        Int1LinkedList list = new Int1LinkedList();

        list.add(1);
        list.add(2);
        list.add(3);

//        list.removeVal(2);
//        System.out.println(list);
//        list.removeVal(1);
//        System.out.println(list);
//        list.removeVal(3);
//        System.out.println(list);

        list.resetIter();
        while (list.hasCur()){
            System.out.println(list.getCur());
            list.removeCur();
        }
        list.resetIter();

        System.out.println(list);


    }
}
