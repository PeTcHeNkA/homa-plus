package plus.gf2.launch;


public interface ITranslationCallback {
    int getCurrent();
    int getTotal();

    boolean isCompleted();
}