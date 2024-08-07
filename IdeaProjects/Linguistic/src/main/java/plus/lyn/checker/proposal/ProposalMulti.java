package plus.lyn.checker.proposal;

import plus.lyn.Ending;
import plus.lyn.unit.Predicate;
import plus.lyn.unit.Proposal;


public class ProposalMulti implements ProposalPredicate{
    @Override
    public boolean isSuccess(int cursor, Proposal proposal) {
        int rating = 0;
        for (Predicate word: proposal.predicates){
            Ending ending1 = word.getWord().ending();
            if(ending1 != null){
                if(ending1.maybeMulti)++rating;
                if(ending1.maybeSingle)--rating;
            }
        }
        return rating > 0;
    }
}