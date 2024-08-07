package plus.lyn.checker.proposal;

import plus.lyn.Ending;
import plus.lyn.WordIndex;
import plus.lyn.unit.Predicate;
import plus.lyn.unit.Proposal;


public class ProposalGen implements ProposalPredicate{
    private static final int ending = WordIndex.inst.werbToIndex6("а");

    @Override
    public boolean isSuccess(int cursor, Proposal proposal) {
        int rating = 0;
        for (Predicate word: proposal.predicates){
            Ending ending1 = word.getWord().ending();
            if(ending1 == null)rating -= 1;
            else if(ending1.str.index == ending)rating += 1;
        }
        return rating > 0;
    }
}
