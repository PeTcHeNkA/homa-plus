package plus.lyn.checker.word;

import plus.lyn.Case;
import plus.lyn.Word;


public class WordCase implements WordPredicate{
    private final int id;

    public WordCase(int id) {
        this.id = id;
    }


    @Override
    public boolean isSuccess(Word word) {
        Case cs = word.getCase();
        if(cs == null)return id == -1;
        return cs.getType().id == id;
    }
}
