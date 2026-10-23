package plus.lyn.unit;

import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import plus.lyn.Word;
import plus.lyn.WordBuilder;
import plus.lyn.WordPartIndex;
import plus.lyn.utl.FList;


public class Proposal {
    public  static final Word[]      EMPTY_WERBS      = new Word[]{};
    public  static final Attribute[] EMPTY_ATTRS      = new Attribute[]{};
    public  static final Subject[]   EMPTY_SUBJECTS   = new Subject[]{};
    public  static final Predicate[] EMPTY_PREDICATES = new Predicate[]{};

    public final Predicate[] predicates;
    public final Subject[]   subjects;
    public final Word[]      words;


    //todo fix part detecting
    public static final class Builder extends WordBuilder {
        private final FList<Word>      werbBuf1 = new FList<>();
        private final FList<Attribute> attrBuf1 = new FList<>();
        private final FList<Subject>      subj  = new FList<>();
        private final FList<Predicate>    preds = new FList<>();
        private final FList<Word>      allWords = new FList<>();
        private int cursor;

        public Proposal buildProposial(String str){
            return new Proposal(this, buildWerbs(str));
        }


        public Word[] buildWerbs(String str){
            final Char2CharOpenHashMap fixer = Proposal.fixer;
            cursor = 0;
            allWords.clearOfSize(16);

            char[] chars = str.toCharArray();
            int start = -1, wordIndex = 0;

            for (int i = 0, cl = chars.length; i < cl; i++){
                char cur = chars[i] = fixer.get(chars[i]);
                if(cur == '@'){
                    chars[i] = ' ';
                    for (start = ++i; i < cl; i++){
                        cur = chars[i];
                        if(cur == ' ' || cur == '.' || cur == '?' || cur == '!' || cur == ','){
                            Word word = allWords.get(wordIndex);
                            if(word == null)word = new Word();
                            unsafe(word, chars, start, start-i);
                            allWords.safeAdd(word);
                            ++wordIndex;
                            start = -i;
                            break;
                        }
                    }
                }

                if(cur == ' ' || cur == '.' || cur == '?' || cur == '!' || cur == ','){
                    if(start != -1){
                        Word word = allWords.get(wordIndex);
                        if(word == null)word = new Word();
                        build(word, null, chars, start, i);
                        allWords.safeAdd(word);
                        ++wordIndex;
                        start = -1;
                    }
                } else {
                    if(start == -1) start = i;
                }
            }
            if(start != -1){
                Word word = allWords.get(wordIndex);
                if(word == null)word = new Word();
                build(word, null, chars, start, chars.length);
                allWords.safeAdd(word);
            }
            return allWords.toArray(EMPTY_WERBS);
        }


        public void clear(){
            werbBuf1.clear();
            allWords.clear();
            subj.clear();
            preds.clear();
            allWords.clear();
        }
    }


    private static final Char2CharOpenHashMap fixer = new Char2CharOpenHashMap();
    private static void addAll(char[]... lib){
        final Char2CharOpenHashMap lFixer = fixer;
        for (char[] pair:lib) {
            lFixer.addTo(pair[0], pair[0]);
            for (int i = 1; i < pair.length; i++) {
                lFixer.addTo(pair[i], pair[0]);
            }
        }
    }
    private static void defaults(char... lib){
        for (char c:lib) fixer.addTo(c, c);
    }

