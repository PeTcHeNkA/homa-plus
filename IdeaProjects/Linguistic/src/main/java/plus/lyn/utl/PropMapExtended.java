package plus.lyn.utl;

import plus.lyn.unit.Proposal;
import java.util.concurrent.ConcurrentLinkedQueue;


public class PropMapExtended extends PropMap{
    private final ConcurrentLinkedQueue<Proposal.Builder> builders = new ConcurrentLinkedQueue<>();

    public Proposal.Builder builder(){
        Proposal.Builder builder;
        return (builder = builders.poll()) == null?new Proposal.Builder():builder;
    }


    public <T> Chanel<T> makeChan(){
        return new Chanel<>(this);
    }


    public static class Chanel<T> extends Result<T>{
        private final PropMapExtended parent;
        private Proposal.Builder builder;
        private Proposal proposal;


        public Chanel(PropMapExtended parent) {
            this.parent = parent;
        }


        public Proposal.Builder builder(){
            return builder == null?builder = parent.builder():builder;
        }


        public void free(){
            if(builder != null){
                parent.free(builder);
                builder = null;
            }
        }


        public Chanel<T> read(String key){
            proposal = null;
            parent.get(this, key, builder());
            return this;
        }


        public void put(String key, T val){
            parent.put(key, builder(), val);
        }


        public Proposal proposal(){
            return proposal == null?proposal = new Proposal(builder(), words):proposal;
        }
    }


    public void free(Proposal.Builder builder){
        builders.add(builder);
    }


    public void clean(){
        for (Proposal.Builder builder:builders) builder.clear();
    }


    public void get(Result<?> result, String key){
        Proposal.Builder builder;
        super.get(result, key, builder = builder());
        free(builder);
    }


    public void add(Object val, String... keys){
        Proposal.Builder builder = builder();
        for (String key:keys){
            super.put(key, builder, val);
        }
        free(builder);
    }
}
