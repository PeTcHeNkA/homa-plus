package plus.lyn;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.lyn.utl.Statister;
import plus.lyn.utl.UnLinker;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;
import plus.tson.utl.TupleMutable;

import java.util.Arrays;
import java.util.Map;


public class SuffixIndex {
    public static final class SuffixList{
        public final Suffix.Type type;
        public final WordPartIndex.SPart[] parts;
        public final String[] suffixes;

        public SuffixList(Suffix.Type type, WordPartIndex.SPart[] parts, String[] suffixes) {
            this.type = type;
            this.suffixes = suffixes;
            this.parts = parts;
        }
    }


    protected static SuffixList of(WordPartIndex.SPart part, Suffix.Type type, String... s){
        return new SuffixList(type, new WordPartIndex.SPart[]{part}, s);
    }


    protected static SuffixList of(WordPartIndex.SPart[] parts, Suffix.Type type, String... s){
        return new SuffixList(type, parts, s);
    }


    protected static Suffix ofStr(UnLinker<IdxString> linker, Suffix.Type type, String str, WordPartIndex.SPart[] parts, int[] weights){
        boolean mayEnd = false;
        if(str.charAt(str.length()-1) == '-'){
            mayEnd = true;
            str = str.substring(1, str.length()-1);
        } else {
            str = str.substring(1);
        }
        if(weights.length == 1)weights = WordPartIndex.emptyWeights;
        if(str.length()>6)return null;
        return new Suffix(linker.unLink(IdxString.of(str)), parts, weights, mayEnd, type);
    }


    //Степень сравнения причастий, наречий и прилагательных на -о и -е
    public static SuffixList ADJECTIVE_COMPARING_SUFFIXES = of(new WordPartIndex.SPart[]{
                    WordPartIndex.ADJECTIVE, WordPartIndex.PARTICIPLE, WordPartIndex.ADVERB
            }, Suffix.Type.ETC,
            "-айш-", "-е-", "-ее-", "-ей-", "-ейш-", "-же-", "-ше-"
    );

    //Прошедшее время глаголов
    public static SuffixList VERB_PAST_SUFFIXES = of(WordPartIndex.VERB, Suffix.Type.ETC,
            "-л-"
    );
    //Неопределённая форма глагола
    public static SuffixList VERB_UNDEFINED_FORM_SUFFIXES = of(VERB_PAST_SUFFIXES.parts, Suffix.Type.ETC,
            "-ти", "-ть"
    );
    //Повелительное наклонение глагола
    public static SuffixList VERB_IMPERATIVE_MOOD_SUFFIXES = of(VERB_PAST_SUFFIXES.parts, Suffix.Type.ETC,
            "-и"
    );
    //Действительные причастия
    public static SuffixList PARTICIPLE_ACTIVE_SUFFIXES = of(WordPartIndex.PARTICIPLE, Suffix.Type.ETC,
            "-ащ-", "-ящ-", "-вш-", "-ш-", "-ущ-", "-ющ-"
    );
    //Страдательные причастия
    public static SuffixList PARTICIPLE_PASSIVE_SUFFIXES = of(PARTICIPLE_ACTIVE_SUFFIXES.parts, Suffix.Type.ETC,
            "-ем-", "-им-", "-ом-", "-нн-", "-енн-", "-т-"
    );
    //Деепричастия
    public static SuffixList DEE_PARTICIPLE_SUFFIXES = of(WordPartIndex.DEE_PARTICIPLE, Suffix.Type.ETC,
            "-а-", "-я-", "-учи-", "-ючи-", "-вши-", "-ши-"
    );
    //Множественное число существительных
    public static SuffixList NOUN_MULTI_SUFFIXES = of(WordPartIndex.NOUN, Suffix.Type.ETC,
            "-ес-", "-ен-", "-ер-", "-й-"
    );
    //Степень превосходства причастий, прилагательных и наречий
    public static SuffixList ADJECTIVE_BEST_SUFFIXES = of(ADJECTIVE_COMPARING_SUFFIXES.parts, Suffix.Type.ETC,
            "-ейш-", "-айш-"
    );

