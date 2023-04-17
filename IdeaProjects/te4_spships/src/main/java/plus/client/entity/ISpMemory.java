package plus.client.entity;

public interface ISpMemory {
    boolean isActive();
    void setActive(boolean b);

    double getPrevXZRot();
    double getPrevYRot();
    void setPrevXZRot(double v);
    void setPrevYRot(double v);

    double getCurXZRot();
    double getCurYRot();
    void setCurRot(double xz, double y);
    void setCurXZRot(double xz);
    void setCurYRot(double y);

    int getFreezeTimes();
    void setFreezeTimes(int t);
}
