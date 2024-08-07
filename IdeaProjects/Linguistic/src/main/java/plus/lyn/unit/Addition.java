package plus.lyn.unit;

import plus.lyn.Word;


public class Addition extends ProposalPart{
    public Addition(Word word) {
        super(word);
    }


    @Override
    public Type type() {
        return Type.ADDITION;
    }
}