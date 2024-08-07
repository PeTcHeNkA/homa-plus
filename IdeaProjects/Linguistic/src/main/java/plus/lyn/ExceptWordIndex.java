package plus.lyn;

import plus.lyn.utl.WordLib;
import java.util.ArrayList;
import java.util.Arrays;


public class ExceptWordIndex {
    public WordLib[] index = new IdxBuilder()
            .use(WordPartIndex.UNION).add(
                    "и", "как", "так", "столько", "или", "либо", "то", "а",
                    "есть", "именно", "что", "чтобы", "когда", "лишь", "едва", "если", "коли", "ежели",
                    "где", "куда", "откуда", "такой", "настолько", "будто", "словно", "точно"
            )
            .use(WordPartIndex.PREPOSITION).add(
                    "в", "без", "до", "из", "к", "на", "по", "о", "от", "перед", "при", "чрез", "с", "у", "за",
                    "над", "об", "под", "про", "для", "вблизи", "вглубь", "вдоль", "возле", "около", "вокруг", "впереди",
                    "после"
            )
            .use(WordPartIndex.PARTICLE).add(
                    "пусть", "пускай", "давайте", "да", "давай", "бы", "б", "бывало",
                    "не", "нет", "никак", "ли", "неужели", "даже", "ни", "же", "ведь",
                    "уж", "все"
            )
            .build();

    public static final ExceptWordIndex inst = new ExceptWordIndex();


    public static Word of(WordPartIndex.SPart part, Case cs, String root){
        Word werb = new Word(root, null, null, root, null, null, null);
        werb.part = part;
        werb.cs   = cs;
        return werb;
    }

    static class IdxBuilder {
        private final ArrayList<Word> werbs = new ArrayList<>();
        private WordPartIndex.SPart part;
        private Case aCase;

        public IdxBuilder use(WordPartIndex.SPart part){
            this.part = part;
            this.aCase = null;
            return this;
        }
        public IdxBuilder use(Case aCase){
            this.aCase = aCase;
            this.part  = aCase.part().part();
            return this;
        }
        public IdxBuilder add(String... str){
            for (String str0:str) werbs.add(of(part, aCase, str0));
            return this;
        }

        WordLib[] build(){
            WordLib[] libs = new WordLib[]{};
            for (Word werb:werbs){
                int ln = werb.length();
                if(ln >= libs.length){
                    int pre = libs.length;
                    libs = Arrays.copyOf(libs, ln);
                    while (pre<ln) libs[pre++] = new WordLib();
                }
                libs[ln-1].fput(werb);
            }
            return libs;
        }
    }


    private ExceptWordIndex(){}
}
