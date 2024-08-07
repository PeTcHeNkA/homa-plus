package plus.lyn.checker.proposal;

import it.unimi.dsi.fastutil.booleans.BooleanByteImmutablePair;
import plus.lyn.checker.word.WordPredicate;
import plus.lyn.unit.Proposal;


public class ProposalCheckWord implements ProposalPredicate{
    private final WordPredicate predicate;
    private final boolean relative;
    private final int target;


    public ProposalCheckWord(WordPredicate predicate, int target, boolean relative) {
        this.target = target;
        this.relative = relative;
        this.predicate = predicate;
    }


    public static BooleanByteImmutablePair argsOf(String key, String subKey){
        switch (key){
            case "start":
                return new BooleanByteImmutablePair(false, (byte) 0);
            case "end":
                return new BooleanByteImmutablePair(false, (byte)-1);
            case "b":
            case "before":
                return new BooleanByteImmutablePair(true, (byte)-Integer.parseInt(subKey));
            case "c":
            case "cursor":
                if(subKey == null)
                    return new BooleanByteImmutablePair(true, (byte) 0);
            case "after":
                return new BooleanByteImmutablePair(true, (byte) Integer.parseInt(subKey));
            case "pos":
            case "position":
                return new BooleanByteImmutablePair(false, (byte) Integer.parseInt(subKey));
            default:
                return new BooleanByteImmutablePair(false, (byte) Integer.parseInt(key));
        }
    }


    @Override
    public boolean isSuccess(int cursor, Proposal proposal) {
        if(relative){
            cursor += target;
        } else {
            if(target > 0)cursor = target;
            else cursor = proposal.words.length + target;
        }
        if(cursor >= proposal.words.length || cursor < 0)return false;
        return predicate.isSuccess(proposal.words[cursor]);
    }
}







