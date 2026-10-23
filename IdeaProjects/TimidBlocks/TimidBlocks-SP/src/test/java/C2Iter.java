import plus.utl.collections.CircularArrayQueue;
import java.util.function.Consumer;

public class C2Iter {
    private final CircularArrayQueue<String> list;
    private StringBuilder builder = new StringBuilder();

    public C2Iter(CircularArrayQueue<String> list) {
        this.list = list;
    }


    public void forEach(Consumer<String> action) {
        final int mMax = list.size();
        StringBuilder builder = this.builder;
        for (int min = 0; min < mMax; min++) {
            int curFWords = -1;
            for (int max = mMax; max > min; max--) {
                int curWords = 0;
                for (int index = min; index < max; index++) {
                    String s = list.getAtHead(index);
                    if(s != null){
                        builder.append(s).append(' ');
                        curWords++;
                    } else break;
                }
                if(curWords == curFWords){
                    builder.setLength(0);
                    continue;
                } else {
                    curFWords = curWords;
                }

                if(builder.length() != 0){
                    builder.setLength(builder.length() - 1);
                    action.accept(builder.toString());
                    builder.setLength(0);
                }
            }
        }
    }
}