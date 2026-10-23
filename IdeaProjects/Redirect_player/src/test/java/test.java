public class test {

    public static void main(String... args){
        v1 v = new v1() {
            @Override
            void test() {
                //super.test(); <- не должно быть
                System.out.println("2");
            }
        };
        v.test();
    }
}
abstract class v1{
    void test(){
        System.out.println("1");//должно печатать всегда
    }
}