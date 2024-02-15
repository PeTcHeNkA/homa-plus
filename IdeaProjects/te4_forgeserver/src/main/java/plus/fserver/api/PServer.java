package plus.fserver.api;


public interface PServer {
    Vars VARS = new Vars();
    class Vars{
        private Vars(){}
        public boolean announceAdvancements = true;
    }
}