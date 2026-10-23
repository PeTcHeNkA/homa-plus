package plus.lyn;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.lyn.utl.WordLib;


public class WordBuilder {
    private final WordLib.WKey eKey         = new WordLib.WKey();
    private int[] alloc  = new int[32];
    private int[] cIndex = new int[32];
    private char[] str;
    private int position = 0;
    private PartScanner scanner;

    public WordBuilder(){}


    public PartScanner scanner(){
        return scanner == null?scanner = new PartScanner():scanner;
    }


    public Word build(String werb){
        return build(werb, str = werb.toCharArray(), 0, str.length);
    }


    public Word build(char[] str, int start, int end){
        return build(null, str, start, end);
    }


    public Word build(String src, char[] str, int start, int end){
        Word word;
        build(word = new Word(), src, str, start, end);
        return word;
    }


    protected void unsafe(Word word, char[] str, int start, int end){
        word.init(new String(str, start, start-end), WordPartIndex.UNSAFE);
    }


    protected void build(Word word, String src, char[] str, int start, int end){
        final int strLength;
        if((strLength = end - start)<4){
            String val;
            word.init(val = new String(str, start, strLength),
                    null, null, val, null, null, null
            );
            return;
        }
        position = 0;

        //start exceptions
        final WordLib[] exceptions = ExceptWordIndex.inst.index;
        if(strLength < exceptions.length){
            Word except = exceptions[strLength].get(eKey.init(str, start, end));
            if(except != null){
                word.init(except);
                return;
            }
        }
        //end exceptions

        this.str = str;
        final int[] cIndex, alloc;
        if(this.alloc.length < strLength){
            alloc  = this.alloc  = new int[strLength];
            cIndex = this.cIndex = new int[strLength];
        } else {
            cIndex = this.cIndex;
            alloc  = this.alloc;
        }

        for (int i = 0; i < strLength; i++) {
            cIndex[i] = WordIndex.indexOf(str[i+start]);
        }

        IdxString prefix1 = searchPrefix(strLength, alloc, cIndex);
        IdxString prefix2 = prefix1 == null?null:searchPrefix(strLength, alloc, cIndex);
        int startRoot = position;

        position = strLength-1;
        Ending ending = searchEnding(alloc, cIndex);

        Suffix suffix1 = searchSuffix(alloc, cIndex);
        Suffix suffix2 = suffix1 == null?null:searchSuffix(alloc, cIndex);

        if(suffix2 == null){
            suffix2 = suffix1;
            suffix1 = null;
        }

        String root = position>startRoot?new String(str, startRoot+start, position-startRoot+1):"";

        word.init(src, prefix1, prefix2, root, suffix1, suffix2, ending);
    }


    private IdxString searchPrefix(final int strLength, final int[] alloc, final  int[] cIndex){
        int count = 0, summ = 0, i;

        for (i = position; i < strLength && count < 7; i++){
            summ |= (cIndex[i] << count * 5);

            for (int sub = count; sub < 7; sub++) alloc[sub] = summ;

            ++count;
        }

        if(count>6)count = 6;

        IdxString result;
        Int2ObjectOpenHashMap<IdxString>[] sepIndex = WordPartIndex.prefixIndex.sepIndex;
        for (i = count - 1; i > -1; --i){
            if((result = sepIndex[i].get(alloc[i])) != null){
                position += result.value.length();
                return result;
            }
        }
        return null;
    }


    private Ending searchEnding(final int[] alloc, final int[] cIndex){
        int cursor = position, temp = cIndex[cursor--], i;

        for (i = 0; i < 3; i++) alloc[i] = temp;

        temp = cIndex[cursor--];
        for (i = 1; i < 3; i++) alloc[i] = (alloc[i] << 5) | temp;

        temp = cIndex[cursor];

        alloc[2] = alloc[2] << 10 | temp;

        Ending result;

        final Int2ObjectOpenHashMap<Ending>[] index = EndingIndex.inst.index;
        for (i = 2; i > -1; --i){
            if((result = index[i].get(alloc[i])) != null){
                position -= result.str.value.length();
                return result;
            }
        }
        return null;
    }


    private Suffix searchSuffix(final int[] alloc, final int[] cIndex){
        int cursor = position, oldCursor = cursor, temp = cIndex[cursor--], i;

        for (i = 0; i < 7; i++) alloc[i] = temp;

        for (i = 5; i > 0 && cursor > 0; i--) {
            temp = cIndex[cursor--];

            for (int j = 0, shift = 6 - i; j < i; j++)
                alloc[j] = (alloc[j] << 5 * shift) | temp;
        }

        oldCursor = oldCursor>5?5:oldCursor;
        Suffix result;

        final Int2ObjectOpenHashMap<Suffix>[] index = SuffixIndex.inst.index;
        for (i = oldCursor; i >= 0; i--){
            if((result = index[i].get(alloc[5 - i])) != null){
                position -= result.str.value.length();
                return result;
            }
        }
        return null;
    }


    public static class PartScanner{
        private final WordPartIndex.SPart[] parts = new WordPartIndex.SPart[16];
        private final int[] weights  = new int[16], csWeights = new int[16];
        private final Case[] cases = new Case[16];
        private int size = 0, csSize = 0;

        private void feed(WordPartIndex.SPart part, int weight){
            WordPartIndex.SPart[] parts = this.parts;
            for (int i = 0; i < size; i++){
                if(parts[i] == part){
                    weights[i] += weight;
                    return;
                }
            }
            parts[size] = part;
            weights[size] = weight;

            ++size;
        }


        private void feed(Suffix suffix){
            if(suffix != null) {
                WordPartIndex.SPart[] parts = suffix.parts;
                for (int i = 0; i < parts.length; i++)
                    feed(parts[i], suffix.weights[i]);
            }
        }


        public WordPartIndex.SPart calcPart(){
            if(size == 0)return null;
            int max = 0;
            WordPartIndex.SPart maxPart = null;
            for (int i = 0; i < size;i++){
                if(weights[i] > max ){
                    maxPart = parts[i];
                    max = weights[i];
                }
            }
            return maxPart;
        }


        public Case calcCase(WordPartIndex.SPart part){
            if(csSize == 0 || part == null)return null;
            int max = 0;
            Case maxCase = null;
            for (int i = 0; i < csSize;i++){
                if(csWeights[i] > max){
                    if(cases[i].part().part() == part) {
                        maxCase = cases[i];
                        max = csWeights[i];
                    }
                }
            }
            return maxCase;
        }


        private void feed(Case cs, int weight){
            final Case[] cases = this.cases;
            for (int i = 0; i < csSize; i++){
                if(cases[i] == cs){
                    csWeights[i] += weight;
                    return;
                }
            }
            cases[csSize] = cs;
            csWeights[csSize] = weight;
            ++csSize;

            feed(cs.part().part(), weight);
        }


        private void feed(Ending ending){
            if(ending != null) {
                final Case[] ecs = ending.cases;
                for (int i = 0; i < ecs.length; i++)
                    feed(ecs[i], ending.weights[i]);
            }
        }


        public void scan(Word werb){
            size = csSize = 0;
            feed(werb.suffix1());
            feed(werb.suffix2());
            feed(werb.ending());
        }
    }
}


