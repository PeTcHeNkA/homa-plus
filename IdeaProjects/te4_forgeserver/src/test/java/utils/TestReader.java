package utils;

import plus.fserver.utls.CSVReader;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;


public class TestReader {
    public static void main(String[] args) {
        byte[] bytes = (
                "test1, false \n"+
                "test2, text \n"+
                "test3, true \n"
        ).getBytes(StandardCharsets.UTF_8);

        CSVReader<Object> reader = new CSVReader<>(bytes, objects -> {
            for (Object obj:objects){
                if(obj instanceof  Boolean)obj = "B-"+obj;
                System.out.println("["+obj+"]");
            }
            return objects.length;
        });

        for (Object obj:reader){
            System.out.println("->"+obj);
        }
        System.out.println("NEN");
    }
}
