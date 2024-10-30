package plus.fserver.plugin;

import sun.misc.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;


public class MemoryClassLoader{
    public static Class<?> define(ClassLoader loader, String name, byte[] bytes){
        try {
            return loadUnsafe(loader, name, bytes);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    static Class<?> loadUnsafe(ClassLoader loader, String name, byte[] data) throws Exception{
        Method method = URLClassLoader.class.getDeclaredMethod("defineClass",
                String.class, Resource.class);

        method.setAccessible(true);

        return (Class<?>) method.invoke(loader, name, new MemorySrc(data));
    }


    static class MemorySrc extends Resource{
        private final byte[] bytes;

        MemorySrc(byte[] bytes) {
            this.bytes = bytes;
        }
        @Override
        public String getName() {
            return "";
        }
        @Override
        public URL getURL() {
            return null;
        }
        @Override
        public URL getCodeSourceURL() {
            try {
                return new URL("memory", "memory", 0, "memory", new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u) {
                        return new URLConnection(u) {
                            @Override
                            public void connect() {}
                            @Override
                            public InputStream getInputStream() throws IOException {
                                return MemorySrc.this.getInputStream();
                            }
                        };
                    }
                });
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public byte[] getBytes() {
            return bytes;
        }
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }
        @Override
        public int getContentLength() {
            return bytes.length;
        }
    }
}