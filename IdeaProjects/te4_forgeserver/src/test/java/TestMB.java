import plus.fserver.ElcaMobs;

public class TestMB {


    public static void main(String[] args) {
        System.out.println(ElcaMobs.GLOBAL.low);
    }



    static void init(Class c) {
        try {
            c.getClassLoader().loadClass(c.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
