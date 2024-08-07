package plus.lyn.checker.word;

import plus.lyn.Word;


public class WordOr implements WordPredicate{
    private final WordPredicate first, second;

    public WordOr(WordPredicate first, WordPredicate second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean isSuccess(Word word) {
        if(first.isSuccess(word))return true;
        return second.isSuccess(word);
    }
}
