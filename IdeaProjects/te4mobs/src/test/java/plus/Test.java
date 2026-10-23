package plus;


import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModClassLoader;
import org.apache.commons.io.output.StringBuilderWriter;
import plus.mobs.utl.MobCompiler;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;

public class Test {


    public static void main(String... args){
        System.out.println("--------------");
        try {
            main();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main() throws IOException, InstantiationException, IllegalAccessException {
        String className = "test.java";
        String code = "package ex;public class test{public test(){System.out.println(1);}}";

        MemoryJavaCompiler comp = new MemoryJavaCompiler();
        Map<String, byte[]> result = comp.compile(className, code);
        System.out.println(result.get("ex.test"));

    }


    public static class memLoader extends ClassLoader{
//        //p
//        public void test(){
//            defineClass()
//        }
    }


    public static Class<?> load(Path path, String name){
        try {
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(
                    new URL[]{path.toUri().toURL()}
            );
            return urlClassLoader.loadClass(name);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }



    public static Path compile(String className, String code){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File file = new File("temp");
        if(!file.exists()){
            file.mkdirs();
        }
        Path output = file.toPath();


        JavaCompiler.CompilationTask task = compiler.getTask(
                null, null,null,
                Arrays.asList("-d", output.toAbsolutePath().toString()),
                null,
                singletonList(
                        new SimpleJavaFileObject(
                                URI.create("string:///" + className.replace('.', '/') +
                                        JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {

                            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                                return code;
                            }
                        })
        );

        boolean result = task.call();

        //System.out.println(w.toString());

        if (!result) throw new RuntimeException("Compilation error!");
        return output;
    }

}
