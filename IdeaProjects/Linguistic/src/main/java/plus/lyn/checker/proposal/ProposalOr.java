package plus.lyn.checker.proposal;

import plus.lyn.unit.Proposal;

public class ProposalOr implements ProposalPredicate{
    private final ProposalPredicate first, second;

    public ProposalOr(ProposalPredicate first, ProposalPredicate second) {
        this.first = first;
        this.second = second;
    }


    @Override
    public boolean isSuccess(int cursor, Proposal proposal) {
        if(first.isSuccess(cursor, proposal))return true;
        return second.isSuccess(cursor, proposal);
    }
}
