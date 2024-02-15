package plus.fserver.server.zstd;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.ServerCommand;
import plus.tson.TsonFile;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class MapCoder {
    public static boolean disabled = false;
    private static boolean train = false;
    private static ZStdCoder coder = new ZStdCoder(1024 * 16, 7);
    private static File keyFile;

    public static void loadSettings(File file, int size, int lvl){
        keyFile = file;
        coder = new ZStdCoder(size, lvl).loadKey(TsonFile.readFile(file));
    }

    static {
        MapCoder.loadSettings(
                new File(Loader.instance().getConfigDir(), "ZSTD_KEY.key"),
                1024 * 16, 7
        );
        initCommands();
    }

    public static void initCommands(){
        new ServerCommand("zstd-save", "train/complete", 4) {
            private final List<String> completions = Arrays.asList(
                    "train", "complete"
            );
            private final List<String> completions2 = Arrays.asList(
                    "start", "info", "pause", "clean"
            );
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length<1){
                    reply(sender, "Args should be more 1");
                    return;
                }
                String cmd = args[0];
                if(cmd.equals("train")){
                    if(args.length<2){
                        reply(sender, "Args should be more 2! (start|info|pause)");
                        return;
                    }
                    cmd = args[1];

                    switch (cmd){
                        case "start":
                            reply(sender, "Start training...");
                            train = true;
                            return;
                        case "info":
                            reply(sender,
                                    "Training:    "+(train?"enabled":"disabled")
                                      +"\nSamples:    "+coder.samples()
                                      +"\nTotal size:  "+coder.totalSize()/(8<<10)+"kb"
                            );
                            return;
                        case "pause":
                            train = false;
                            reply(sender, "Training paused");
                            return;
                        case "clean":
                            coder.clean();
                            reply(sender, "All samples cleared");
                            return;
                    }
                } else if (cmd.equals("complete")) {
                    train = false;
                    reply(sender, "Start compile samples...");
                    coder.train();
                    reply(sender, "Saving key...");
                    dumpKey();
                    reply(sender, "Success!");
                }
            }

            @Override
            public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
                if(args.length==1 && args[0].isEmpty()) return completions;
                if(args.length==2 && args[0].equals("train")) {
                    return completions2;
                }
                if(args.length>2) return empty;
                return completions;
            }
        }.register();
    }

    public static void dumpKey(){
        byte[] key = coder.dumpKey();
        if(key==null)return;
        TsonFile.write(keyFile, key);
    }

    public static void startTrain(){
        train = true;
    }

    public static void completeTrain(){
        train = false;
        coder.train();
    }


    public static OutputStream newOStream(OutputStream stream) throws IOException {
        if(disabled)return new GZIPOutputStream(stream);
        return coder.compress(train?new TrainProxy(stream):stream);
    }


    public static InputStream newIStream(InputStream stream) throws IOException {
        if(disabled)return new GZIPInputStream(stream);
        return coder.decompress(stream);
    }


    private static final class TrainProxy extends OutputStream{
        private final OutputStream parent;
        private final ByteArrayOutputStream cache = new ByteArrayOutputStream();

        private TrainProxy(OutputStream parent) {
            this.parent = parent;
        }


        @Override
        public void write(int b) throws IOException {
            parent.write(b);
            cache.write(b);
        }


        @Override
        public void write(@NotNull byte[] b) throws IOException {
            parent.write(b);
            cache.write(b);
        }


        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            parent.write(b, off, len);
            cache.write(b, off, len);
        }


        @Override
        public void close() throws IOException {
            super.close();
            parent.close();
            cache.close();
            coder.feed(cache.toByteArray());
        }
    }
}