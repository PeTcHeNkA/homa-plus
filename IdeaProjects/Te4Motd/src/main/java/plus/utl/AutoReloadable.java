package plus.utl;


public abstract class AutoReloadable implements Reloadable{

    public AutoReloadable(){
        Reloadable.add(this);
    }
}