    //Оттенки значения
    //Суффиксы существительных
    public static SuffixList C_NOUN_MIN_SUFFIXES = of(NOUN_MULTI_SUFFIXES.parts, Suffix.Type.COLOR,
            "-кос-", "-кас-"
    );
    public static SuffixList C_NOUN_LIKE_SUFFIXES = of(NOUN_MULTI_SUFFIXES.parts, Suffix.Type.COLOR,
            "-ик-", "-ек-", "-ок-", "-чик", "-еньк-", "-оньк-", "-ечк-", "-ичк-", "-очк-", "-ашк-", "-ишк-", "-ушк-",
            "-юшк-", "-ышк-", "-ец-", "-иц-", "-енк-", "-инк-", "-онк-", "-ин-", "-ищ-", "-ушек", "-ышек", "-еныш"
    );
    //Суффиксы прилагательных
    public static SuffixList C_ADJECTIVE_SUFFIXES = of(WordPartIndex.ADJECTIVE, Suffix.Type.COLOR,
            "-еньк-", "-оньк-", "-ехоньк-", "-оханьк-", "-ешеньк-", "-ошеньк-", "-ущ-", "-ющ-", "-юсеньк-", "-енн-", "-оват-"
    );
    //Суффиксы наречий
    public static SuffixList C_ADVERB = of(WordPartIndex.ADVERB, Suffix.Type.COLOR,
            "-еньк-", "-оньк-", "-енечко", "-онечко", "-еват", "-оват"
    );

    //Словообразующие суффиксы
    //Суффиксы существительных:
    public static SuffixList B_NOUN_SUFFIXES = of(NOUN_MULTI_SUFFIXES.parts, Suffix.Type.BUILDING,
            "-тель", "-чик", "-щик", "-ник", "-ниц-", "-к-", "-иц-", "-юх", "-енок", "-ушк-", "-ышк-","-ость",
            "-як", "-ун", "-ач", "-ущ-", "-кос-", "-кас-"
    );
    //Суффиксы прилагательных
    public static SuffixList B_ADJECTIVE_SUFFIXES = of(C_ADJECTIVE_SUFFIXES.parts, Suffix.Type.BUILDING,
            "-ив-", "-чив-", "-лив-", "-ист-", "-ск-", "-ов-", "-ев-", "-н-", "-евит-", "-ин-"
    );
    //Суффиксы глаголов
    public static SuffixList B_VERB_SUFFIXES = of(WordPartIndex.VERB, Suffix.Type.BUILDING,
            "-ова-", "-ева-", "-ыва-", "-и-", "-я-", "-е-", "-а-", "-ва-" //??
    );
    //Наречные суффиксы
    public static SuffixList B_ADVERB_SUFFIXES = of(C_ADVERB.parts, Suffix.Type.BUILDING,
            "-а", "-о", "-ю"
    );

    public static SuffixList[] ALL_SUFFIXES = new SuffixList[]{
            NOUN_MULTI_SUFFIXES,
            ADJECTIVE_COMPARING_SUFFIXES,
            ADJECTIVE_BEST_SUFFIXES,
            VERB_PAST_SUFFIXES,
            VERB_IMPERATIVE_MOOD_SUFFIXES,
            VERB_UNDEFINED_FORM_SUFFIXES,
            PARTICIPLE_PASSIVE_SUFFIXES,
            PARTICIPLE_ACTIVE_SUFFIXES,
            DEE_PARTICIPLE_SUFFIXES,
            C_ADVERB,
            C_NOUN_LIKE_SUFFIXES,
            C_ADJECTIVE_SUFFIXES,
            C_NOUN_MIN_SUFFIXES,
            B_NOUN_SUFFIXES,
            B_ADJECTIVE_SUFFIXES,
            B_VERB_SUFFIXES,
            B_ADVERB_SUFFIXES
    };


    public static final SuffixIndex inst = new SuffixIndex();
    protected final Int2ObjectOpenHashMap<Suffix>[] index;

    private SuffixIndex(){
        index = fillIndex();
    }


