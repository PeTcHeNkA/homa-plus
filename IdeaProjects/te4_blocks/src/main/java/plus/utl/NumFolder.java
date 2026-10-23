package plus.utl;


public interface NumFolder {
    int getInt(int index);
    void setInt(int index, int val);
    boolean getBool(int index);
    void setBool(int index, boolean b);
    void insert(int i);
    int toInt();

    default NumFolder copy(int data){
        try {
            NumFolder f = this.getClass().newInstance();
            f.insert(data);
            return f;
        } catch (InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            return this;
        }
    }

    default NumFolder copy() {
        return copy(this.toInt());
    }
}