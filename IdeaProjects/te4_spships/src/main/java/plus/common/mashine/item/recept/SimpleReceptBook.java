package plus.common.mashine.item.recept;


public abstract class SimpleReceptBook<T extends ICraftMatrix> extends ReceptBook<T, ReceptProcessor> {
    protected SimpleReceptBook(int controlSize) {
        super(controlSize);
    }
}