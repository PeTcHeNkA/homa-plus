package plus.lyn.utl;

import it.unimi.dsi.fastutil.booleans.BooleanByteImmutablePair;
import plus.lyn.Word;
import plus.lyn.checker.proposal.ProposalCheckWord;
import plus.lyn.unit.Proposal;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;


public class ProposalVarProvider implements IVarProvider<PropMapExtended.Chanel<?>> {
    @Override
    public IVarGetter<PropMapExtended.Chanel<?>> getFmGetter(String name) {
        if(name.startsWith("word-")){
            String[] subKeys = name.split("-");
            BooleanByteImmutablePair pair = ProposalCheckWord.argsOf(subKeys[1], subKeys.length<=3?null:subKeys[2]);

            String type = subKeys.length>=4?subKeys[3]:"value";
            switch (type){
                case "value":
                    return new PropGetter(pair) {
                        @Override
                        public String get(PropMapExtended.Chanel<?> chan) {
                            Word word = selectWord(chan);
                            if(word == null)return null;
                            return word.toString();
                        }
                    };
                case "prefixes":
                    return new PropGetter(pair) {
                        @Override
                        public String get(PropMapExtended.Chanel<?> chan) {
                            Word word = selectWord(chan);
                            if(word == null)return null;
                            if(word.prefix1() == null)return null;
                            if(word.prefix2() == null)return word.prefix1().value;
                            return word.prefix1().value + word.prefix2();
                        }
                    };
                case "root":
                    return new PropGetter(pair) {
                        @Override
                        public String get(PropMapExtended.Chanel<?> chan) {
                            Word word = selectWord(chan);
                            if(word == null)return null;
                            return word.indexPart();
                        }
                    };
                case "suffixes":
                    return new PropGetter(pair) {
                        @Override
                        public String get(PropMapExtended.Chanel<?> chan) {
                            Word word = selectWord(chan);
                            if(word == null)return null;
                            if(word.suffix1() == null)return null;
                            if(word.suffix2() == null)return word.suffix1().toString();
                            return word.suffix1().toString() + word.prefix2();
                        }
                    };
                default:
                    throw new IllegalArgumentException("Name = "+name);
            }

        } else {
            switch (name){
                case "length": return chanel -> String.valueOf(chanel.proposal().words.length);
            }
        }
        return null;
    }


    private static abstract class PropGetter implements IVarGetter<PropMapExtended.Chanel<?>>{
        private final boolean relative;
        private final int target;

        private PropGetter(BooleanByteImmutablePair pair){
            this(pair.leftBoolean(), pair.rightByte());
        }

        private PropGetter(boolean relative, int target) {
            this.relative = relative;
            this.target = target;
        }


        public Word selectWord(PropMapExtended.Chanel<?> chan) {
            return selectWord(chan.getStart(), chan.proposal());
        }


        public Word selectWord(int cursor, Proposal proposal) {
            if(relative){
                cursor += target;
            } else {
                if(target > 0)cursor = target;
                else cursor = proposal.words.length + target;
            }
            if(cursor >= proposal.words.length || cursor < 0)return null;
            return proposal.words[cursor];
        }
    }
}
