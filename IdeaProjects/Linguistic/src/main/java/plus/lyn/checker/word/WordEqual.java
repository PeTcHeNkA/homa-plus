package plus.lyn.checker.word;

import plus.lyn.Word;
import plus.lyn.WordBuilder;


public class WordEqual implements WordPredicate{
    private final Word word;
    public WordEqual(WordBuilder builder, String str) {
        this.word = builder.build(str);
    }

    @Override
    public boolean isSuccess(Word other) {
        return word.equals(other);
    }
}
