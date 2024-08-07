package plus.lyn;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.tson.utl.Te4HashMap;


public class WordPartIndex {
    private static final IdxString[] EMPTY_IDX_STR = new IdxString[0];
    public static final String[] allPrefixes = new String[]{
            "агит", "ан", "англо", "анти", "атто", "без", "бес", "брам", "в", "вз", "вне", "военно", "воз", "вос", "вы", "гекса",
            "гексаконта", "гекта", "гекто", "гепта", "гептаконта", "гига", "гипер", "гор", "гос", "де", "дез", "дека", "деци",
            "дикта", "до", "додека", "за", "зепто", "зетта", "из", "изо", "ин", "интервики", "интра", "инфра", "ис", "йокто",
            "йотта", "квадра", "квази", "кила", "кило", "ко", "кое", "контр", "лейб", "мега", "меж", "микро", "милли", "мини",
            "мириа", "моно", "на", "над", "наи", "нано", "не", "недо", "ни", "низ", "нис", "нона", "о", "около", "окта", "октаконта",
            "от", "па", "пара", "пентаконта", "пере", "пета", "пико", "по", "под", "подь", "после", "пост", "пра", "пре", "пред",
            "при", "про", "прото", "раз", "рас", "ре", "роз", "рос", "с", "санти", "сверх", "со", "су", "суб", "супер", "супра",
            "сюр", "тера", "тетра", "тетраконта", "транс", "тре", "три", "триаконта", "тридека", "трикта", "у", "ультра", "ундека",
            "фемто", "черес", "эйкоза", "экзо", "экс", "экса", "экстра", "эннеаконта",
            "между"
    };


    public static final int[] emptyWeights = new int[]{1};


    private static IdxString[] of(String... str){
        if(str.length == 0)return EMPTY_IDX_STR;
        IdxString[] idx = new IdxString[str.length];
        for (int i=0;i<idx.length;i++){
            idx[i] = IdxString.of(str[i]);
        }
        return idx;
    }


    private static Case Case(IdxString[] s, IdxString[] m){
        return new Case(s,m);
    }


    public static final WordPartIndex prefixIndex = new WordPartIndex(allPrefixes, 6);
    private static final Te4HashMap<String, SPart> partsByNames = new Te4HashMap<>();

    public static SPart byName(String name){
        return partsByNames.get(name.toLowerCase());
    }


    //Существительное
    public static final SPart NOUN = new SPart("Noun",
            new Part("Delc-1",
                    Case(of("а", "я"),   of("ы", "и")     ),
                    Case(of("ы", "и"),   of()                 ),
                    Case(of("е"),        of("ам", "ям")   ),
                    Case(of("у", "ю"),   of("ы", "и")     ),
                    Case(of("ой", "ей"), of("ами", "ями") ),
                    Case(of("е"),        of("ах", "ях")   )
            ),
            new Part("Delc-2",
                    Case(of(),               of("а", "и")     ),
                    Case(of("а", "я"),   of("ов", "ей")   ),
                    Case(of("у", "ю"),   of("ам", "ям")  ),
                    Case(of("я"),        of("а", "ей")    ),
                    Case(of("ом", "ем"), of("ами", "ями") ),
                    Case(of("е"),        of("ах", "ях")   )
            ),
            new Part("Delc-3",
                    Case(of(),               of("и")         ),
                    Case(of("и"),        of("ей")         ),
                    Case(of("и"),        of("ям")         ),
                    Case(of(),              of("и", "ей")    ),
                    Case(of("ю"),        of("ями")        ),
                    Case(of("и"),        of("ях")         )
            )
    );
    //Прилагательное
    public static final SPart ADJECTIVE = new SPart("Adjective",
            new Part("Mele",
                    Case(of("ый", "ий"  ),  of("ые", "ие")   ),
                    Case(of("ого", "его"),  of("ых", "их")   ),
                    Case(of("ому", "ему"),  of("ам", "ям")  ),
                    Case(of(),                  of()                ),
                    Case(of("ым", "им"  ),  of("ыми", "ими") ),
                    Case(of("ом", "ем"  ),  of("ых", "их")   )
            ),
            new Part("Famele",
                    Case(of("ая", "яя"  ),  of("ые", "ие")   ),
                    Case(of("ой", "ей"  ),  of("ых", "их")   ),
                    Case(of("ой", "ей"  ),  of("ам", "ям")   ),
                    Case(of("ую", "юю"  ),  of()                ),
                    Case(of("ой", "ей"  ),  of("ыми", "ими") ),
                    Case(of("ой", "ей"  ),  of("ых", "их")   )
            ),
            new Part("Midle",
                    Case(of("ое", "ее"  ),  of("ые", "ие")   ),
                    Case(of("ого", "его"),  of("ых", "их")   ),
                    Case(of("ому", "ему"),  of("ам", "ям")   ),
                    Case(of("ое", "ее"  ),  of()                ),
                    Case(of("ым", "им"  ),  of("ыми", "ими") ),
                    Case(of("ом", "ем"  ),  of("ых", "их")   )
            )
    );

