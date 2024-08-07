package plus.lyn.checker.word;

import plus.lyn.IdxString;
import plus.lyn.Word;
import plus.lyn.WordBuilder;


public class WordAnyPrefixes implements WordPredicate{
    protected final IdxString[][] strings;

    public WordAnyPrefixes(WordBuilder builder, String... src) {
        strings = new IdxString[src.length][2];
        init(builder, src);
    }


    protected final boolean eqIdxStrings(IdxString first, IdxString second){
        if(first == second)return true;
        return first != null && first.index == second.index;
    }


    protected void init(WordBuilder builder, String... src){
        for (int i=0;i<src.length;i++){
            Word word = builder.build(src[i]);
            strings[i] = new IdxString[]{
                    word.prefix1(), word.prefix2()
            };
        }
    }


    @Override
    public boolean isSuccess(Word word) {
        for (IdxString[] prefix: strings)
            if(eqIdxStrings(prefix[0], word.prefix1()) && eqIdxStrings(prefix[1], word.prefix2()))return true;
        return false;
    }
}
