package plus.lyn.checker.word;

import plus.lyn.Word;


public class WordNot implements WordPredicate{
    private final WordPredicate parent;

    public WordNot(WordPredicate parent) {
        this.parent = parent;
    }


    @Override
    public boolean isSuccess(Word word) {
        return !parent.isSuccess(word);
    }
}
