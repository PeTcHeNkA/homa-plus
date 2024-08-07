package plus.lyn.checker.word;

import plus.lyn.Suffix;
import plus.lyn.Word;
import plus.lyn.WordBuilder;


public class WordAnySuffixes implements WordPredicate{
    protected final Suffix[][] strings;

    public WordAnySuffixes(WordBuilder builder, String... src) {
        strings = new Suffix[src.length][2];
        init(builder, src);
    }


    protected final boolean eqIdxStrings(Suffix first, Suffix second){
        if(first == second)return true;
        return first != null && first.str.index == second.str.index;
    }


    protected void init(WordBuilder builder, String... src){
        for (int i=0;i<src.length;i++){
            Word word = builder.build(src[i]);
            strings[i] = new Suffix[]{
                    word.suffix1(), word.suffix2()
            };
        }
    }


    @Override
    public boolean isSuccess(Word word) {
        for (Suffix[] suffixes: strings)
            if(eqIdxStrings(suffixes[0], word.suffix1()) && eqIdxStrings(suffixes[1], word.suffix2()))return true;
        return false;
    }
}
