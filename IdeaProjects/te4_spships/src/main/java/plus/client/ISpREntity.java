package plus.client;


public interface ISpREntity {
    double getSpXZRot();
    double getSpYRot();

    double getPrevSpXZRot();
    double getPrevSpYRot();

    void clearSpRot();

    boolean isActive();
    void setActive(boolean b);
}