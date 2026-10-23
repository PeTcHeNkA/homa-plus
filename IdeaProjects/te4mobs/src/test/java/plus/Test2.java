package plus;

public class Test2 {

    public static int INT = 10;

    public static void main(String... args){
        System.out.println("--------------");
        try {
            main();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(){
        System.out.println(Integer.MAX_VALUE*10000);
    }

    private static <T> void insert(Class<?> clazz, String label, T value){
        try {
            clazz.getField(label).set(null, value);
        }catch (Exception e){}
    }
}