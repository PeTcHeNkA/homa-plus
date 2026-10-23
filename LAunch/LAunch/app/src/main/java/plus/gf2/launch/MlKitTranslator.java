package plus.gf2.launch;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import plus.proto.LangPackV2;
import plus.tson.TJsonFile;


public class MlKitTranslator {
    private Translator translator;
    private LangPackV2<?> pack;
    private String cacheFile = "translation_cache.jobj";



    public void setPack(LangPackV2<?> pack) {
        this.pack = pack;
    }


    public void setupTranslator(Consumer<Boolean> future){
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.RUSSIAN)
                .setExecutor(Executors.newFixedThreadPool(8))
                .build();
        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded().addOnSuccessListener(unused -> {
            future.accept(Boolean.TRUE);
        }).addOnFailureListener(e -> {
            future.accept(Boolean.FALSE);
        });
    }


    public ITranslationCallback translateAll(MainActivity act, Runnable future){
        TranslationTask head = null;
        TranslationTask lastTask = null;
        TJsonFile cache = new TJsonFile(new File(act.getFilesDir(), cacheFile), false);
        try {
            cache.load();
        } catch (Exception e){}

        TranslationGroup group = new TranslationGroup();
        group.cache = cache;
        TranslateFunc cFunc = new TranslateFunc() {
            @Override
            public void translate(String src, Consumer<String> future) {
                if (!trs1.matcher(src).find()) {
                    String cached = cache.getStrSafe(src);
                    if (cached != null) {
                        future.accept(cached);
                        return;
                    }
                    MlKitTranslator.this.translate(src, new Consumer<>() {
                        @Override
                        public void accept(String s) {
                            synchronized (this) {
                                cache.put(src, s);
                            }
                            future.accept(s);
                        }
                    });
                } else {
                    future.accept(src);
                }
            }
        };
        group.func = (src, future1) -> translateItems(slitColor(src), 1000, cFunc, future1);

        int size = pack.size();
        for (int i = 0; i < size; i++) {
            LangPackV2.Line line = pack.getLineByIndex(i);

            if(line.actual == null){
                if(isTranslatable(line.src)){
                    String cached = cache.getStrSafe(line.src);
                    if(cached != null){
                        line.actual = cached;
                    } else {
                        TranslationTask newTask = new TranslationTask(group, line);
                        group.total += 1;
                        if(lastTask != null){
                            lastTask.setFuture(newTask);
                        }
                        lastTask = newTask;
                        if(head == null){
                            head = lastTask;
                        }
                    }
                }
            }
        }

        if(head != null){
            lastTask.setFuture(() -> {
                group.completed = true;
                cache.save();
                future.run();
            });
            head.run();
        } else {
            group.completed = true;
            future.run();
        }

        return group;
    }


    private static ArrayList<Item> slitColor(String text) {
        ArrayList<Item> items = new ArrayList<>();
        int len1 = text.length();
        int prev = 0;
        int i = 0;

        while (i < len1) {
            char chr = text.charAt(i);
            i++;

            if (chr == '<') {
                items.add(new Item(0, text.substring(prev, i - 1)));
                for (int j = i; j < len1; j++) {
                    char chr2 = text.charAt(j);

                    if (chr2 == '>') {
                        items.add(new Item(1, text.substring(i - 1, j + 1)));
                        prev = j + 1;
                        i = j + 1;
                        break;
                    }
                }
            }
        }
        if(prev != len1)
            items.add(new Item(0, text.substring(prev, len1)));
        return items;
    }


    private void splitedTranslate(String text, int lim, TranslateFunc func, Consumer<String> future) {
        if (text.length() > lim) {
            LinkedList<String> chunks = new LinkedList<>();

            int pos = 0;
            while (pos != -1) {
                int index;
                if (pos + lim > text.length()) {
                    index = -1;
                } else {
                    index = text.lastIndexOf(" ", pos + lim);
                }
                String tTextR;
                if (index == -1) {
                    tTextR = text.substring(pos);
                } else {
                    tTextR = text.substring(pos, index);
                }

                chunks.add(tTextR);

                if (index == -1) {
                    break;
                }
                pos = index + 1;
            }
            Consumer<String> future1 = new Consumer<>() {
                final StringBuilder builder = new StringBuilder();

                @Override
                public void accept(String s) {
                    builder.append(s);
                    if (chunks.isEmpty()) {
                        future.accept(builder.toString());
                    } else {
                        builder.append(" ");
                        translate(chunks.removeFirst(), this);
                    }
                }
            };
            translate(chunks.removeFirst(), future1);
        } else {
            func.translate(text, future);
        }
    }


    interface TranslateFunc{
        void translate(String src, Consumer<String> future);
    }


    private void translate(String src, Consumer<String> future){
        //if(!trs1.matcher(src).find()){
            translator.translate(src)
                    .addOnSuccessListener(future::accept)
                    .addOnFailureListener(e -> future.accept(src))
                    .addOnCanceledListener(() -> future.accept(src));
//        } else {
//            future.accept(src);
//        }
    }


    private void translateItems(ArrayList<Item> data, int lim, TranslateFunc func, Consumer<String> future) {
        LinkedList<Item> chunks = new LinkedList<>(data);
        Consumer<String> future1 = new Consumer<>() {
            final StringBuilder builder = new StringBuilder();
            String curText = null;
            @Override
            public void accept(String s) {
                if (curText != null) {
                    if (curText.startsWith(" ") && !s.startsWith(" ")) {
                        builder.append(" ");
                    }
                    builder.append(s);
                    if (curText.endsWith(" ") && !s.endsWith(" ")) {
                        builder.append(" ");
                    }
                } else {
                    builder.append(s);
                }
                if (chunks.isEmpty()) {
                    future.accept(builder.toString());
                } else {
                    Item next = chunks.removeFirst();

                    if (next.mode == 0) {
                        splitedTranslate(curText = next.text, lim, func, this);
                    } else {
                        this.accept(next.text);
                    }
                }
            }
        };
        future1.accept("");
    }


    private static class Item {
        final int mode;
        final String text;

        public Item(int mode, String text) {
            this.mode = mode;
            this.text = text;
        }
    }


    public static class TranslationGroup implements ITranslationCallback{
        private int total;
        private int count;
        private TranslateFunc func;
        private TJsonFile cache;
        private boolean completed = false;

        public int getCurrent() {
            return count;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }
    }


    private class TranslationTask implements Consumer<String>, Runnable{
        final TranslationGroup group;
        final LangPackV2.Line line;
        Runnable future;

        private TranslationTask(TranslationGroup group, LangPackV2.Line line) {
            this.group = group;
            this.line = line;
        }

        public TranslationTask setFuture(Runnable task){
            this.future = task;
            return this;
        }

        @Override
        public void accept(String s) {
            line.actual = s;
            group.count += 1;

            if(group.count % 250 == 0){
                group.cache.save();
            }
            if(future != null){
                future.run();
            }
        }

        @Override
        public void run() {
            group.func.translate(line.src, this);
        }
    }



    private static final Pattern trs1 = Pattern.compile("^(?:[^a-zA-Z]*|[^a-zA-Z]*[a-zA-Z][^a-zA-Z]*)$");
    private static final Pattern trs2 = Pattern.compile("[A-Z][A-Z]-[1-9]-[1-9]");

    private static boolean isTranslatable(String key){
        if(key == null || key.isBlank())return false;
        if(key.length() == 1)return false;
        if(trs2.matcher(key).find())return false;
        return !trs1.matcher(key).find();
    }
}
