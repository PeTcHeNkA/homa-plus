package plus.te4net.mds.html.vr;


@FunctionalInterface
public interface IToStrAgent {
    boolean onAdd(StringBuilder b, ICitem parent, ICitem item);
}