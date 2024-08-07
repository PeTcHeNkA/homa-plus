package plus.lyn;

import java.util.Arrays;


public class Case {
    public static final Case EMPTY = new Case(null,null);

    public final IdxString[] endingsSingle;
    public final IdxString[] endingsMulti;
    private Type type;
    private WordPartIndex.Part part;

    Case(IdxString[] single, IdxString[] multi) {
        this.endingsSingle = single;
        this.endingsMulti = multi;
    }

    Case link(WordPartIndex.Part part, Type type) {
        this.part = part;
        this.type = type;
        return this;
    }

    public Type getType() {
        return type;
    }


    public WordPartIndex.Part part() {
        return part;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Case aCase = (Case) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(endingsSingle, aCase.endingsSingle)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(endingsMulti, aCase.endingsMulti)) return false;
        if (type != aCase.type) return false;
        return part.equals(aCase.part);
    }


    @Override
    public int hashCode() {
        int result = Arrays.hashCode(endingsSingle);
        result = 31 * result + Arrays.hashCode(endingsMulti);
        result = 31 * result + type.hashCode();
        result = 31 * result + part.hashCode();
        return result;
    }


    public enum Type {
        NOMINATIVE(0), GENITIVE(1), DATIVE(2), ACCUSATIVE(3), ABLATIVE(4), PREPOSITIONAL(5);
        public final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type ofString(String src){
            src = src.toLowerCase();
            switch (src){
                case "1":
                case "nom":
                case "nominative":
                case "им":
                case "именительный":
                    return NOMINATIVE;
                case "2":
                case "gen":
                case "genitive":
                case "род":
                case "родительный":
                    return GENITIVE;
                case "3":
                case "dat":
                case "dative":
                case "д":
                case "дат":
                case "дательный":
                    return DATIVE;
                case "4":
                case "acc":
                case "accusative":
                case "вин":
                case "винительный":
                    return ACCUSATIVE;
                case "5":
                case "abl":
                case "ablative":
                case "твор":
                case "творительный":
                    return ABLATIVE;
                case "6":
                case "prep":
                case "prepositional":
                case "пред":
                case "предл":
                case "предложный":
                    return PREPOSITIONAL;
            }
            throw new IllegalArgumentException();
        }
    }
}
