package plus.lyn.checker.word;

import plus.lyn.Word;


public class WordMulti implements WordPredicate{
    private final boolean multi;

    public WordMulti(boolean multi) {
        this.multi = multi;
    }

    @Override
    public boolean isSuccess(Word word) {
        if(multi) {
            if (word.ending() == null) return false;
            return word.ending().maybeMulti;
        } else {
            if (word.ending() == null) return true;
            return word.ending().maybeSingle;
        }
    }
}
