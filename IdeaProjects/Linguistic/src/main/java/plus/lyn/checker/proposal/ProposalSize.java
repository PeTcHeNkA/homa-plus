package plus.lyn.checker.proposal;

import plus.lyn.unit.Proposal;


public class ProposalSize implements ProposalPredicate{
    private final int size, func;

    public ProposalSize(int size, int func) {
        this.size = size;
        this.func = func;
    }


    @Override
    public boolean isSuccess(int cursor, Proposal proposal) {
        switch (func){
            default : return proposal.words.length == size;
            case  1 : return proposal.words.length >= size;
            case -1 : return proposal.words.length <= size;
        }
    }
}