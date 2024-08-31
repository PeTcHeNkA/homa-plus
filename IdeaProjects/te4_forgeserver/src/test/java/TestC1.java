public class TestC1 {
    public static void main(String[] args) {
        String[] items = new String[]{"A", "B", "C", "D"};

        for (int i = 0, s = items.length; i < s; i++){
            for (int j = i + 1; j < s; j++){
                System.out.println(items[i]+items[j]);
            }
        }



    }
}
