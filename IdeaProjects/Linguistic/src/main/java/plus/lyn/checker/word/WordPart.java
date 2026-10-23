package plus.lyn.checker.word;

import plus.lyn.Word;
import plus.lyn.WordPartIndex;
import plus.tson.utl.Te4HashMap;


public class WordPart implements WordPredicate{
    private static final Te4HashMap<WordPartIndex.SPart, WordPart> cache = new Te4HashMap<>();
    public static final WordPart NULL_PART = new WordPart((WordPartIndex.SPart) null);
    private final WordPartIndex.SPart part;

    protected WordPart(String name) {
        this.part = WordPartIndex.byName(name);
    }


    protected WordPart(WordPartIndex.SPart part){
        this.part = part;
    }


    public static WordPart getByName(String name){
        return getByPart(WordPartIndex.byName(name));
    }


    public static WordPart getByPart(WordPartIndex.SPart part){
        if(part == null)return NULL_PART;
        WordPart result = cache.get(part);
        if(result != null)return result;
        cache.fput(part, result = new WordPart(part));
        return result;
    }


    @Override
    public boolean isSuccess(Word word) {
        return word.getPart() == part;
    }
}
