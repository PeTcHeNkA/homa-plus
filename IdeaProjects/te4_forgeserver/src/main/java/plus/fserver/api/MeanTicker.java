package plus.fserver.api;


public interface MeanTicker {
    default double meanTickTime(){return 0d;}

    static final MeanTicker EMPTY = new MeanTicker() {
        @Override
        public double meanTickTime() {
            return 0;
        }
    };
}