    public static final SPart PRONOUN = new SPart("Pronoun",
            new Part("Self-1",
                    Case(of("я"   ),         of("мы"  )  ),
                    Case(of("меня"),         of("нас" )  ),
                    Case(of("мне" ),         of("нам" )  ),
                    Case(of("меня"),         of("нас" )  ),
                    Case(of("мной", "мною"), of("нами")  ),
                    Case(of("мне" ),         of("нас" )  )
            ),
            new Part("Self-2",
                    Case(of("ты"),             of("вы"  )  ),
                    Case(of("тебя"),           of("вас" )  ),
                    Case(of("тебе"),           of("вам" )  ),
                    Case(of("тебя"),           of("вас" )  ),
                    Case(of("тобой", "тобою"), of("вами")  ),
                    Case(of("тебе"),           of("вас" )  )
            ),
            new Part("Self-3",
                    Case(of("он", "она", "оно"),                  of("они"       )),
                    Case(of("его","ее","его","него","нее","него"),of("их","них"  )),
                    Case(of("ему","ей","ему","нему","ней","нему"),of("им","ним"  )),
                    Case(of("его","ее","его","него","нее","него"),of("их","них"  )),
                    Case(of("им", "ней", "нею", "ним"),           of("ими","ними")),
                    Case(of("нем", "ней", "нем"),                 of("них"       ))
            ),
            new Part("Relative",
                    Case(of("кто","что","какой","каков","чей","который"),of("которые","каковы","чьи")),
                    Case(of("кого", "чего"),                             of()                            ),
                    Case(of("кому", "чему"),                             of()                            ),
                    Case(of("кого", "что" ),                             of()                            ),
                    Case(of("кем", "чем"  ),                             of()                            ),
                    Case(of("ком", "чем"  ),                             of()                            )
            ),
            new Part("Index",
                    Case(of("столько","этот","эта","это", "тот","та", "то","такой",
                            "такая","такое", "таков", "такова", "таково", "сей", "сия", "сие"),
                            of("эти","те", "такие", "таковы", "сии")),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  )
            ),
            new Part("Attributive",
                    Case(of("всякий","любой","сам","самый","другой","иной"),
                            of("все","весь","всяк","каждый")),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  )
            ),
            new Part("Negative",
                    Case(of("никто","ничто","некого","ничего","нисколько","никакой", "ничей"), of()),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  )
            ),
            new Part("Undefined",
                    Case(of("некто","нечто","некий","некоторый","несколько"), of()),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  ),
                    Case(of(),of()  )
            )

    );

    //глагол
    public static final SPart VERB           = new SPart("Verb");
    //причастие
    public static final SPart PARTICIPLE     = copyEndingsOf("Participle", ADJECTIVE);
    //деепричастие
    public static final SPart DEE_PARTICIPLE = copyEndingsOf("Participle", ADJECTIVE);
    //наречие
    public static final SPart ADVERB         = new SPart("Adverb");
    //системная заглушка
    public static final SPart EMPTY          = new SPart(null, false);
    //предлог
    public static final SPart PREPOSITION    = new SPart("Preposition", false);
    //союз
    public static final SPart UNION          = new SPart("Union", false);
    //частица
    public static final SPart PARTICLE       = new SPart("Particle", false);
    //unsafe
    public static final SPart UNSAFE         = new SPart("Unsafe");


    protected final Int2ObjectOpenHashMap<IdxString>[] sepIndex;
    protected final Int2ObjectOpenHashMap<IdxString> index;

    protected WordPartIndex(){
        sepIndex = onInit();
        index = sepIndex[sepIndex.length-1];
    }


    protected Int2ObjectOpenHashMap<IdxString>[] onInit(){
        throw new UnsupportedOperationException();
    }


    protected WordPartIndex(String[] items, int lvls){
        sepIndex = new Int2ObjectOpenHashMap[lvls];
        fillIndex(items);
        index = sepIndex[lvls-1];
    }


    public IdxString getLinkOrNull(String suffix){
        return index.get(WordIndex.inst.werbToIndex6(suffix.toCharArray(), 0));
    }


    private void fillIndex(String[] items){
        int ignored = 0;
        for (int i=0;i<sepIndex.length;i++){
            sepIndex[i] = new Int2ObjectOpenHashMap<>();
        }

        for (String suffix:items){
            if(suffix.length()>sepIndex.length){
                ++ ignored;
                continue;
            }
            int idx = WordIndex.inst.werbToIndex6(suffix.toCharArray(),0);
            for (int i = 0;i<sepIndex.length;i++){
                if(i+1==suffix.length()){
                    sepIndex[i].put(idx, IdxString.of(suffix, idx));
                }
            }
        }
        for (int i=0;i<sepIndex.length;i++){
            sepIndex[i].trim();
        }
    }


    public static class SPart{
        public final String name;
        public final Part[] parts;
        public final boolean standalone;
        Suffix[] suffixes;

        SPart(String name, Part... parts) {
            this(name, true, parts);
            if(name == null)name = "null";
            partsByNames.fput(name.toLowerCase(), this);
        }

        SPart(String name, boolean standalone, Part... parts) {
            this.standalone = standalone;
            this.name = name;
            this.parts = parts;
            for (Part part:parts){
                part.link(this);
            }
        }

        public Suffix[] suffixes(){
            return suffixes;
        }


        @Override
        public String toString() {
            return name;
        }
    }


    private static SPart copyEndingsOf(String name, SPart part){
        Part[] parts = new Part[part.parts.length];
        for (int i=0;i< parts.length;i++){
            Part src = part.parts[i];
            parts[i] = new Part(src.name,
                    copyCase(src.nominative),
                    copyCase(src.genitive),
                    copyCase(src.dative),
                    copyCase(src.accusative),
                    copyCase(src.ablative),
                    copyCase(src.prepositional)
            );
        }
        return new SPart(name, parts);
    }


    private static Case copyCase(Case cs){
        return new Case(cs.endingsSingle, cs.endingsMulti);
    }


    protected static class Part { //Noun
        public final String name;
        public final Case nominative, genitive, dative, accusative, ablative, prepositional;
        public final Case[] allCases;
        private SPart sPart;

        Part(String name, Case nominative, Case genitive, Case dative, Case accusative, Case ablative, Case prepositional) {
            this.name = name;
            this.nominative    = nominative    .link(this, Case.Type.NOMINATIVE    );
            this.genitive      = genitive      .link(this, Case.Type.GENITIVE      );
            this.dative        = dative        .link(this, Case.Type.DATIVE        );
            this.accusative    = accusative    .link(this, Case.Type.ACCUSATIVE    );
            this.ablative      = ablative      .link(this, Case.Type.ABLATIVE      );
            this.prepositional = prepositional .link(this, Case.Type.PREPOSITIONAL );
            allCases = new Case[]{
                    nominative, genitive, dative, accusative, ablative, prepositional
            };
        }
        private Part link(SPart sPart){
            this.sPart = sPart;
            return this;
        }
        public SPart part() {
            return sPart;
        }
    }
}