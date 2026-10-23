import plus.utl.collections.CircularArrayQueue;


public class TestArr {
    public static void main(String[] args) {
        CircularArrayQueue<Integer> queue = new CircularArrayQueue<>(10);


        for (int i = 0; i < 15; i++) {
            queue.add(i);
        }

        for (int i = 0; i < 10; i++) {
            System.out.println(getS(queue, 2) + ", " + getS(queue, 1) + ", " + getS(queue, 0));
            //System.out.println(queue.pop());

            queue.add(i*10);
        }
    }


    public static String getS(CircularArrayQueue<Integer> queue, int idx){
        Integer result = queue.getAtTail(idx);
        if(result == null){
            return "null";
        }
        return result.toString();
    }
}
