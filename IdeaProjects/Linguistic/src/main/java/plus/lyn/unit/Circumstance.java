package plus.lyn.unit;

import plus.lyn.Word;


public class Circumstance extends ProposalPart{
    public Circumstance(Word word) {
        super(word);
    }


    @Override
    public Type type() {
        return Type.CIRCUMSTANCE;
    }
}