    private Int2ObjectOpenHashMap<Suffix>[] fillIndex(){
        UnLinker<IdxString> linker = new UnLinker<>();
        Te4HashMap<String, Statister<SuffixList>> lin2 = new Te4HashMap<>();

        for (SuffixList sfxList:ALL_SUFFIXES){
            for (String sfx:sfxList.suffixes){
                Statister<SuffixList> statister = lin2.get(sfx);
                if(statister == null){
                    lin2.fput(sfx, statister = new Statister<>());
                }
                statister.feed(sfxList);
            }
        }

        TupleMutable<SuffixList[],int[]> field = new TupleMutable<>(new SuffixList[]{}, new int[]{});
        TupleMutable<WordPartIndex.SPart[],int[]> field2 = new TupleMutable<>(new WordPartIndex.SPart[]{}, new int[]{});

        Te4HashMap<WordPartIndex.SPart, Te4HashSet<Suffix>> suffixes = new Te4HashMap<>();


//        ArrayList<WerbPartIndex.SPart> allParts  = new ArrayList<>();
//        IntArrayList                   allWeighs = new IntArrayList();
        //Te4HashMap<WerbPartIndex.SPart, AtomicInteger> allParts = new Te4HashMap<>();
        Statister<WordPartIndex.SPart> allParts = new Statister<>();
        Int2ObjectOpenHashMap<Suffix>[] preIndex = new Int2ObjectOpenHashMap[]{};


        for (Map.Entry<String, Statister<SuffixList>> entry:lin2.entrySet()){
            entry.getValue().dump(field);
            String sfxName = entry.getKey();

            Suffix.Type type = ofStats(field);
            allParts.clear();

            for (int i=0;i<field.A.length;i++){
                int boost = field.B[i];
                for (WordPartIndex.SPart part:field.A[i].parts){
                    allParts.feed(part, boost);
                }
            }
            allParts.dump(field2);

            Suffix suffix = ofStr(linker, type, sfxName, field2.A, field2.B);
            if(suffix==null){
                System.err.println("Illegal suffix ["+sfxName+"]! length > 6!");
                continue;
            }

            Int2ObjectOpenHashMap<Suffix> curIdxList = null;
            if(suffix.length() > preIndex.length){
                preIndex = Arrays.copyOf(preIndex, suffix.length());
            } else {
                curIdxList = preIndex[suffix.length()-1];
            }
            if(curIdxList == null){
                curIdxList = preIndex[suffix.length()-1] = new Int2ObjectOpenHashMap<>();
            }

            curIdxList.put(suffix.str.index, suffix);

            for (SuffixList list: field.A){
                for (WordPartIndex.SPart part: list.parts){

                    Te4HashSet<Suffix> sfx2 = suffixes.get(part);
                    if(sfx2 == null){
                        suffixes.fput(part, sfx2 = new Te4HashSet<>());
                    }
                    sfx2.add(suffix);
                }
            }
        }

        for (Map.Entry<WordPartIndex.SPart, Te4HashSet<Suffix>> entry: suffixes.entrySet()){
            entry.getKey().suffixes = entry.getValue().toArray(new Suffix[0]);
        }

        for (int i=0;i<preIndex.length;i++){
            if(preIndex[i] == null){
                preIndex[i] = new Int2ObjectOpenHashMap<>();
            } else {
                preIndex[i].trim();
            }
        }
        return preIndex;
    }


    private static Suffix.Type ofStats(TupleMutable<SuffixList[],int[]> field){
        if(field.A.length == 1){
            field.B = WordPartIndex.emptyWeights;
            return field.A[0].type;
        }

        int maxValue = 1;
        Suffix.Type curType = field.A[0].type;

        for (int cursor = 1;cursor<field.A.length;++cursor){
            int num = field.B[cursor];
            if(num>maxValue){
                maxValue = num;
                curType = field.A[cursor].type;
            } else if (num==maxValue && curType == Suffix.Type.ETC){
                curType = field.A[cursor].type;
            }
        }
        return curType;
    }
}
