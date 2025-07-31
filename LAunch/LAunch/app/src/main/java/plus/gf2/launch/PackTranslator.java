package plus.gf2.launch;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import plus.proto.LangPackV2;

public class PackTranslator {
    private Translator translator;
    private LangPackV2<?> pack;
    private String cacheFile = "translation_cache.jobj";


    public void setPack(LangPackV2<?> pack) {
        this.pack = pack;
    }


    private void setupTranslator(Consumer<Boolean> future){
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


    private static String translate(String src, int lim, Function<String, String> func){
        return translateItems(slitColor(src), lim, func);
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
        items.add(new Item(1, text.substring(prev, len1)));
        return items;
    }


    private static String splitedTranslate(String text, int lim, Function<String,String> translateFunc) {
        if (text.length() > lim) {
            ArrayList<String> data = new ArrayList<>();
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
                String tText = translateFunc.apply(tTextR);
                data.add(tText);
                if (index == -1) {
                    break;
                }
                pos = index + 1;
            }
            return String.join(" ", data);
        } else {
            return translateFunc.apply(text);
        }
    }


    private static String translateItems(ArrayList<Item> data, int lim, Function<String,String> translateFunc) {
        StringBuilder translated = new StringBuilder();
        for (Item line : data) {
            String text = line.text;
            if (text == null || text.isEmpty()) {
                continue;
            }

            if (line.mode == 0) {
                String tText = splitedTranslate(text, lim, translateFunc);
                if (tText == null || tText.isEmpty()) {
                    translated.append(text);
                } else {
                    if (text.startsWith(" ") && !tText.startsWith(" ")) {
                        tText = " " + tText;
                    }

                    if (text.endsWith(" ") && !tText.endsWith(" ")) {
                        tText = tText + " ";
                    }
                    translated.append(tText);
                }
            } else {
                translated.append(text);
            }
        }
        return translated.toString();
    }


    public static class Item {
        final int mode;
        final String text;

        public Item(int mode, String text) {
            this.mode = mode;
            this.text = text;
        }
    }


    private static class TranslationTask implements Consumer<String>{
        private LangPackV2.Line line;
        private Runnable future;

        public TranslationTask setFuture(Runnable task){
            this.future = task;
            return this;
        }

        @Override
        public void accept(String s) {
            line.actual = s;
            if(future != null){
                future.run();
            }
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
