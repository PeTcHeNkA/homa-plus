package test;

import plus.nlaunch.NLaunch;
import plus.tson.TsonClass;
import plus.tson.TsonMap;

import java.util.function.Consumer;

public class TestProtected {
    static NLaunch launch = new NLaunch(TestProtected.class.getClassLoader());

    public static void main(String[] args) {
        System.out.println(System.setProperty("file.encoding", "NULL"));
        //Cp1251
//        String str = "TEXT";
//        boolean b = str.equals("V");
//        System.out.println(b);

        System.load("C:\\Users\\Te4hnoPlus\\IdeaProjects\\HelloNL_SPIGOT\\protected\\src\\test\\resources\\nlaunch.dll");

        //System.load("C:\\Users\\Te4hnoPlus\\IdeaProjects\\HelloNL_SPIGOT\\main\\src\\main\\resources\\nlaunch.dll");

        load().accept(new TsonMap());
    }


    private static Consumer<TsonMap> load(){
        try {
            Class clazz = Class.forName("plus.pro.Main", true, launch);
            if (clazz == null) throw new RuntimeException("Class not found");
            return (Consumer<TsonMap>) new TsonClass(clazz).createInst();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
