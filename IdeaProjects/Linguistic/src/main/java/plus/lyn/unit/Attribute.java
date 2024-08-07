package plus.lyn.unit;

import plus.lyn.Word;


public class Attribute extends ProposalPart{
    public final Word[] modifications;

    public Attribute(Word word, Word... mods) {
        super(word);
        this.modifications = mods;
    }


    @Override
    public Type type() {
        return Type.DEFINITION;
    }
}
