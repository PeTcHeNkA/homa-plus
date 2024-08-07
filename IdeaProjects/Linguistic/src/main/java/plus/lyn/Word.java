package plus.lyn;

import java.nio.charset.StandardCharsets;


public final class Word {
    IdxString prefix1, prefix2;
    String root;
    Suffix suffix1, suffix2;
    Ending ending;
    String value;
    WordPartIndex.SPart part = WordPartIndex.EMPTY;
    Case cs = Case.EMPTY;

    public Word(){}


    public Word(String value, WordPartIndex.SPart part){
        this.value = value;
        this.root  = value;
        this.prefix1 = null;
        this.prefix2 = null;
        this.suffix1 = null;
        this.suffix2 = null;
        this.ending  = null;
        this.part    = part;
        this.cs      = null;
    }


    public Word(String value, IdxString prefix1, IdxString prefix2, String root, Suffix suffix1, Suffix suffix2, Ending ending) {
        this.value = value;
        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
        this.root = root;
        this.suffix1 = suffix1;
        this.suffix2 = suffix2;
        this.ending = ending;
    }


    void init(Word src){
        this.value = src.value;
        this.root  = src.root;
        this.prefix1 = src.prefix1;
        this.prefix2 = src.prefix2;
        this.suffix1 = src.suffix1;
        this.suffix2 = src.suffix2;
        this.ending  = src.ending;
        this.part    = src.part;
        this.cs      = src.cs;
    }


    void init(String value, WordPartIndex.SPart part){
        this.value = value;
        this.root  = value;
        this.prefix1 = null;
        this.prefix2 = null;
        this.suffix1 = null;
        this.suffix2 = null;
        this.ending  = null;
        this.part    = part;
        this.cs      = null;
    }


    void init(String value, IdxString prefix1, IdxString prefix2, String root, Suffix suffix1, Suffix suffix2, Ending ending) {
        this.value = value;
        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
        this.root = root;
        this.suffix1 = suffix1;
        this.suffix2 = suffix2;
        this.ending = ending;
    }


    public IdxString prefix1(){
        return prefix1;
    }


    public IdxString prefix2(){
        return prefix2;
    }


    public String root(){
        return root;
    }


    public Suffix suffix1(){
        return suffix1;
    }


    public Suffix suffix2(){
        return suffix2;
    }


    public Ending ending(){
        return ending;
    }


    public WordPartIndex.SPart getPart(){
        return getPart(null);
    }


    public Case getCase(){
        return getCase(null);
    }


    public WordPartIndex.SPart getPart(WordBuilder.PartScanner scanner){
        if(part != WordPartIndex.EMPTY) return part;
        fillData(scanner);
        return part;
    }


    public Case getCase(WordBuilder.PartScanner scanner){
        if(cs != Case.EMPTY) return cs;
        fillData(scanner);
        return cs;
    }


    public String indexPart(){
        return root == null?value:root;
    }


    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == Word.class && ((Word)obj).value.equals(value);
    }


    @Override
    public int hashCode() {
        return value.hashCode();
    }


    private void fillData(WordBuilder.PartScanner scanner){
        if(scanner==null)scanner = new WordBuilder.PartScanner();
        scanner.scan(this);
        if(part == WordPartIndex.EMPTY) part = scanner.calcPart();
        if(cs   == Case.EMPTY)          cs   = scanner.calcCase(part);
    }


    @Override
    public String toString() {
        return value;
    }


    public int length(){
        return value.length();
    }


    public String toUTF8Str(){
        return new String(value.getBytes(StandardCharsets.UTF_8));
    }
}