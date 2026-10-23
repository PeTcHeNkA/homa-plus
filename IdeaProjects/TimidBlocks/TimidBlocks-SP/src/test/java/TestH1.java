import org.vosk.Recognizer;
import plus.tson.utl.Te4HashMap;
import plus.utl.collections.CircularArrayQueue;
import plus.utl.collections.CircularWordList;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestH1 {

    private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private static Te4HashMap<String,String> lib = new Te4HashMap<>();
    private CircularArrayQueue<String> words = new CircularArrayQueue<>(10);

    public void handleData() {
        synchronized (this) {
            while (!queue.isEmpty()) {
                String aiAnswerpartial = queue.poll();
                String aiAnswertext = null;
                //AIAnswer aiAnswer = new AIAnswer();
                //aiAnswer.partial = queue.poll();

                CircularArrayQueue<String> words;
                String result;

                if(aiAnswerpartial != null && !aiAnswerpartial.isEmpty()){
                    words = new CircularArrayQueue<>(0);
                    this.words.copyTo(words);
                    result = aiAnswerpartial;
                } else {
                    words = this.words;
                    result = aiAnswertext;
                }

                if (!result.isEmpty()) {
                    if(true){
                        System.out.println("SND:"+result);
                    }

                    String[] words2 = result.split(" ");
                    int count = 0;

                    StringBuilder sb = null;

                    l1: while (count < words2.length) {
                        if(sb == null){
                            sb = new StringBuilder();
                        } else {
                            sb.setLength(0);
                        }

                        words.add(words2[count++]);

                        int cnt = words.size();

                        Te4HashMap<String, String> wordData = lib;

                        int prev = -1;
                        while (cnt >= 0) {
                            for (int i = 0; i < cnt; i++) {
                                String word = words.getAtHead(i);
                                if(word != null) {
                                    sb.append(word).append(' ');
                                } else break;
                            }

                            if(sb.length() > 0){
                                sb.setLength(sb.length()-1);

                                String block = wordData.get(sb.toString());

                                if(block != null) {
                                    System.out.println("BLOCK:"+block);
                                    words.clear();
                                    continue l1;
                                }
                                sb.setLength(0);
                            }
                            --cnt;
                        }
                    }
                }
            }
        }
    }


    public static void test(String... s) {
        TestH1 t1 = new TestH1();
        t1.queue.addAll(Arrays.asList(s));
        t1.handleData();
    }


    public static void main(String[] args) {
//        lib.fput("камень", "1");
//        lib.fput("трава", "2");
//
//        test("трава зеленый камень");
//        //test("камень", "ножнцы", "бумага", "трава камень", "зеленый камень");

        test2();
    }


    public static void test2(){
        CircularWordList lst = new CircularWordList(5);

        lst.addWords("камень ножницы бумага котлета");
        C2Iter iter = new C2Iter(lst);

        iter.forEach(System.out::println);
    }

}