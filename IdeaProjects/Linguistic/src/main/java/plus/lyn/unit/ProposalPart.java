package plus.lyn.unit;

import plus.lyn.Word;


public abstract class ProposalPart {
    protected Word word;


//    public ProposalPart build(WerbBuilder.PartScanner scanner, Werb... werbs){
//        WerbPartIndex.SPart part = werb.getPart(scanner);
//        if(part == WerbPartIndex.NOUN){
//
//        }
//
//    }
//
//    private ProposalPart buildNoun(){
//
//    }


    public Word getWord() {
        return word;
    }


    public ProposalPart(Word word) {
        this.word = word;
    }

    public abstract Type type();


    public boolean isMain(){
        return type().main;
    }


    public enum Type{
        SUBJECT(true), PREDICATE(true), ADDITION, CIRCUMSTANCE, DEFINITION, SYSTEM;

        public final boolean main;

        Type() {
            this.main = false;
        }
        Type(boolean main) {
            this.main = main;
        }
    }
}