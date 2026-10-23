package plus.lyn.checker.proposal;

import plus.lyn.unit.Proposal;


public class ProposalNot implements ProposalPredicate{
    private final ProposalPredicate parent;

    public ProposalNot(ProposalPredicate parent) {
        this.parent = parent;
    }


    @Override
    public boolean isSuccess(int cursor, Proposal proposal) {
        return !parent.isSuccess(cursor, proposal);
    }
}