    static {
        addAll(new char[][]{
                {' ', ':', '>', '~', ']', '[', '}', '{', '(', ')', '\'', '"', '*'},

                {'а','А','a','A'},{'б','Б','6'},{'в','В','B'},{'г','Г'},{'д','Д'},{'е', 'ё','Ё','Е'},{'ж','Ж'},{'з','З', '3'},
                {'и','И'},{'й','Й'},{'к','К','k','K'},{'л','Л'},{'м','М','m','M'},{'н','Н','H'},
                {'о','О','o','O','0'},{'п','П'},{'р','Р','p','P'},{'с','С','c','C'},{'т','Т'},{'у','У','y','Y'},

                {'ф','Ф'},{'х','Х','x','X'},{'ц','Ц'},{'ч','Ч'},{'ш','Ш'},{'щ','Щ'},{'ы','Ы'},
                {'э','Э'},{'ь', 'ъ','Ъ','Ь','b'},{'ю','Ю'},{'я','Я'}
        });
        defaults('.', '?', '!', ',', '@');

        fixer.trim();
        fixer.defaultReturnValue(' ');
    }


    public Proposal(Builder builder, Word... words) {
        this.words = words;

        builder.subj.clearOfSize(words.length);
        builder.preds.clearOfSize(words.length);
        for (; builder.cursor < words.length; builder.cursor++){
            Subject subject = buildSubject(builder);
            if(subject != null){
                builder.subj.add(subject);
                if(subject.predicate != null){
                    builder.preds.add(subject.predicate);
                }
            } else {
                Predicate pred = buildPredicate(builder);
                if(pred != null){
                    builder.preds.add(pred);
                }
            }
        }
        subjects   = builder.subj.toArray(EMPTY_SUBJECTS);
        predicates = builder.preds.toArray(EMPTY_PREDICATES);
    }


    private Predicate buildPredicate(Builder builder){
        builder.attrBuf1.clearOfSize(words.length- builder.cursor);
        builder.werbBuf1.clearOfSize(words.length- builder.cursor);

        while (builder.cursor < words.length) {
            Word cur = words[builder.cursor];
            WordPartIndex.SPart part = cur.getPart(builder.scanner());
            if(part != null) {
                if (part == WordPartIndex.ADVERB) {
                    builder.werbBuf1.add(cur);
                } else if (part == WordPartIndex.VERB) {
                    return new Predicate(cur, builder.werbBuf1.toArray(EMPTY_WERBS));
                } else if (part.standalone) {
                    return null;
                }
            }
            ++builder.cursor;
        }
        return null;
    }


    private Subject buildSubject(Builder builder){
        int start = builder.cursor;
        builder.attrBuf1.clearOfSize(start);
        Word cur = words[builder.cursor];
        if(cur.getPart(builder.scanner()) == WordPartIndex.NOUN){
            Attribute attr = null;
            while (builder.cursor>0){
                builder.cursor-=1;
                attr = buildAttribute(builder);
                if(attr == null)break;
                builder.attrBuf1.add(attr);
            }
            builder.cursor = start+1;
            Subject subj = new Subject(cur, builder.attrBuf1.toArray(EMPTY_ATTRS));
            Predicate pred = buildPredicate(builder);
            if(pred != null){
                subj.predicate = pred;
                pred.subject   = subj;
            }
            return subj;
        }
        return null;
    }


    private Attribute buildAttribute(Builder builder){
        Word cur = words[builder.cursor];
        if(cur.getPart(builder.scanner()) == WordPartIndex.ADJECTIVE){
            return new Attribute(words[builder.cursor], getAJMods(builder));
        }
        return null;
    }


    private Word[] getAJMods(Builder builder){
        final FList<Word> buf;
        (buf = builder.werbBuf1).clearOfSize(builder.cursor);

        while (builder.cursor>0) {
            Word cur = words[builder.cursor--];
            if (!cur.getPart(builder.scanner()).standalone) {
                buf.add(cur);
            } else break;
        }
        return buf.toArray(EMPTY_WERBS);
    }


//    private void searchAdjectives(int before, FList<Werb> list, FList<Attribute> attrs){
//        list.clearOfSize(before);
//        attrs.clearOfSize(before);
//        int latest = -1;
//        for (int i=before;i>0;i--){
//            if(werbs[i].getPart(builder.scanner()) == WerbPartIndex.ADJECTIVE){
//                latest = i;
//            }
//        }
//    }
}