package plus.bika;

import org.tensorflow.Graph;
import org.tensorflow.Session;

public class AIEngine {
    private Graph graph;
    private Session session;

    public AIEngine() {
        graph = new Graph();
        session = new Session(graph);
    }


    public void feed(InMatrix in, InMatrix out) {

        //session.runner().feed()
    }
}
