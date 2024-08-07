package plus.lyn.unit;

import plus.lyn.Word;


public class Subject extends ProposalPart{
    public final Attribute[] attributes;
    protected Predicate predicate;

    public Subject(Word werb, Attribute... attributes) {
        super(werb);
        this.attributes = attributes;
    }


    public Predicate getPredicate(){
        return predicate;
    }


    @Override
    public Type type() {
        return Type.SUBJECT;
    }
}
