package plus.common.mashine.item;


public class Booster extends ReactComponent {
    Booster(String name, int lvl, int usages) {
        super(name, lvl, usages);
    }

    public TYPE getType() {
        return TYPE.BOOSTER;
    }
}
