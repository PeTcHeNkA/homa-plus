package plus.lyn.checker.word;

import plus.lyn.Ending;
import plus.lyn.Word;
import plus.lyn.WordBuilder;


public class WordAnyEnding implements WordPredicate{
    protected final Ending[] strings;

    public WordAnyEnding(WordBuilder builder, String... src) {
        strings = new Ending[src.length];
        init(builder, src);
    }


    protected final boolean eqIdxStrings(Ending first, Ending second){
        if(first == second)return true;
        return first != null && first.str.index == second.str.index;
    }


    protected void init(WordBuilder builder, String... src){
        for (int i = 0;i < src.length; i++){
            Word word = builder.build(src[i]);
            strings[i] = word.ending();
        }
    }


    @Override
    public boolean isSuccess(Word word) {
        for (Ending ending: strings)
            if(eqIdxStrings(ending, word.ending()))return true;
        return false;
    }
}
