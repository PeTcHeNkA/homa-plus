package plus.lyn.checker.word;

import plus.lyn.Word;


public class WordAnd implements WordPredicate{
    private final WordPredicate first, second;

    public WordAnd(WordPredicate first, WordPredicate second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean isSuccess(Word word) {
        if(!first.isSuccess(word))return false;
        return second.isSuccess(word);
    }
}
