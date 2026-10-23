package plus.lyn.unit;

import plus.lyn.Word;


public class Predicate extends ProposalPart{
    public final Word[] adverbs;
    protected Subject subject;

    public Predicate(Word word, Word... adverbs) {
        super(word);
        this.adverbs = adverbs;
    }


    public Subject getSubject() {
        return subject;
    }


    @Override
    public Type type() {
        return Type.PREDICATE;
    }
}