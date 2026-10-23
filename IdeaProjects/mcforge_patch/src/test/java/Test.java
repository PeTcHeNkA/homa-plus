import LZMA.LzmaInputStream;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

public class Test {


    public static void main(String[] args) throws IOException {

        //InputStream binpatchesCompressed = Test.class.getResourceAsStream("/binpatches.pack.lzma");
        InputStream binpatchesCompressed = new FileInputStream("binpatches.pack.lzma");

        try (LzmaInputStream binpatchesDecompressed = new LzmaInputStream(binpatchesCompressed))
        {
            ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
            try (JarOutputStream jos = new JarOutputStream(jarBytes))
            {

                Pack200.newUnpacker().unpack(binpatchesDecompressed, jos);
                JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes.toByteArray()));

                JarOutputStream out = new JarOutputStream(new FileOutputStream("forgebinpatches.jar"));

                copyFiles(jis, out);

                out.close();

            }
        }
        binpatchesCompressed.close();

    }


    private static void copyFiles(JarInputStream in, JarOutputStream out)
            throws IOException {
        JarEntry inEntry;
        byte[] buffer = new byte[4096];

        while ((inEntry=(JarEntry)in.getNextEntry())!=null)
        {if (inEntry.getMethod()==0)
        {out.putNextEntry(new JarEntry(inEntry));
        }
        else {out.putNextEntry(new JarEntry(inEntry.getName()));
        }
            int num;
            int len;
            while ((len=in.read(buffer))>0)
            {out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

}
